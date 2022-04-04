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
 *     Ericsson 		  	- Modified for additional features in DSF Reference implementation
 *******************************************************************************/

package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIOutput;
import gdb.mi.service.command.output.MIStackListVariablesInfo;

import java.util.ArrayList;

/**
 *
 *     -stack-list-locals PRINT-VALUES
 *
 *  Display the local variable names for the current frame.  With an
 * argument of 0 prints only the names of the variables, with argument of 1
 * prints also their values.
 *
 */
public class MIStackListVariables extends MICommand<MIStackListVariablesInfo> {

	public MIStackListVariables(boolean printValues) {
		this(printValues, "", "");
	}

	public MIStackListVariables(boolean printValues, String threadId, String frameId) {
		super("-stack-list-variables");

		final ArrayList<String> arguments = new ArrayList<>();
		if (!threadId.isEmpty()) {
			arguments.add("--thread");
			arguments.add(threadId);
		}

		if (!frameId.isEmpty()) {
			arguments.add("--frame");
			arguments.add(frameId);
		}

		if (printValues) {
			arguments.add("--all-values");
		} else {
			arguments.add("--no-values");
		}

		if (!arguments.isEmpty()) {
			setParameters(arguments.toArray(new String[0]));
		}
	}

	@Override
	public MIStackListVariablesInfo getResult(MIOutput out) {
		return new MIStackListVariablesInfo(out);
	}
}
