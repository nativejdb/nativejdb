/*
 * Copyright (C) 2022 IBM Corporation
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License v2 with Classpath Exception.
 * The text of the license is available in the file LICENSE.TXT.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See LICENSE.TXT for more details.
 */

package gdb.mi.service.command.commands;

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
