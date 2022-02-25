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
 * *stopped,reason="location-reached",thread-id="0",frame={addr="0x0804858e",func="main2",args=[],file="hello.c",line="27"}
 */
public class MILocationReachedEvent extends MIStoppedEvent {

	protected MILocationReachedEvent(int token, MIResult[] results, MIFrame frame) {
		super(token, results, frame);
	}

	/**
	 * @since 1.1
	 */
	public static MILocationReachedEvent parse(int token, MIResult[] results) {
		MIStoppedEvent stoppedEvent = MIStoppedEvent.parse(token, results);
		return new MILocationReachedEvent(token, results, stoppedEvent.getFrame());
	}
}
