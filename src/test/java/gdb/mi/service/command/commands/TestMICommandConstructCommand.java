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

import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.output.MIInfo;
import org.junit.Test;

/**
 * Test verifying that the construct command method handles separators and
 * escaping correctly
 *
 */
public class TestMICommandConstructCommand {

	@Test
	public void multipleParametersShouldHaveCorrectSeparators() {
		// Setup
		MICommand<MIInfo> target = new MICommand<>("-test-operation");
		target.setOptions(new String[] { "-a a_test\\with slashes", "-b \"hello\"", "-c c_test" });
		target.setParameters(new String[] { "-param1 param", "param2", "-param3" });

		// Act
		String result = target.constructCommand();

		// Assert
		assertEquals("Wrong syntax for command",
				"-test-operation \"-a a_test\\\\with slashes\" \"-b \\\"hello\\\"\" \"-c c_test\" -- \"-param1 param\" param2 -param3\n",
				result);
	}

}
