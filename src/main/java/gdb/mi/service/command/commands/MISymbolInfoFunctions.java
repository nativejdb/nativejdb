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
import gdb.mi.service.command.output.MiSymbolInfoFunctionsInfo;

import java.util.ArrayList;

/**
 *
 *  -symbol-info-functions [--include-nondebug]
 *                         [--type type_regexp]
 *                         [--name name_regexp]
 *                         [--max-results limit]
 *
 * Return a list containing the names and types for all global functions taken from the debug information.
 * The functions are grouped by source file, and shown with the line number on which each function is defined.
 *
 * The --include-nondebug option causes the output to include code symbols from the symbol table.
 *
 * The options --type and --name allow the symbols returned to be filtered based on either the name of the function, or the type signature of the function.
 *
 * The option --max-results restricts the command to return no more than limit results.
 * If exactly limit results are returned then there might be additional results available if a higher limit is used.
 */
public class MISymbolInfoFunctions extends MICommand<MiSymbolInfoFunctionsInfo> {

	public MISymbolInfoFunctions() {
		this("", "", 0, false);
	}

	public MISymbolInfoFunctions(String typeRegExp, String nameRegExp, int maxResults, boolean includeNonDebug) {
		super("-symbol-info-functions");

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
	public MiSymbolInfoFunctionsInfo getResult(MIOutput out) {
		return new MiSymbolInfoFunctionsInfo(out);
	}
}