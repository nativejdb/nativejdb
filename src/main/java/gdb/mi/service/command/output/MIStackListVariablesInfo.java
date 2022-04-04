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

package gdb.mi.service.command.output;

/**
 * GDB/MI stack list Variables parsing.
 * -stack-list-variables 1
 * ^done,variables=[{name="p",value="0x8048600 \"ghislaine\""},{name="buf",value="\"'\", 'x' <repeats 24 times>, \"i,xxxxxxxxx\", 'a' <repeats 24 times>"},{name="buf2",value="\"\\\"?'\\\\()~\""},{name="buf3",value="\"alain\""},{name="buf4",value="\"\\t\\t\\n\\f\\r\""},{name="i",value="0"}]
 *
 * On MacOS X 10.4 this returns a tuple:
 * ^done,variables={{name="p",value="0x8048600 \"ghislaine\""},{name="buf",value="\"'\", 'x' <repeats 24 times>, \"i,xxxxxxxxx\", 'a' <repeats 24 times>"},{name="buf2",value="\"\\\"?'\\\\()~\""},{name="buf3",value="\"alain\""},{name="buf4",value="\"\\t\\t\\n\\f\\r\""},{name="i",value="0"}}
 */
public class MIStackListVariablesInfo extends MIInfo {

	MIArg[] variables;

	public MIStackListVariablesInfo(MIOutput out) {
		super(out);
		variables = null;
		if (isDone()) {
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("variables")) { //$NON-NLS-1$
						MIValue value = results[i].getMIValue();
						if (value instanceof MIList) {
							variables = MIArg.getMIArgs((MIList) value);
						} else if (value instanceof MITuple) {
							variables = MIArg.getMIArgs((MITuple) value);
						}
					}
				}
			}
		}
		if (variables == null) {
			variables = new MIArg[0];
		}
	}

	public MIArg[] getVariables() {
		return variables;
	}
}
