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
 */
public class MISignalChangedEvent extends MIEvent {

	final private String name;

	public MISignalChangedEvent(String n) {
		this(0, n);
	}

	public MISignalChangedEvent(int id, String n) {
		super(id, null);
		name = n;
	}

	public String getName() {
		return name;
	}

}
