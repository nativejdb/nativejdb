/*******************************************************************************
 * Copyright (c) 2017, 2018 Kichwa Coders Ltd and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIOutput;
import gdb.mi.service.command.output.MiSymbolInfoVariablesInfo;

import java.util.ArrayList;

/**
 *
 * -symbol-info-variables [--include-nondebug]
 *		[--type type_regexp]
 *		[--name name_regexp]
 *		[--max-results limit]
 *
 * Return a list containing the names and types for all global variables taken from the debug information. The variables are grouped by source file, and shown with the line number on which each variable is defined.
 *
 * The --include-nondebug option causes the output to include data symbols from the symbol table.
 *
 * The options --type and --name allow the symbols returned to be filtered based on either the name of the variable, or the type of the variable.
 *
 * The option --max-results restricts the command to return no more than limit results. If exactly limit results are returned then there might be additional results available if a higher limit is used.
 */
public class MISymbolInfoVariables extends MICommand<MiSymbolInfoVariablesInfo> {

	public MISymbolInfoVariables() {
		this("", "", 0, false);
	}

	public MISymbolInfoVariables(String typeRegExp, String nameRegExp, int maxResults, boolean includeNonDebug) {
		super("-symbol-info-variables");

		final ArrayList<String> arguments = new ArrayList<>();
		if (!typeRegExp.isEmpty()) {
			arguments.add("--type");
			arguments.add(typeRegExp);
		}

		if (!nameRegExp.isEmpty()) {
			arguments.add("--name");
			arguments.add(nameRegExp);
		}

		if (maxResults > 0) {
			arguments.add("--max-results");
			arguments.add(String.valueOf(maxResults));
		}

		if (includeNonDebug) {
			arguments.add("--include-nondebug");
		}

		if (!arguments.isEmpty()) {
			setParameters(arguments.toArray(new String[0]));
		}
	}

	@Override
	public MiSymbolInfoVariablesInfo getResult(MIOutput out) {
		return new MiSymbolInfoVariablesInfo(out);
	}
}