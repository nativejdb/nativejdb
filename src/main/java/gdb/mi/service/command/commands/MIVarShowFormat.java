/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
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
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIOutput;
import gdb.mi.service.command.output.MIVarShowFormatInfo;

/**
 *
 *    -var-show-format NAME
 *
 *  Returns the format used to display the value of the object NAME.
 *
 *     FORMAT ==>
 *     FORMAT-SPEC
 *
 */
public class MIVarShowFormat extends MICommand<MIVarShowFormatInfo> {
	/**
	 * @since 1.1
	 */
	public MIVarShowFormat(String name) {
		super("-var-show-format", new String[] { name }); //$NON-NLS-1$
	}

	@Override
	public MIVarShowFormatInfo getResult(MIOutput out) {
		return new MIVarShowFormatInfo(out);
	}
}
