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

import gdb.mi.service.command.events.*;
import gdb.mi.service.command.output.MIBreakInsertInfo;
import gdb.mi.service.command.output.MIResult;
import gdb.mi.service.command.output.MIValue;
import gdb.mi.service.command.output.MIInfo;
import jdwp.jdi.LocationImpl;
import jdwp.jdi.MethodImpl;
import jdwp.jdi.ConcreteMethodImpl;
import jdwp.jdi.ThreadReferenceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Translator {

	public static PacketStream getVMStartedPacket(GDBControl gc) {
		PacketStream packetStream = new PacketStream(gc);
		byte suspendPolicy = JDWP.SuspendPolicy.ALL;
		byte eventKind = JDWP.EventKind.VM_START;
		packetStream.writeByte(suspendPolicy);
		packetStream.writeInt(1);
		packetStream.writeByte(eventKind);
		packetStream.writeInt(0); // requestId is 0 since it's automatically generated
		packetStream.writeObjectRef(getMainThreadId(gc)); // Todo ThreadId -- change this!!!!
		return packetStream;
	}


	public static PacketStream translate(GDBControl gc, MIEvent event) {
		if (event instanceof MIBreakpointHitEvent) {
			return translateBreakpointHit(gc, (MIBreakpointHitEvent) event);
		} else if (event instanceof MISteppingRangeEvent) {
			return translateSteppingRange(gc, (MISteppingRangeEvent) event);
		} else if (event instanceof MIInferiorExitEvent) {
			return translateExitEvent(gc, (MIInferiorExitEvent) event);
		} else if (event instanceof ClassPrepareEvent) {
			return translateClassPrepare(gc, (ClassPrepareEvent) event);
		}
		return null;
	}

	private static PacketStream translateClassPrepare(GDBControl gc, ClassPrepareEvent event) {
		PacketStream packetStream = new PacketStream(gc);
		byte eventKind = JDWP.EventKind.CLASS_PREPARE;

		packetStream.writeByte(event.suspendPolicy);
		packetStream.writeInt(1);
		packetStream.writeByte(eventKind);
		packetStream.writeInt(event.requestID);
		packetStream.writeObjectRef(getMainThreadId(gc));
		packetStream.writeByte(event.referenceType.tag());
		packetStream.writeObjectRef(event.referenceType.uniqueID());
		packetStream.writeString(event.referenceType.signature());
		packetStream.writeInt(7);
		return packetStream;
	}

	private static PacketStream translateExitEvent(GDBControl gc, MIInferiorExitEvent event) {
		PacketStream packetStream = new PacketStream(gc);
		byte suspendPolicy = JDWP.SuspendPolicy.NONE;
		byte eventKind = JDWP.EventKind.VM_DEATH;
		packetStream.writeByte(suspendPolicy);
		packetStream.writeInt(1); // Number of events in this response packet
		packetStream.writeByte(eventKind);
		packetStream.writeInt(0);
		return packetStream;
	}

	private static PacketStream translateBreakpointHit(GDBControl gc, MIBreakpointHitEvent event) {
		PacketStream packetStream = new PacketStream(gc);
		Integer eventNumber = Integer.parseInt(event.getNumber());

		if (eventNumber == 1) { // This is the very first breakpoint due the use of start
			gc.initialized();
			return null;
		}

		MIBreakInsertInfo info = JDWP.bkptsByBreakpointNumber.get(eventNumber);
		if (info == null) { // This happens for a synthetic breakpoint (not set by the user)
			return null;
		}
		byte suspendPolicy = info.getMIInfoSuspendPolicy();
		int requestId = info.getMIInfoRequestID();
		byte eventKind = info.getMIInfoEventKind();
		LocationImpl loc = JDWP.bkptsLocation.get(eventNumber);
		long threadID = getThreadId(event);
		System.out.println("THREAD ID FOR HIT: "+ threadID);
		//long threadID = getMainThreadId(gc);

		packetStream.writeByte(suspendPolicy);
		packetStream.writeInt(1); // Number of events in this response packet
		packetStream.writeByte(eventKind);
		packetStream.writeInt(requestId);
		packetStream.writeObjectRef(threadID);
		packetStream.writeLocation(loc);
		return packetStream;
	}

	public static long getMainThreadId(GDBControl gc) {
		return (long) 1;
//		List<ThreadReferenceImpl> list = gc.vm.allThreads();
//		for(ThreadReferenceImpl thread: list){
//			if ("main".equals(thread.name())) {
//				return thread.uniqueID();
//			}
//		}
//		return 0;
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
		System.out.println("Translating end-stepping-range");
		PacketStream packetStream = new PacketStream(gc);
		Long threadID = getThreadId(event);
		//long threadID = getMainThreadId(gc);
		MIInfo info = JDWP.stepByThreadID.get(threadID);
		if (info == null) {
			System.out.println("Returning null");
			return null;
		}

		packetStream.writeByte(info.getMIInfoSuspendPolicy());
		packetStream.writeInt(1); // Number of events in this response packet
		packetStream.writeByte(info.getMIInfoEventKind());
		packetStream.writeInt(info.getMIInfoRequestID());
		packetStream.writeObjectRef(getMainThreadId(gc));
		LocationImpl loc = locationLookup(event.getFrame().getFunction(), event.getFrame().getLine());
		if (loc != null) {
			packetStream.writeLocation(loc);
			JDWP.stepByThreadID.remove(threadID);
			return packetStream;

		}
		return packetStream;
	}

	private static  boolean isPrimitive(String type) {
		if (type.equals("byte") || type.equals("short") || type.equals("int") ||
			type.equals("long") || type.equals("float") || type.equals("double")
			|| type.equals("boolean") || type.equals("char") || type.equals("void")) {
			return true;
		}
		return false;
	}

	public static String normalizeFunc(String func) {
		if (func.indexOf("(") == -1) { // Function does not contain parameter types
			return func;
		}
		String start = func.substring(0, func.indexOf("(")).replace(".", "/");
		String paramList = func.substring(func.indexOf("(") + 1, func.indexOf(")"));
		String[] params = paramList.split(", ");
		ArrayList<String> newParams = new ArrayList<>();
		for (String param: params) {
			if (param.endsWith(" *")) { // zero or more param
				param = param.substring(0, param.indexOf(" ")) + ";";
			}
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
			if (!param.equals("void")) {
				newParams.add(param);
			}
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

	public static LocationImpl locationLookup(String func, int line) {
		String name = normalizeFunc(func);
		MethodImpl impl = MethodImpl.methods.get(name);
		if (impl != null) {
			List<LocationImpl> list = ((ConcreteMethodImpl) impl).getBaseLocations().lineMapper.get(line);
			if (list != null && list.size() >= 1) {
				return list.get(0);
			}
			return null;
		}
		if (!name.contains("(")) {
			Set<String> keys = MethodImpl.methods.keySet();
			for (String key: keys) {
				if (key.contains(name)) {
					ConcreteMethodImpl impl1 = (ConcreteMethodImpl) MethodImpl.methods.get(key);
					List<LocationImpl> list = ((ConcreteMethodImpl) impl1).getBaseLocations().lineMapper.get(line);
					if (list != null && list.size() >= 1) {
						return list.get(0);
					}
				}
			}
		}
		return null;
	}


}


