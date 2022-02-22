/*******************************************************************************
 * Copyright (c) 2008, 2009 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package gdb.mi.service.command.events;

/**
 * This can only be detected by gdb/mi after GDB 6.8.
 * @since 1.1
 *
 */
public class MIThreadGroupCreatedEvent extends MIEvent {

	final private String fGroupId;

	public MIThreadGroupCreatedEvent(int token, String groupId) {
		super(token, null);
		fGroupId = groupId;
	}

	public String getGroupId() {
		return fGroupId;
	}

}
