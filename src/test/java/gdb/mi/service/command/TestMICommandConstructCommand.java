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

import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.output.MIInfo;
import org.junit.Test;

/**
 * Test verifying that the construct command method handles separators and
 * escaping correctly
 *
 * @author qtobsod
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
