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
import gdb.mi.service.command.output.MIVarInfoExpressionInfo;

/**
 *
 *     -var-info-expression NAME
 *
 *  Returns what is represented by the variable object NAME:
 *
 *     lang=LANG-SPEC,exp=EXPRESSION
 *
 * where LANG-SPEC is `{"C" | "C++" | "Java"}'.
 *
 */

//MIVarInfoExpression.java
public class MIVarInfoExpression extends MICommand<MIVarInfoExpressionInfo> {
	/**
	 * @since 1.1
	 */
	public MIVarInfoExpression(String name) {
		super("-var-info-expression", new String[] { name }); //$NON-NLS-1$
	}

	@Override
	public MIVarInfoExpressionInfo getResult(MIOutput out) {
		return new MIVarInfoExpressionInfo(out);
	}
}
