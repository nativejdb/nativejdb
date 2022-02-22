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
 * Conveys that gdb reported the target stopped because of a breakpoint. This
 * includes catchpoints, as gdb reports them as a breakpoint-hit. The
 * async-exec-output record looks like this:
 *
 * <code>
 *    ^stopped,reason="breakpoint-hit",bkptno="1",thread-id="0",frame={addr="0x08048468",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="4"}
 * </code>
 */
public class MIBreakpointHitEvent extends MIStoppedEvent {

	private String bkptno;

	/** @since 5.0 */
	protected MIBreakpointHitEvent(int token, MIResult[] results, MIFrame frame,
			String bkptno) {
		super(token, results, frame);
		this.bkptno = bkptno;
	}

	/** @since 5.0 */
	public String getNumber() {
		return bkptno;
	}

	public static MIBreakpointHitEvent parse(int token, MIResult[] results) {
		String bkptno = ""; //$NON-NLS-1$

		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();
			String str = ""; //$NON-NLS-1$
			if (value != null && value instanceof MIConst) {
				str = ((MIConst) value).getString();
			}

			if (var.equals("bkptno")) { //$NON-NLS-1$
				try {
					bkptno = str.trim();
				} catch (NumberFormatException e) {
				}
			}
		}

		MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(token, results);
		return new MIBreakpointHitEvent(token, results, stoppedEvent.getFrame(), bkptno);
	}
}
