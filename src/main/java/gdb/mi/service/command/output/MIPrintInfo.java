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

package gdb.mi.service.command.output;

/**
 * GDB print parsing.
 */
public class MIPrintInfo extends MIInfo {

	MIArg[] variables;

	public MIPrintInfo(MIOutput out) {
		super(out);
		variables = null;
		if (isDone()) {
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("variables")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							variables = MIArg.getMIArgs((MIList) value);
						} else if (value instanceof MITuple) {
							variables = MIArg.getMIArgs((MITuple) value);
						}
					}
				}
			}
		}
		if (variables == null) {
			variables = new MIArg[0];
		}
	}

	public MIArg[] getVariables() {
		return variables;
	}
}
