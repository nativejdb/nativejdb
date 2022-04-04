/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
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
 *     Ericsson				- Modified for handling of Frame contexts
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIOutput;
import gdb.mi.service.command.output.MIStackListFramesInfo;

/**
 *
 *     -stack-list-frames [ LOW-FRAME HIGH-FRAME ]
 *
 *  List the frames currently on the stack.  For each frame it displays
 * the following info:
 *
 * `LEVEL'
 *    The frame number, 0 being the topmost frame, i.e. the innermost
 *    function.
 *
 * `ADDR'
 *    The `$pc' value for that frame.
 *
 * `FUNC'
 *    Function name.
 *
 * `FILE'
 *    File name of the source file where the function lives.
 *
 * `FULLNAME'
 *    Absolute file name of the source file where the function lives.
 *    @since gdb 6.4
 *
 * `LINE'
 *   Line number corresponding to the `$pc'.
 *
 *  If invoked without arguments, this command prints a backtrace for the
 * whole stack.  If given two integer arguments, it shows the frames whose
 * levels are between the two arguments (inclusive).  If the two arguments
 * are equal, it shows the single frame at the corresponding level.
 *
 */
public class MIStackListFrames extends MICommand<MIStackListFramesInfo> {
	public MIStackListFrames(String threadId) {
		super("-stack-list-frames", new String[] {"--thread", threadId}); //$NON-NLS-1$
	}

	public MIStackListFrames(String threadId, int low, int high) {
		super("-stack-list-frames", new String[] {"--thread", threadId, Integer.toString(low), Integer.toString(high) }); //$NON-NLS-1$
	}

	@Override
	public MIStackListFramesInfo getResult(MIOutput out) {
		return new MIStackListFramesInfo(out);
	}
}
