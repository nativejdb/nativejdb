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

import gdb.mi.service.command.output.MIResult;

public abstract class MIEvent {
	private final int fToken;
	private final MIResult[] fResults;

	public MIEvent(int token, MIResult[] results) {
		fToken = token;
		fResults = results;
	}

	public int getToken() {
		return fToken;
	}

	public MIResult[] getResults() {
		return fResults;
	}

	@Override
	public String toString() {
		if (fResults == null) {
			return super.toString();
		} else if (fResults.length == 1) {
			return fResults[0].toString();
		} else {
			StringBuilder builder = new StringBuilder();
			for (MIResult result : fResults) {
				builder.append(result);
				builder.append('\n');
			}
			return builder.toString();
		}
	}
}
