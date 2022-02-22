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

package gdb.mi.service.command.events;

/**
 * This can not be detected yet by gdb/mi.
 *
 */
public class MIThreadExitEvent extends MIEvent {

	final private String fThreadId;

	public MIThreadExitEvent(int id) {
		this(0, id);
	}

	public MIThreadExitEvent(int token, int id) {
		super(token, null);
		fThreadId = Integer.toString(id);
	}

	/**
	 * @since 1.1
	 */
	public MIThreadExitEvent(String threadId) {
		this(0, threadId);
	}

	/**
	 * @since 1.1
	 */
	public MIThreadExitEvent(int token, String threadId) {
		super(token, null);
		fThreadId = threadId;
	}

	public int getId() {
		try {
			return Integer.parseInt(fThreadId);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * @since 1.1
	 */
	public String getStrId() {
		return fThreadId;
	}
}
