package gdb.mi.service.command;

import gdb.mi.service.command.output.MIInfo;

import java.util.EventListener;

public interface Listener extends EventListener {
	void onEvent(MIInfo info);
}
