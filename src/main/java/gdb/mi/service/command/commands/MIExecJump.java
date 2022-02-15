/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIInfo;

/**
 *
 * -exec-jump LOCATION
 *
 * <p>
 * Resumes execution of the inferior program at LOCATION
 * </p>
 *
 * Available since GDB 7.0
 *
 * @since 3.0
 */
public class MIExecJump extends MICommand<MIInfo> {
	public MIExecJump(String location) {
		super("-exec-jump", null, new String[] { location }); //$NON-NLS-1$
	}
}
