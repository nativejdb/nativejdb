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

import gdb.mi.service.command.output.MIFrame;
import gdb.mi.service.command.output.MIResult;

/**
 *
 *  *stopped,reason="end-stepping-range",thread-id="0",frame={addr="0x08048538",func="main",args=[{name="argc",value="1"},{name="argv",value="0xbffff18c"}],file="hello.c",line="13"}
 */
public class MISteppingRangeEvent extends MIStoppedEvent {

	protected MISteppingRangeEvent(int token, MIResult[] results, MIFrame frame) {
		super(token, results, frame);
	}

	/**
	 * @since 1.1
	 */
	public static MISteppingRangeEvent parse(int token, MIResult[] results) {
		MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(token, results);
		return new MISteppingRangeEvent(token, results, stoppedEvent.getFrame());
	}
}
