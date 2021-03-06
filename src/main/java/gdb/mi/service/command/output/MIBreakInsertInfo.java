/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
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

package gdb.mi.service.command.output;

import java.util.ArrayList;
import java.util.List;

/**
 * -break-insert main
 * ^done,bkpt={number="1",type="breakpoint",disp="keep",enabled="y",addr="0x08048468",func="main",file="hello.c",line="4",times="0"}
 * -break-insert -a p
 * ^done,hw-awpt={number="2",exp="p"}
 * -break-watch -r p
 * ^done,hw-rwpt={number="4",exp="p"}
 * -break-watch p
 * ^done,wpt={number="6",exp="p"}
 */
public class MIBreakInsertInfo extends MIInfo {

	MIBreakpoint bpt;
	byte eventKind;
	byte suspendPolicy;
	int requestID;

	public MIBreakInsertInfo(MIOutput record) {
		super(record);
		if (isDone()) {
			MIResultRecord rr = record.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					MIValue val = results[i].getMIValue();
					if (var.equals("wpt")) { //$NON-NLS-1$
						if (val instanceof MITuple) {
							this.bpt = createMIBreakpoint((MITuple) val);
							this.bpt.setEnabled(true);
							this.bpt.setWriteWatchpoint(true);
						}
					} else if (var.equals("bkpt")) { //$NON-NLS-1$
						if (val instanceof MITuple) {
							bpt = createMIBreakpoint((MITuple) val);
						}
					} else if (var.equals("hw-awpt")) { //$NON-NLS-1$
						if (val instanceof MITuple) {
							bpt = createMIBreakpoint((MITuple) val);
							bpt.setAccessWatchpoint(true);
							bpt.setEnabled(true);
						}
					} else if (var.equals("hw-rwpt")) { //$NON-NLS-1$
						if (val instanceof MITuple) {
							bpt = createMIBreakpoint((MITuple) val);
							bpt.setReadWatchpoint(true);
							bpt.setEnabled(true);
						}
					}
				}
			}
		}
	}

	public MIBreakpoint getMIBreakpoint() {
		return bpt;
	}

	/**
	 * Create a target specific MIBreakpoint
	 *
	 * @param tuple
	 *            tuple suitable for passing to MIBreakpoint constructor
	 * @return new breakpoint
	 * @since 5.3
	 */
	protected MIBreakpoint createMIBreakpoint(MITuple tuple) {
		return new MIBreakpoint(tuple);
	}
}
