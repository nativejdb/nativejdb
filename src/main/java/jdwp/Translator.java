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
import gdb.mi.service.command.events.MISteppingRangeEvent;
import gdb.mi.service.command.events.MIStoppedEvent;
import gdb.mi.service.command.output.MIBreakInsertInfo;
import gdb.mi.service.command.output.MIResult;
import gdb.mi.service.command.output.MIValue;
import gdb.mi.service.command.output.MIInfo;
import jdwp.jdi.LocationImpl;
import jdwp.jdi.MethodImpl;
import jdwp.jdi.ConcreteMethodImpl;

import java.util.ArrayList;
import java.util.List;

public class Translator {

	public static PacketStream translate(GDBControl gc, MIEvent event) {
		if (event instanceof MIBreakpointHitEvent) {
			return translateBreakpointHit(gc, (MIBreakpointHitEvent) event);
		} else if (event instanceof MISteppingRangeEvent) {
			return translateSteppingRange(gc, (MISteppingRangeEvent) event);
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
		byte suspendPolicy = info.getMIInfoSuspendPolicy();
		int requestId = info.getMIInfoRequestID();
		byte eventKind = info.getMIInfoEventKind();
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

	private static long getThreadId(MIStoppedEvent event) {
		long id = 0;
		for (MIResult result: event.getResults()) {
			if ("thread-id".equals(result.getVariable())) {
				MIValue value = result.getMIValue();
				id = Long.parseLong(value.toString());
			}
		}
		return id;
	}

	private static PacketStream  translateSteppingRange(GDBControl gc, MISteppingRangeEvent event) {
		PacketStream packetStream = new PacketStream(gc);
		Long threadID = getThreadId(event);
		MIInfo info = JDWP.stepByThreadID.get(threadID);
		if (info == null) {
			return null;
		}

		packetStream.writeByte(info.getMIInfoSuspendPolicy());
		packetStream.writeInt(1); // Number of events in this response packet
		packetStream.writeByte(info.getMIInfoEventKind());
		packetStream.writeInt(info.getMIInfoRequestID());
		packetStream.writeLong(threadID); // TODO!! Might need to use PacketStream.writeObjectRef()

		String func = normalizeFunc(event.getFrame().getFunction());
		MethodImpl impl = MethodImpl.methods.get(func);
		if (impl != null) {
			List<LocationImpl> list = ((ConcreteMethodImpl) impl).getBaseLocations().lineMapper.get(event.getFrame().getLine());
			if (list != null && list.size() >= 1) {
				packetStream.writeLocation(list.get(0));
				JDWP.stepByThreadID.remove(threadID);
				return packetStream;
			}
		}
		return packetStream;
	}

	private static  boolean isPrimitive(String type) {
		if (type.equals("byte") || type.equals("short") || type.equals("int") ||
			type.equals("long") || type.equals("float") || type.equals("double")
			|| type.equals("boolean") || type.equals("char")) {
			return true;
		}
		return false;
	}

	public static String normalizeFunc(String func) {
		String start = func.substring(0, func.indexOf("("));
		String paramList = func.substring(func.indexOf("(") + 1, func.indexOf(")"));
		String[] params = paramList.split(", ");
		ArrayList<String> newParams = new ArrayList<>();
		for (String param: params) {
			if (param.indexOf(" ") > 0) {
				param = param.substring(0, param.indexOf(" "));
			}
			if (!isPrimitive(param)) {
				param = "L" + param;
			}
			param = param.replace(".", "/");
			if (param.endsWith("[]")) { // array
				param = param.substring(0, param.indexOf("["));
				param = "[" + param + ";";
			}
			newParams.add(param);
		}
		String newParamList = "";
		for (int i = 0; i < newParams.size(); i++) {
			if (i == 0) {
				newParamList += newParams.get(i);
			} else {
				newParamList += ", " + newParams.get(i);
			}
		}
		return start + "(" + newParamList + ")";
	}
}


