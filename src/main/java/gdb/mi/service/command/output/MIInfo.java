/*******************************************************************************
 * Copyright (c) 2000, 2011 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *******************************************************************************/

package gdb.mi.service.command.output;

/**
 * Base class for teh parsing/info GDB/MI classes.
 */
public class MIInfo {

	private final MIOutput miOutput;
	byte eventKind;
	byte suspendPolicy;
	int requestID;

	public MIInfo(MIOutput record) {
		miOutput = record;
	}

	public MIOutput getMIOutput() {
		return miOutput;
	}

	public int getMIInfoRequestID() {
		return requestID;
	}

	public byte getMIInfoEventKind() {
		return eventKind;
	}

	public byte getMIInfoSuspendPolicy() {
		return suspendPolicy;
	}

	public void setMIInfoRequestID(int id) {
		this.requestID = id;
	}

	public void setMIInfoEventKind(byte eventKind) {
		this.eventKind = eventKind;
	}

	public void setMIInfoSuspendPolicy(byte suspendPolicy) {
		this.suspendPolicy = suspendPolicy;
	}

	public boolean isDone() {
		return isResultClass(MIResultRecord.DONE);
	}

	public boolean isRunning() {
		return isResultClass(MIResultRecord.RUNNING);
	}

	public boolean isConnected() {
		return isResultClass(MIResultRecord.CONNECTED);
	}

	public boolean isError() {
		return isResultClass(MIResultRecord.ERROR);
	}

	public boolean isExit() {
		return isResultClass(MIResultRecord.EXIT);
	}

	@Override
	public String toString() {
		if (miOutput != null) {
			return miOutput.toString();
		}
		return ""; //$NON-NLS-1$
	}

	boolean isResultClass(String rc) {
		if (miOutput != null) {
			MIResultRecord rr = miOutput.getMIResultRecord();
			if (rr != null) {
				String clazz = rr.getResultClass();
				return clazz.equals(rc);
			}
		}
		return false;
	}

	public String getErrorMsg() {
		if (miOutput != null) {
			MIResultRecord rr = miOutput.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("msg")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIConst) {
							String s = ((MIConst) value).getCString();
							return s;
						}
					}
				}
			}
		}
		return ""; //$NON-NLS-1$
	}
}
