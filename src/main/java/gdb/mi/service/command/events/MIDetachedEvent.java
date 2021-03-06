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

/**
 *
 *  ^running
 */
public class MIDetachedEvent extends MIEvent {

	/**
	 * @since 1.1
	 */
	public MIDetachedEvent(int token) {
		super(token, null);
	}

	@Override
	public String toString() {
		return "Detached"; //$NON-NLS-1$
	}
}
