
/*******************************************************************************
 * Copyright (c) 2008, 2010 QNX Software Systems and others.
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
 *     Ericsson AB   		- Modified for new DSF Reference Implementation
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIInfo;

/**
 *
 *    -thread-select THREADNUM
 *
 * Make THREADNUM the current thread.  It prints the number of the new
 * current thread, and the topmost frame for that thread.
 *
 */

public class MIThreadSelect extends MICommand<MIInfo> {
	public MIThreadSelect(int threadNum) {
		this(Integer.toString(threadNum));
	}

	/**
	 * @since 1.1
	 */
	public MIThreadSelect(String threadNum) {
		super("-thread-select", new String[] { threadNum }); //$NON-NLS-1$
	}
}
