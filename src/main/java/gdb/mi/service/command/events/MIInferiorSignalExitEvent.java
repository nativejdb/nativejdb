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
 *******************************************************************************/

package gdb.mi.service.command.events;

import gdb.mi.service.command.output.MIConst;
import gdb.mi.service.command.output.MIResult;
import gdb.mi.service.command.output.MIValue;

/**
 * signal 2
 * "signal 2\n"
 * ^done,reason="exited-signalled",signal-name="SIGINT",signal-meaning="Interrupt"
 *
 */
public class MIInferiorSignalExitEvent extends MIEvent {

	final private String sigName;
	final private String sigMeaning;

	/**
	 * @since 1.1
	 */
	public MIInferiorSignalExitEvent(int token, MIResult[] results, String sigName,
			String sigMeaning) {
		super(token, results);
		this.sigName = sigName;
		this.sigMeaning = sigMeaning;
	}

	public String getName() {
		return sigName;
	}

	public String getMeaning() {
		return sigMeaning;
	}

	/**
	 * @since 1.1
	 */
	public static MIInferiorSignalExitEvent parse(int token, MIResult[] results) {
		String sigName = ""; //$NON-NLS-1$
		String sigMeaning = ""; //$NON-NLS-1$

		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$
			if (value instanceof MIConst) {
				str = ((MIConst) value).getString();
			}

			if (var.equals("signal-name")) { //$NON-NLS-1$
				sigName = str;
			} else if (var.equals("signal-meaning")) { //$NON-NLS-1$
				sigMeaning = str;
			}
		}
		return new MIInferiorSignalExitEvent(token, results, sigName, sigMeaning);
	}
}
