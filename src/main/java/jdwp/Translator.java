/* Copyright (C) 2022 IBM Corporation
*
* This program is free software; you can redistribute and/or modify it under
* the terms of the GNU General Public License v2 with Classpath Exception.
* The text of the license is available in the file LICENSE.TXT.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE. See LICENSE.TXT for more details.
*/

package jdwp;

import gdb.mi.service.command.events.MIBreakpointHitEvent;
import gdb.mi.service.command.events.MIEvent;
import gdb.mi.service.command.output.MIBreakInsertInfo;
import gdb.mi.service.command.output.MIResult;
import gdb.mi.service.command.output.MIValue;
import jdwp.jdi.LocationImpl;

public class Translator {

	public static PacketStream translate(GDBControl gc, MIEvent event) {
		if (event instanceof MIBreakpointHitEvent) {
			return translateBreakpointHit(gc, (MIBreakpointHitEvent) event);
		}
		return null;
	}

	private static PacketStream translateBreakpointHit(GDBControl gc, MIBreakpointHitEvent event) {
		PacketStream packetStream = new PacketStream(gc);
		Integer eventNumber = Integer.parseInt(event.getNumber());
		MIBreakInsertInfo info = JDWP.bkptsByBreakpointNumber.get(eventNumber);
		if (info == null) { // This happens for a synthetic breakpoint (not set by the user)
			return null;
		}
		byte suspendPolicy = info.getMIBreakpointSuspendPolicy();
		int requestId = info.getMIBreakpointRequestID();
		byte eventKind = info.getMIBreakpointEventKind();
		LocationImpl loc = JDWP.bkptsLocation.get(eventNumber);
		long threadID = getThreadId(event);

		packetStream.writeByte(suspendPolicy);
		packetStream.writeInt(1); // Number of events in this response packet
		packetStream.writeByte(eventKind);
		packetStream.writeInt(requestId);
		packetStream.writeLong(threadID); // TODO!! Might need to use PacketStream.writeObjectRef()
		packetStream.writeLocation(loc);
		return packetStream;
	}

	private static long getThreadId(MIBreakpointHitEvent event) {
		long id = 0;
		for (MIResult result: event.getResults()) {
			if ("thread-id".equals(result.getVariable())) {
				MIValue value = result.getMIValue();
				id = Long.parseLong(value.toString());
			}
		}
		return id;
	}

}

