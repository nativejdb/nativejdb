/*******************************************************************************
 * Copyright (c) 2000, 2016 QNX Software Systems and others.
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

import gdb.mi.service.command.output.MIConst;
import gdb.mi.service.command.output.MILogStreamOutput;
import gdb.mi.service.command.output.MIOOBRecord;
import gdb.mi.service.command.output.MIResult;
import gdb.mi.service.command.output.MIStreamRecord;
import gdb.mi.service.command.output.MIValue;

/**
 * (gdb)
 * &"warning: Cannot insert breakpoint 2:\n"
 * &"Cannot access memory at address 0x8020a3\n"
 * 30^error,msg=3D"Cannot access memory at address 0x8020a3"=20
 */
public class MIErrorEvent extends MIStoppedEvent {

	final private String msg;
	final private String log;
	final private MIOOBRecord[] oobs;

	protected MIErrorEvent(int token, MIResult[] results, MIOOBRecord[] oobs, String msg,
			String log) {
		super(token, results, null);
		this.msg = msg;
		this.log = log;
		this.oobs = oobs;
	}

	public String getMessage() {
		return msg;
	}

	public String getLogMessage() {
		return log;
	}

	/**
	 * @since 5.3
	 */
	public static MIErrorEvent parse(int token, MIResult[] results, MIOOBRecord[] oobs) {
		String msg = "", log = ""; //$NON-NLS-1$ //$NON-NLS-2$

		if (results != null) {
			for (int i = 0; i < results.length; i++) {
				String var = results[i].getVariable();
				MIValue value = results[i].getMIValue();
				String str = ""; //$NON-NLS-1$
				if (value instanceof MIConst) {
					str = ((MIConst) value).getString();
				}

				if (var.equals("msg")) { //$NON-NLS-1$
					msg = str;
				}
			}
		}
		if (oobs != null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < oobs.length; i++) {
				if (oobs[i] instanceof MILogStreamOutput) {
					MIStreamRecord o = (MIStreamRecord) oobs[i];
					sb.append(o.getString());
				}
			}
			log = sb.toString();
		}
		return new MIErrorEvent(token, results, oobs, msg, log);
	}

	@Override
	public String toString() {
		if (oobs != null) {
			StringBuilder builder = new StringBuilder();
			for (MIOOBRecord oob : oobs) {
				builder.append(oob.toString());
			}
			builder.append(super.toString());
			return builder.toString();
		} else {
			return super.toString();
		}
	}
}
