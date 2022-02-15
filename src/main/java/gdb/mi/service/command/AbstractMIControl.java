/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 		  - Modified for handling of multiple stacks and threads
 *     Nokia - create and use backend service.
 *     Onur Akdemir (TUBITAK BILGEM-ITI) - Multi-process debugging (Bug 237306)
 *     Marc Khouzam (Ericsson) - New method to properly created ErrorThread (Bug 350837)
 *     Jason Litton (Sage Electronic Engineering, LLC) - Use Dynamic Tracing option (Bug 379169)
 *     Jonah Graham (Kichwa Coders) - Bug 317173 - cleanup warnings
 *     John Dallaway - Decode line breaks in status message (Bug 539455)
 *
 *
 * Copyright (C) 2022 IBM Corporation
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package gdb.mi.service.command;

import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.commands.RawCommand;
import gdb.mi.service.command.output.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

/**
 * Base implementation of an MI control service.  It provides basic handling
 * of input/output channels, and processing of the commands.
 * <p>
 * Extending classes need to implement the initialize() and shutdown() methods.
 */
public abstract class AbstractMIControl {
	private static final String MI_TRACE_IDENTIFIER = "[MI]"; //$NON-NLS-1$
	private static final int NUMBER_CONCURRENT_COMMANDS = 3;

	/*
	 *  Thread control variables for the transmit and receive threads.
	 */
	private TxThread fTxThread;
	private RxThread fRxThread;
	private ErrorThread fErrorThread;

	private final BlockingQueue<CommandHandle> fTxCommands = new LinkedBlockingQueue<>();
	private final Map<Integer, CommandHandle> fRxCommands = Collections
			.synchronizedMap(new HashMap<Integer, CommandHandle>());

	/**
	 *   Current command which have not been handed off to the backend yet.
	 */
	private final List<CommandHandle> fCommandQueue = new ArrayList<>();

	private CommandFactory fCommandFactory;


	public AbstractMIControl() {
		this(new CommandFactory());
	}

	public AbstractMIControl(CommandFactory factory) {
		super();
		fCommandFactory = factory;
	}

	public CommandFactory getCommandFactory() {
		return fCommandFactory;
	}

	/**
	 * Starts the threads that process the debugger input/output/error channels.
	 * To be invoked by the initialization routine of the extending class.
	 *
	 *
	 * @param inStream
	 * @param outStream
	 * @param errorStream
	 * @since 4.1
	 */
	public void startCommandProcessing(InputStream inStream, OutputStream outStream, InputStream errorStream) {

		fTxThread = new TxThread(outStream);
		fRxThread = new RxThread(inStream);

		if (errorStream != null) {
			fErrorThread = new ErrorThread(errorStream);
		}

		fTxThread.start();
		fRxThread.start();
		if (fErrorThread != null) {
			fErrorThread.start();
		}
	}

	public CommandHandle queueCommand(final MICommand<MIInfo> miCommand) {

		final CommandHandle handle = new CommandHandle(miCommand);

		// If the command control stopped processing commands, just return an error immediately.
			/*
			 *  We only allow three outstanding commands to be on the wire to the backend
			 *  at any one time. This allows for coalescing as well as canceling
			 *  existing commands on a state change. So we add it to the waiting list and let
			 *  the user know they can now work with this item if need be.
			 */
			fCommandQueue.add(handle);

			if (fRxCommands.size() < NUMBER_CONCURRENT_COMMANDS) {
				processNextQueuedCommand();
			}

		return handle;
	}

	private void processNextQueuedCommand() {
		if (!fCommandQueue.isEmpty()) {
			final CommandHandle handle = fCommandQueue.remove(0);
			if (handle != null) {
				if (!(handle.getCommand() instanceof RawCommand)) {
					// Only generate a token id if the command is not a RawCommand
					// RawCommands are sent to GDB without an answer expected, so we don't
					// need a token id.  In fact, GDB will fail if we send one in this case.
					handle.generateTokenId();
				}
				fTxCommands.add(handle);
			}
		}
	}

	/*
	 * A global counter for all command, the token will be use to identify uniquely a command.
	 * Unless the value wraps around which is unlikely.
	 */
	private int fTokenIdCounter = 0;

	private int getNewTokenId() {
		int count = ++fTokenIdCounter;
		// If we ever wrap around.
		if (count <= 0) {
			count = fTokenIdCounter = 1;
		}
		return count;
	}

	/*
	 *  Support class which creates a convenient wrapper for holding all information about an
	 *  individual request.
	 */
	private class CommandHandle {

		private MICommand<MIInfo> fCommand;
		private int fTokenId;

		CommandHandle(MICommand<MIInfo> c) {
			fCommand = c;
			fTokenId = -1; // Only initialize to a real value when needed
		}

		public MICommand<MIInfo> getCommand() {
			return fCommand;
		}

		// This method allows us to generate the token Id when we area actually going to use
		// it.  It is meant to help order the token ids based on when commands will actually
		// be sent
		public void generateTokenId() {
			fTokenId = getNewTokenId();
		}

		public Integer getTokenId() {
			return fTokenId;
		}

		@Override
		public String toString() {
			return Integer.toString(fTokenId) + fCommand;
		}
	}

	/*
	 *  This is the transmitter thread. When a command is given to this thread it has been
	 *  considered to be sent, even if it has not actually been sent yet.  This assumption
	 *  makes it easier from state management.  Whomever fill this pipeline handles all of
	 *  the required state notification ( callbacks ). This thread simply physically gives
	 *  the message to the backend.
	 */
	private class TxThread extends Thread {

		final private OutputStream fOutputStream;

		public TxThread(OutputStream outStream) {
			super("MI TX Thread"); //$NON-NLS-1$
			fOutputStream = outStream;
		}

		@Override
		public void run() {
			while (true) {
				CommandHandle commandHandle = null;

				try {
					commandHandle = fTxCommands.take();
				} catch (InterruptedException e) {
					break; // Shutting down.
				}

				/*
				 *  We note that this is an outstanding request at this point.
				 */
				if (!(commandHandle.getCommand() instanceof RawCommand)) {
					// RawCommands will not get an answer, so we cannot put them in the receive queue.
					fRxCommands.put(commandHandle.getTokenId(), commandHandle);
				}

				/*
				 *   Construct the new command and push this command out the pipeline.
				 */
				final String str;
				if (commandHandle.getCommand() instanceof RawCommand) {
					// RawCommands CANNOT have a token id: GDB would read it as part of the RawCommand!
					str = commandHandle.getCommand().constructCommand();
				} else {
					str = commandHandle.getTokenId() + commandHandle.getCommand().constructCommand();
				}

				try {
					if (fOutputStream != null) {
						fOutputStream.write(str.getBytes());
						fOutputStream.flush();
					}
				} catch (IOException e) {
					break;
				}
			}
			// Must close the stream here to avoid leaking
			try {
				if (fOutputStream != null)
					fOutputStream.close();
			} catch (IOException e) {
			}
		}
	}

	private class RxThread extends Thread {
		private final InputStream fInputStream;
		private final MIParser fMiParser = new MIParser();

		/**
		 * List of out of band records since the last result record. Out of band
		 * records are required for processing the results of CLI commands.
		 */
		private final List<MIOOBRecord> fAccumulatedOOBRecords = new LinkedList<>();

		/**
		 * List of stream records since the last result record, not including
		 * the record currently being processed (if it's a stream one). This is
		 * a subset of {@link #fAccumulatedOOBRecords}, as a stream record is a
		 * particular type of OOB record.
		 */
		private final List<MIStreamRecord> fAccumulatedStreamRecords = new LinkedList<>();

		public RxThread(InputStream inputStream) {
			super("MI RX Thread"); //$NON-NLS-1$
			fInputStream = inputStream;
		}

		@Override
		public void run() {
			BufferedReader reader = new BufferedReader(new InputStreamReader(fInputStream));
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.length() != 0) {
						System.out.println(line);
						processMIOutput(line);
					}
				}
			} catch (IOException e) {
				// Socket is shut down.
			} catch (RejectedExecutionException e) {
				// Dispatch thread is down.
			}
			// Must close the stream here to avoid leaking and
			// to give enough time to read all the data
			try {
				fInputStream.close();
			} catch (IOException e) {
			}
		}

		private MIResult findResultRecord(MIResult[] results, String variable) {
			for (int i = 0; i < results.length; i++) {
				if (variable.equals(results[i].getVariable())) {
					return results[i];
				}
			}
			return null;
		}

		void processMIOutput(String line) {

			MIParser.RecordType recordType = fMiParser.getRecordType(line);

			if (recordType == MIParser.RecordType.ResultRecord) {
				final MIResultRecord rr = fMiParser.parseMIResultRecord(line);

				/*
				 *  Find the command in the current output list. If we cannot then this is
				 *  some form of asynchronous notification. Or perhaps general IO.
				 */
				int id = rr.getToken();

				final CommandHandle commandHandle = fRxCommands.remove(id);

				if (commandHandle != null) {
					final MIOutput response = new MIOutput(rr,
							fAccumulatedOOBRecords.toArray(new MIOOBRecord[fAccumulatedOOBRecords.size()]));
					fAccumulatedOOBRecords.clear();
					fAccumulatedStreamRecords.clear();
				} else {
					/*
					 *  GDB apparently can sometimes send multiple responses to the same command.  In those cases,
					 *  the command handle is gone, so post the result as an event.  To avoid processing OOB records
					 *  as events multiple times, do not include the accumulated OOB record list in the response
					 *  MIOutput object.
					 */
					final MIOutput response = new MIOutput(rr, new MIOOBRecord[0]);


				}
			} else if (recordType == MIParser.RecordType.OOBRecord) {
				// Process OOBs
				final MIOOBRecord oob = fMiParser.parseMIOOBRecord(line);

				fAccumulatedOOBRecords.add(oob);
				// limit growth, but only if these are not responses to CLI commands
				if (fRxCommands.isEmpty() && fAccumulatedOOBRecords.size() > 20
						|| fAccumulatedOOBRecords.size() > 100000) {
					fAccumulatedOOBRecords.remove(0);
				}

				// The handling of this OOB record may need the stream records
				// that preceded it. One such case is a stopped event caused by a
				// catchpoint in gdb < 7.0. The stopped event provides no
				// reason, but we can determine it was caused by a catchpoint by
				// looking at the target stream.

				final MIOutput response = new MIOutput(oob,
						fAccumulatedStreamRecords.toArray(new MIStreamRecord[fAccumulatedStreamRecords.size()]));

				// If this is a stream record, add it to the accumulated bucket
				// for possible use in handling a future OOB (see comment above)
				if (oob instanceof MIStreamRecord) {
					fAccumulatedStreamRecords.add((MIStreamRecord) oob);
					if (fAccumulatedStreamRecords.size() > 20) { // limit growth; see bug 302927
						fAccumulatedStreamRecords.remove(0);
					}
				}
			}
			processNextQueuedCommand();

		}
	}

	/**
	 * A thread that will read GDB's stderr stream.
	 * When a PTY is not being used for the inferior, everything
	 * the inferior writes to stderr will be output on GDB's stderr.
	 * If we don't read it, gdb eventually blocks, when the sream is
	 * full.
	 *
	 * Although we could write this error output to the inferior
	 * console, we actually write it to the GDB console.  This is
	 * because we cannot differentiate between inferior errors printouts
	 * and GDB error printouts.
	 *
	 * See bug 327617 for details.
	 */
	private class ErrorThread extends Thread {
		private final InputStream fErrorStream;
		private final MIParser fMiParser = new MIParser();

		public ErrorThread(InputStream errorStream) {
			super("MI Error Thread"); //$NON-NLS-1$
			fErrorStream = errorStream;
		}

		@Override
		public void run() {
			BufferedReader reader = new BufferedReader(new InputStreamReader(fErrorStream));
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					// Create an error MI out-of-band record so that our gdb console prints it.
					final MIOOBRecord oob = fMiParser.parseMIOOBRecord("&" + line + "\n"); //$NON-NLS-1$//$NON-NLS-2$
				}
			} catch (IOException e) {
				// Socket is shut down.
			} catch (RejectedExecutionException e) {
				// Dispatch thread is down.
			}
			// Must close the stream here to avoid leaking
			try {
				fErrorStream.close();
			} catch (IOException e) {
			}
		}
	}
}
