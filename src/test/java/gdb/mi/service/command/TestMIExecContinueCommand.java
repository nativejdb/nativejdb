package gdb.mi.service.command;

import gdb.mi.service.command.commands.MIBreakInsert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Verifies that the break insert MI command have the correct path substitution.
 */
public class TestMIExecContinueCommand {

	@Test
	public void pathWithSlashesShouldNotBeSubstituted() {
		MIBreakInsert target = new MIBreakInsert(false, false, null, 1, "/test/this/path:14", "4",
				false);

		assertEquals("Wrong syntax for command", "-break-insert -i 1 -p 4 /test/this/path:14\n",
				target.constructCommand());
	}

}
