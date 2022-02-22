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
 *
 *  ^running
 */
public class MIRunningEvent extends MIEvent {
	public static final int CONTINUE = 0;
	public static final int NEXT = 1;
	public static final int NEXTI = 2;
	public static final int STEP = 3;
	public static final int STEPI = 4;
	public static final int FINISH = 5;
	public static final int UNTIL = 6;
	public static final int RETURN = 7;

	final private int type;
	final private int threadId;

	public MIRunningEvent(int token, int t) {
		this(token, t, -1);
	}

	public MIRunningEvent(int token, int t, int threadId) {
		super(token, null);
		type = t;
		this.threadId = threadId;
	}

	public int getType() {
		return type;
	}

	public int getThreadId() {
		return threadId;
	}

	@Override
	public String toString() {
		return "Running"; //$NON-NLS-1$
	}
}
