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
