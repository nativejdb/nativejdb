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
 *     Ericsson				- Modified for handling of execution contexts
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIInfo;

/**
 *
 *     -exec-next [count]
 *
 *  Asynchronous command.  Resumes execution of the inferior program,
 *  stopping when the beginning of the next source line is reached.
 *
 */
public class MIExecNext extends MICommand<MIInfo> {
	public MIExecNext() {
		this( 1);
	}

	public MIExecNext(int count) {
		super("-exec-next", new String[] { Integer.toString(count) }); //$NON-NLS-1$
	}
}