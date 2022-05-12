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

import gdb.mi.service.command.commands.MIBreakDelete;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMIBreakDeleteCommand {

	@Test
	public void deleteSingleBreakpoint() {
		MIBreakDelete target = new MIBreakDelete(new String[]{"1"});

		assertEquals("", "-break-delete 1\n",
				target.constructCommand());
	}

}
