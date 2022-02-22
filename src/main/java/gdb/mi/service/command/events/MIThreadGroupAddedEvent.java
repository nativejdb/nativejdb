/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package gdb.mi.service.command.events;

/**
 * =thread-group-added,id="i1"
 *
 * This can only be detected by gdb/mi with GDB >= 7.2.
 * @since 5.1
 */
public class MIThreadGroupAddedEvent extends MIEvent {

	final private String fGroupId;

	public MIThreadGroupAddedEvent(int token, String groupId) {
		super( token, null);
		fGroupId = groupId;
	}

	public String getGroupId() {
		return fGroupId;
	}
}
