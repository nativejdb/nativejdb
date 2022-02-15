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
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIInfo;

/**
 *
 *      -exec-step-instruction [count]

 *  Asynchronous command.  Resumes the inferior which executes one
 * machine instruction.  The output, once GDB has stopped, will vary
 * depending on whether we have stopped in the middle of a source line or
 * not.  In the former case, the address at which the program stopped will
 * be printed as well.
 *
 */
public class MIExecStepInstruction extends MICommand<MIInfo> {
	public MIExecStepInstruction() {
		this( 1);
	}

	public MIExecStepInstruction(int count) {
		super( "-exec-step-instruction", new String[] { Integer.toString(count) }); //$NON-NLS-1$
	}
}
