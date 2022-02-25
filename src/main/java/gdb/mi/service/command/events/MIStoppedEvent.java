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
import gdb.mi.service.command.output.MITuple;
import gdb.mi.service.command.output.MIValue;

/**
 *  *stopped
 *
 */
public class MIStoppedEvent extends MIEvent {

	final private MIFrame frame;

	protected MIStoppedEvent(int token, MIResult[] results, MIFrame frame) {
		super(token, results);
		this.frame = frame;
	}

	public MIFrame getFrame() {
		return frame;
	}

	/**
	 * @since 1.1
	 */
	public static MIStoppedEvent parse(int token, MIResult[] results) {
		MIFrame frame = null;

		for (int i = 0; i < results.length; i++) {
			String var = results[i].getVariable();
			MIValue value = results[i].getMIValue();

			if (var.equals("frame")) { //$NON-NLS-1$
				if (value instanceof MITuple) {
					frame = new MIFrame((MITuple) value);
				}
			}
		}
		return new MIStoppedEvent(token, results, frame);
	}
}
