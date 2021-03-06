/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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
 *   -break-delete ( BREAKPOINT )+
 *
 * Delete the breakpoint(s) whose number(s) are specified in the
 * argument list.  This is obviously reflected in the breakpoint list.
 *
 * Result:
 *  ^done
 *
 */

public class MIBreakDelete extends MICommand<MIInfo> {
	/** @since 5.0 */
	public MIBreakDelete(String[] array) {
		super("-break-delete"); //$NON-NLS-1$
		if (array != null && array.length > 0) {
			String[] brkids = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				brkids[i] = array[i];
			}
			setParameters(brkids);
		}
	}
}
