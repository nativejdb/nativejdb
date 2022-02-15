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
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIInfo;

/**
 *
 *      -exec-continue [--all | --thread-group ID]
 *
 *   Asynchronous command.  Resumes the execution of the inferior program
 *   until a breakpoint is encountered, or until the inferior exits.
 *
 */
public class MIExecContinue extends MICommand<MIInfo> {
	public MIExecContinue() {
		this(false);
	}

	/**
	 * @since 1.1
	 */
	public MIExecContinue(boolean allThreads) {
		this(allThreads, null);
	}

	/**
	 * @since 3.0
	 */
	public MIExecContinue(String groupId) {
		this(false, groupId);
	}

	/*
	 * The parameters allThreads and groupId are mutually exclusive.  allThreads must be false
	 * if we are to use groupId.  The value of this method is to only have one place
	 * where we use the hard-coded strings.
	 */
	private MIExecContinue( boolean allThreads, String groupId) {
		super("-exec-continue"); //$NON-NLS-1$
		if (allThreads) {
			setParameters(new String[] { "--all" }); //$NON-NLS-1$
		} else if (groupId != null) {
			setParameters(new String[] { "--thread-group", groupId }); //$NON-NLS-1$
		}
	}
}
