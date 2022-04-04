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
 *     Wind River Systems   - Modified for new DSF Reference Implementation
 *     Ericsson				- Modified for handling of frame contexts
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIOutput;
import gdb.mi.service.command.output.MIVarSetFormatInfo;

/**
 *
 *    -var-set-format NAME FORMAT-SPEC
 *
 *  Sets the output format for the value of the object NAME to be
 * FORMAT-SPEC.
 *
 *  The syntax for the FORMAT-SPEC is as follows:
 *
 *     FORMAT-SPEC ==>
 *     {binary | decimal | hexadecimal | octal | natural}
 *
 */
public class MIVarSetFormat extends MICommand<MIVarSetFormatInfo> {

	/**
	 * These strings represent the standard known formats for any bit stream
	 * which needs to be formatted. These ID's as well as others which may be
	 * specifically available from the backend are what is returned from the
	 * getID() method.
	 */
	public final static String HEX_FORMAT = "HEX.Format"; //$NON-NLS-1$
	public final static String OCTAL_FORMAT = "OCTAL.Format"; //$NON-NLS-1$
	public final static String NATURAL_FORMAT = "NATURAL.Format"; //$NON-NLS-1$
	public final static String BINARY_FORMAT = "BINARY.Format"; //$NON-NLS-1$
	public final static String DECIMAL_FORMAT = "DECIMAL.Format"; //$NON-NLS-1$
	public final static String STRING_FORMAT = "STRING.Format"; //$NON-NLS-1$
	
	/**
	 * @since 1.1
	 */
	public MIVarSetFormat(String name, String fmt) {
		super("-var-set-format"); //$NON-NLS-1$
		setParameters(new String[] { name, getFormat(fmt) });
	}

	private String getFormat(String fmt) {
		String format = "natural"; //$NON-NLS-1$

		if (HEX_FORMAT.equals(fmt)) {
			format = "hexadecimal"; //$NON-NLS-1$
		} else if (BINARY_FORMAT.equals(fmt)) {
			format = "binary"; //$NON-NLS-1$
		} else if (OCTAL_FORMAT.equals(fmt)) {
			format = "octal"; //$NON-NLS-1$
		} else if (NATURAL_FORMAT.equals(fmt)) {
			format = "natural"; //$NON-NLS-1$
		} else if (DECIMAL_FORMAT.equals(fmt)) {
			format = "decimal"; //$NON-NLS-1$
		}
		return format;
	}

	@Override
	public MIVarSetFormatInfo getResult(MIOutput out) {
		return new MIVarSetFormatInfo(out);
	}
}
