/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
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
 *     Ericsson				- Modified for handling of execution contexts
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIInfo;

/**
 *
 *      -exec-run [ARGS]
 *
 *   Asynchronous command.  Starts execution of the inferior from the
 * beginning.  The inferior executes until either a breakpoint is
 * encountered or the program exits.
 *
 * ARGS will be passed to the inferior.  This option is not documented.
 *
 */
public class MIExecRun extends MICommand<MIInfo> {
	public MIExecRun() {
		super( "-exec-run"); //$NON-NLS-1$
	}

	public MIExecRun(String[] args) {
		super("-exec-run", args); //$NON-NLS-1$
	}
}
