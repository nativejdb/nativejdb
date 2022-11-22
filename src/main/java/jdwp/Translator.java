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
import gdb.mi.service.command.output.*;
import gdb.mi.service.command.output.MiSymbolInfoFunctionsInfo.SymbolFileInfo;
import gdb.mi.service.command.output.MiSymbolInfoFunctionsInfo.Symbols;
import jdwp.jdi.LocationImpl;
import jdwp.jdi.MethodImpl;
import jdwp.jdi.ConcreteMethodImpl;
import jdwp.model.*;

import javax.lang.model.SourceVersion;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {

	static final String JAVA_BOOLEAN = "boolean";
	static final String JAVA_BYTE = "byte";
	static final String JAVA_CHAR = "char";
	static final String JAVA_SHORT = "short";
	static final String JAVA_INT = "int";
	static final String JAVA_LONG = "long";
	static final String JAVA_FLOAT = "float";
	static final String JAVA_DOUBLE = "double";
	static final String JAVA_VOID = "void";

	public static final Map<String, String> typeSignature;	// primitive type signature mapping from C/C++ to JNI
	static {
		typeSignature = new HashMap<>();

		typeSignature.put(JAVA_BOOLEAN, "Z");
		typeSignature.put(JAVA_BYTE, "B");
		typeSignature.put(JAVA_CHAR, "C");
		typeSignature.put(JAVA_SHORT, "S");
		typeSignature.put(JAVA_INT, "I");
		typeSignature.put(JAVA_LONG, "J");
		typeSignature.put(JAVA_FLOAT, "F");
		typeSignature.put(JAVA_DOUBLE, "D");
		typeSignature.put(JAVA_VOID, "V");
	}

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
		MethodLocation loc = JDWP.bkptsLocation.get(eventNumber);
		long threadID = getThreadId(event);
		System.out.println("THREAD ID FOR HIT: "+ threadID);
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
		PacketStream packetStream = null;
		var location = getMethodLocationFromFuncAndLine(gc, event.getFrame().getFunction(),
				event.getFrame().getLine());
		if (location.isPresent()) {
			var threadID = getThreadId(event);
			MIInfo info = JDWP.stepByThreadID.get(threadID);
			if (info != null) {
				packetStream = new PacketStream(gc);
				packetStream.writeByte(info.getMIInfoSuspendPolicy());
				packetStream.writeInt(1); // Number of events in this response packet
				packetStream.writeByte(info.getMIInfoEventKind());
				packetStream.writeInt(info.getMIInfoRequestID());
				packetStream.writeObjectRef(getMainThreadId(gc));
				if (location != null) {
					packetStream.writeLocation(location.get());
					JDWP.stepByThreadID.remove(threadID);
				}
			}
		}
		return packetStream;
	}

	public static void translateExecReturn(GDBControl gc, MIInfo info, long threadID) {
		var event = MIStoppedEvent.parse(info.getMIInfoRequestID(),
				info.getMIOutput().getMIResultRecord().getMIResults());
		if (event.getFrame() != null) {
			var location = getMethodLocationFromFuncAndLine(gc, event.getFrame().getFunction(),
					event.getFrame().getLine());
			if (location.isPresent()) {
					var packetStream = new PacketStream(gc);
					packetStream.writeByte(info.getMIInfoSuspendPolicy());
					packetStream.writeInt(1); // Number of events in this response packet
					packetStream.writeByte(info.getMIInfoEventKind());
					packetStream.writeInt(info.getMIInfoRequestID());
					packetStream.writeObjectRef(threadID);
					packetStream.writeLocation(location.get());
					packetStream.send();
				}
			}
		}

	private static  boolean isPrimitive(String type) {
		return typeSignature.containsKey(type);
	}

	public static MethodInfo createMethodInfoFromGDB(ReferenceType referenceType, String name, String type) {
		String[] functionNames = getClassAndMethodName(name);
		var signature = getSignature(type, functionNames[0], functionNames[1]);
		return new MethodInfo(referenceType, signature);
	}

	public static Optional<MethodLocation> getMethodLocationFromFuncAndLine(GDBControl gc, String func, int line) {
		var names = getClassAndMethodName(func);
		var referenceType = gc.getReferenceTypes().findByClassName(ClassName.fromGDB(names[0]));
		if (referenceType != null) {
			return referenceType.findByNameAndLocation(names[1], line);
		}
		return Optional.empty();
	}

	public static String normalizeType(String type) {
		if (type.startsWith("class ")) {
			type = type.substring(6);
		} else if (type.startsWith("union ")) {
			type = type.substring(6);
		}
		if (type.charAt(type.length() - 1) == '*') {
			type = type.substring(0, type.length() - 1);
		}
		return type.trim();
	}

	/**
	 * Normalize a type information returned by GDB to the JNI signature. The type field has the following structure:
	 * <code>return_type (parm1_type,...)</code>
	 * where return type can be <code>void, int or union interface_name * or class class_name *</code>
	 *
	 * @param type the type information from GDB
	 * @return the JNI signature
	 */
	public static MethodSignature getSignature(String type, String className, String methodName) {
		String returnType;
		var parameterTypes = new ArrayList<String>();
		boolean instanceMethod = false;
		int index = type.indexOf('(');
		if (index == (-1)) {
			returnType = normalizeType(type);
		} else {
			returnType = normalizeType(type.substring(0, type.indexOf("(")));
			String paramList = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
			String[] params = paramList.split(", ");
			for (int i=0; i < params.length;++i) {
				if (!params[i].equals("void")) {
					String paramType = normalizeType(params[i]);
					if (i !=0 || !paramType.equals(className)) {
						parameterTypes.add(paramType);
					} else {
						instanceMethod = true;
					}
				}
			}
		}
		return new MethodSignature(methodName, returnType, parameterTypes, instanceMethod);
	}

	/**
	 * Return the function name from the name field. The returned array is a 2 element array whose first value is the
	 * class name , the second value is the function (method) name.
	 * If no class is found then the first element is null.
	 *
	 * @param name the GDB name field (ie <code>java.util.List::of</code>)
	 * @return a 3 element array
	 */
	public static String[] getClassAndMethodName(String name) {
		String[] names = new String[2];

		int index = name.indexOf("::");
		if (index != (-1)) {
			names[0] = name.substring(0, index);
			name = name.substring(index + 2);

		}
		index = name.indexOf('(');
		if (index != (-1)) {
			names[1] = name.substring(0, index);
		} else {
			names[1] = name;
		}
		return names;
	}

	public static String gdb2JNI(String param) {
		if (param.endsWith("*")) { // zero or more param
			param = param.substring(0, param.indexOf("*"));
		}
		param = param.replace(" ", "");
		param = param.replace(".", "/");
		String prefix = "";
		while (param.endsWith("[]")) {
			param = param.substring(0, param.length() - 2);
			prefix += "[";
		}
		if (!isPrimitive(param)) {
			param = "L" + param + ";";
		} else {
			// If is primitive, provide JNI signature
			param = getPrimitiveJNI(param);
		}
		return prefix + param;
	}

	public static String JNI2gdb(String JNI) {
		var prefix = "";
		var suffix = "";
		var i=0;
		for(; i < JNI.length();++i) {
			if (JNI.charAt(i) == '[') {
				suffix += "[]";
			} else {
				break;
			}
		}
		if (JNI.charAt(JNI.length() - 1) == ';') {
			prefix = JNI.substring(i + 1, JNI.length() - 1).replace('/', '.');
		} else {
			prefix = JNIConstants.fromTag(JNI.charAt(i));
		}
		return prefix + suffix;
	}

	public static String getPrimitiveJNI(String param) {
		return typeSignature.get(param);
	}

	public static LocationImpl locationLookup(String func, int line) {
		String name = func; //TODO
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

	/**
	 * May or may not be helpful since we may be able to get this info from
	 * MISymbolInfoFunctionsInfo.SymbolFileInfo.getFile()
	 *
	 * Determines the file and function name for Qbicc applications.
	 * Filename is needed for break-insert commands.
	 *
	 * Example:
	 * Input: "_JHelloNested_HelloNested_main__3Ljava_lang_String_2_V"
	 * Output: "HelloNested/HelloNested.java"
	 */
	public static String getQbiccFilename(String name) {

		// 1. Find first instance of "__" and keep only the beginning
		// "_JHelloNested_HelloNested_main__3Ljava_lang_String_2_V" --> "_JHelloNested_HelloNested_main"
		String fileFuncName = name.substring(0, name.indexOf("__"));

		// 2. Parse out "_J" in beginning
		// "_JHelloNested_HelloNested_main" --> "HelloNested_HelloNested_main"
		fileFuncName = fileFuncName.substring(2);

		// 3. Determines if filename contains object (identifiable from numeric id) name subclasses.
		// 	  If that's the case, get rid of everything from numeric values.
		// With no numeric: "HelloNested_HelloNested_main" --> "HelloNested_HelloNested"
		// With numeric:    "HelloNested_HelloNested_00024Greeter_greeter" --> "HelloNested_HelloNested"
		fileFuncName = getFileNamesOnly(fileFuncName);

		// 4. Build filename string by splitting "_" 's
		// "HelloNested_HelloNested" --> "HelloNested/HelloNested/"
		String[] files = fileFuncName.split("_");
		StringBuilder javaFilename = new StringBuilder();

		for (String file : files) {
			javaFilename.append(file);
			javaFilename.append("/");
		}

		// 5. Replace final "/" with ".java"
		// "HelloNested/HelloNested/" --> "HelloNested.HelloNested.java"
		javaFilename.setLength(javaFilename.length() - 1);
		javaFilename.append(".java");

		return javaFilename.toString();
	}

	private static String getFileNamesOnly(String name) {
		// Find the numeric value
		Matcher matcher = Pattern.compile("\\d+").matcher(name);
		if (matcher.find()) {
			String number = matcher.group();
			return name.substring(0, name.indexOf(number) - 1);
		}

		return name.substring(0, name.lastIndexOf("_"));
	}

	public static void translateReferenceTypes(ReferenceTypes referenceTypes, MiSymbolInfoFunctionsInfo response) {
		Map<String, ReferenceType> types = new HashMap<>();
		for(SymbolFileInfo symbolFile : response.getSymbolFiles()) {
			for(Symbols symbol : symbolFile.getSymbols()) {
				var index = symbol.getName().indexOf("::");
				var className = symbol.getName().substring(0, index);
				if (isJavaClassName(className)) {
					var refType = types.computeIfAbsent(className,
							key -> new ReferenceType(referenceTypes, symbolFile.getFilename(),
									ClassName.fromGDB(className)));
					createMethodInfoFromGDB(refType, symbol.getName(), symbol.getType());
				}
			}
		}
	}

	private static boolean isJavaClassName(String className) {
		String[] members = className.split("\\.");
		for(String member : members) {
			if (!SourceVersion.isIdentifier(member)) {
				return false;
			}
		}
		return true;
	}

	public static byte arrayClassName2Tag(ClassName className) {
		var elementClassName = ClassName.fromJNI(className.getJNI().substring(1));
		var jni = elementClassName.getJNI();
		var type = jni.charAt(0);
		if (type == JDWP.Tag.OBJECT) {
			if (String.class.getName().equals(elementClassName.getPrintable())) {
				return JDWP.Tag.STRING;
			}
		}
		return (byte) type;
	}

	public static long decodeAddress(String address) {
		long result = 0L;
		for(int index=2; index < address.length();++index) {
			result *= 16;
			result += Character.digit(address.charAt(index), 16);
		}
		return result;
	}
}


