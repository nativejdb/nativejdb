/*******************************************************************************
 * Copyright (c) 2008, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *     Marc Khouzam (Ericsson) - Fix NPE (bug 369583)
 *     Alvaro Sanchez-Leon (Ericsson) - Bug 437562 - Split the dsf-gdb tests to a plug-in and fragment pair
 *******************************************************************************/
package gdb.mi.service.command;

import static org.junit.Assert.assertEquals;
import gdb.mi.service.command.commands.MIBreakInsert;
import org.junit.Test;

/**
 * Verifies that the break insert MI command have the correct path substitution.
 */
public class TestMIBreakInsertCommand {

	@Test
	public void pathWithSlashesShouldNotBeSubstituted() {
		MIBreakInsert target = new MIBreakInsert(false, false, null, 1, "/test/this/path:14", "4",
				false);

		assertEquals("Wrong syntax for command", "-break-insert -i 1 -p 4 /test/this/path:14\n",
				target.constructCommand());
	}

}
