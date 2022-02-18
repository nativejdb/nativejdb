package gdb.mi.service.command;

import gdb.mi.service.command.output.MIInfo;
import jdwp.GDBControl;
import jdwp.Packet;

public class AsyncListener implements Listener{
	GDBControl gc;

	public AsyncListener(GDBControl gc) {
		this.gc = gc;
		this.gc.addEventListener(this);
	}

	@Override
	public void onEvent(MIInfo info) {
		Packet packet = translate(info);
		System.out.print("^^^^^^^^ Async packet onEvent: " + info);
		gc.sendToTarget(packet);
	}

	private Packet translate(MIInfo info) {
		return new Packet();
	}
}
