/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIGDBVersionInfo;
import gdb.mi.service.command.output.MIInfo;
import gdb.mi.service.command.output.MIOutput;

/**
 *
 *     -gdb-version
 *
 * @since 4.6
 *
 */
public class MIGDBVersion extends MICommand<MIGDBVersionInfo> {
	private static final String COMMAND = "-gdb-version"; //$NON-NLS-1$

	public MIGDBVersion() {
		super( COMMAND);
	}

	@Override
	public MIInfo getResult(MIOutput out) {
		return new MIGDBVersionInfo(out);
	}
}
