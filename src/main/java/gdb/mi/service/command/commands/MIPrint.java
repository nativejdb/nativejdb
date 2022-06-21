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
 * print [Expression]
 * print $[Previous value number]
 * print {[Type]}[Address]
 * print [First element]@[Element count]
 * print /[Format] [Expression]
 *
 * Parameters
 * Expression
 * Specifies the expression that will be evaluated and printed. The expression can include references to variables (e.g. i), registers (e.g. $eax) and pseudo-registers (e.g. $pc). Note that if your expression refers to a local variable, you need to ensure that you have selected the correct frame. You can navigate frames using the up and down commands.
 * Previous value number
 * When this format is used and i is specified as the previous value number, the print command will repeat the output produced by its i-th invocation.
 * Type/Address
 * This format allows explicitly specifying the address of the evaluated expression and can be used as a shortcut to the C/C++ type conversion. E.g. *((int *)p) is equivalent to {int}p
 * First element/Element count
 * This form allows interpreting the First element expression as an array of Element count sequential elements. The most common example of it is *argv@argc
 * Format
 * If specified, allows overriding the output format used by the command. Valid format specifiers are:
 * o - octal
 * x - hexadecimal
 * u - unsigned decimal
 * t - binary
 * f - floating point
 * a - address
 * c - char
 * s - string
 *
 *
 */
public class MIPrint extends MICommand<MIInfo> {
	public MIPrint() {
		super("print");
	}

	public MIPrint(String expression) {
		super("print", new String[]{expression});
	}
}
