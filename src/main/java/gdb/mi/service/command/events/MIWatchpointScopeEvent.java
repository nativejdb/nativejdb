/*******************************************************************************
 * Copyright (c) 2009, 2016 QNX Software Systems and others.
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
import gdb.mi.service.command.output.MIFrame;
import gdb.mi.service.command.output.MIResult;
import gdb.mi.service.command.output.MIValue;

/**
 *  *stopped,reason="watchpoint-scope",wpnum="5",
 *
 */
public class MIWatchpointScopeEvent extends MIStoppedEvent {

	final private String number;

	/** @since 5.0 */
	protected MIWatchpointScopeEvent(int token, MIResult[] results, MIFrame frame,
			String number) {
		super(token, results, frame);
		this.number = number;
	}

	/** @since 5.0 */
	public String getNumber() {
		return number;
	}

	/**
	 * @since 1.1
	 */
	public static MIWatchpointScopeEvent parse(int token, MIResult[] results) {
		String number = ""; //$NON-NLS-1$
		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();

			if (var.equals("wpnum")) { //$NON-NLS-1$
				if (value instanceof MIConst) {
					String str = ((MIConst) value).getString();
					try {
						number = str.trim();
					} catch (NumberFormatException e) {
					}
				}
			}
		}

		MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(token, results);
		return new MIWatchpointScopeEvent(token, results, stoppedEvent.getFrame(), number);
	}
}
