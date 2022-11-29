/*
 * Copyright (C) 2018 JetBrains s.r.o.
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License v2 with Classpath Exception.
 * The text of the license is available in the file LICENSE.TXT.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See LICENSE.TXT for more details.
 *
 * You may contact JetBrains s.r.o. at Na Hřebenech II 1718/10, 140 00 Prague,
 * Czech Republic or at legal@jetbrains.com.
 *
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

package jdwp;

import gdb.mi.service.command.output.MIBreakInsertInfo;
import gdb.mi.service.command.output.MIInfo;
import jdwp.model.MethodLocation;

import java.util.HashMap;
import java.util.Map;


/**
 * Java(tm) Debug Wire Protocol
 */
public class JDWP {

    /**
     * The default JDI request timeout when no preference is set.
     */
    public static final int DEF_REQUEST_TIMEOUT = 300000;
    public static final String THIS_VARIABLE = "this";

    /**
     * Global maps to store breakpoint information for both async (by bkpt#) and sync (by requestID) processing
     */
    static Map<Integer, MIBreakInsertInfo> bkptsByBreakpointNumber = new HashMap<>(); //for async events processing
    static Map<Integer, MethodLocation> bkptsLocation = new HashMap<>(); //for async events processing
    static Map<Integer, MIBreakInsertInfo> bkptsByRequestID = new HashMap<>(); //for sync event requests

    static Map<Long, MIInfo> stepByThreadID = new HashMap<>(); //for async events processing

    static long currentThreadID = 0;
    /**
     * A global counter for all command, the token will be use to identify uniquely a command.
     * Unless the value wraps around which is unlikely.
     */
    static int fTokenIdCounter = 0;

    public static final String JAVA_LANG_STRING_SIGNATURE = "Ljava/lang/String;";

    /*
     Special id for special assembly code pseudo variable. As addresses are word aligned, choosing an odd value will
     ensure there will be no conflict with address values returned by GDB.
     */
    public static final String ASM_VARIABLE_NAME = "$asm";
    public static final String ASM_VARIABLE_SIGNATURE = JAVA_LANG_STRING_SIGNATURE;
    static long ASM_ID = 3;

    // A variable to be used for local variables that are optimized out by gdb
    public static final String OPTIMIZED_OUT = "<optimized out>";
    final static long OPTIMIZED_OUT_ID = ASM_ID + 2;

    public static final String VALUE_NOT_FOUND = "<value not found>";

    final static long VALUE_NOT_FOUND_ID = OPTIMIZED_OUT_ID + 2;

    static int getNewTokenId() {
        int count = ++fTokenIdCounter;
        // If we ever wrap around.
        if (count <= 0) {
            count = fTokenIdCounter = 1;
        }
        return count;
    }

    public static void writeValue(PacketStream answer, byte tag, String value) {
        answer.writeByte(tag); // get value via GDB print cmd: print *print->value
        switch (tag) {
            case Tag.ARRAY:
                answer.writeObjectRef(Translator.decodeAddress(value));
                break;
            case Tag.BYTE:
                String[] split = value.split("\\s+");
                answer.writeByte(Byte.parseByte(split[0]));
                break;
            case Tag.CHAR:
                answer.writeChar(value.charAt(0));
                break;
            case Tag.FLOAT:
                answer.writeFloat(Float.parseFloat(value));
                break;
            case Tag.DOUBLE:
                answer.writeDouble(Double.parseDouble(value));
                break;
            case Tag.INT:
                answer.writeInt(Integer.parseInt(value));
                break;
            case Tag.LONG:
                answer.writeLong(Long.parseLong(value));
                break;
            case Tag.SHORT:
                answer.writeShort(Short.parseShort(value));
                break;
            case Tag.BOOLEAN:
                answer.writeBoolean(Boolean.parseBoolean(value));
                break;
            case Tag.STRING:
            case Tag.OBJECT:
                answer.writeObjectRef(Translator.decodeAddress(value));
                break;
        }
    }

    interface Error {
        int NONE = 0;
        int INVALID_THREAD = 10;
        int INVALID_THREAD_GROUP = 11;
        int INVALID_PRIORITY = 12;
        int THREAD_NOT_SUSPENDED = 13;
        int THREAD_SUSPENDED = 14;
        int THREAD_NOT_ALIVE = 15;
        int INVALID_OBJECT = 20;
        int INVALID_CLASS = 21;
        int CLASS_NOT_PREPARED = 22;
        int INVALID_METHODID = 23;
        int INVALID_LOCATION = 24;
        int INVALID_FIELDID = 25;
        int INVALID_FRAMEID = 30;
        int NO_MORE_FRAMES = 31;
        int OPAQUE_FRAME = 32;
        int NOT_CURRENT_FRAME = 33;
        int TYPE_MISMATCH = 34;
        int INVALID_SLOT = 35;
        int DUPLICATE = 40;
        int NOT_FOUND = 41;
        int INVALID_MODULE = 42;
        int INVALID_MONITOR = 50;
        int NOT_MONITOR_OWNER = 51;
        int INTERRUPT = 52;
        int INVALID_CLASS_FORMAT = 60;
        int CIRCULAR_CLASS_DEFINITION = 61;
        int FAILS_VERIFICATION = 62;
        int ADD_METHOD_NOT_IMPLEMENTED = 63;
        int SCHEMA_CHANGE_NOT_IMPLEMENTED = 64;
        int INVALID_TYPESTATE = 65;
        int HIERARCHY_CHANGE_NOT_IMPLEMENTED = 66;
        int DELETE_METHOD_NOT_IMPLEMENTED = 67;
        int UNSUPPORTED_VERSION = 68;
        int NAMES_DONT_MATCH = 69;
        int CLASS_MODIFIERS_CHANGE_NOT_IMPLEMENTED = 70;
        int METHOD_MODIFIERS_CHANGE_NOT_IMPLEMENTED = 71;
        int NOT_IMPLEMENTED = 99;
        int NULL_POINTER = 100;
        int ABSENT_INFORMATION = 101;
        int INVALID_EVENT_TYPE = 102;
        int ILLEGAL_ARGUMENT = 103;
        int OUT_OF_MEMORY = 110;
        int ACCESS_DENIED = 111;
        int VM_DEAD = 112;
        int INTERNAL = 113;
        int UNATTACHED_THREAD = 115;
        int INVALID_TAG = 500;
        int ALREADY_INVOKING = 502;
        int INVALID_INDEX = 503;
        int INVALID_LENGTH = 504;
        int INVALID_STRING = 506;
        int INVALID_CLASS_LOADER = 507;
        int INVALID_ARRAY = 508;
        int TRANSPORT_LOAD = 509;
        int TRANSPORT_INIT = 510;
        int NATIVE_METHOD = 511;
        int INVALID_COUNT = 512;
    }

    interface EventKind {
        int SINGLE_STEP = 1;
        int BREAKPOINT = 2;
        int FRAME_POP = 3;
        int EXCEPTION = 4;
        int USER_DEFINED = 5;
        int THREAD_START = 6;
        int THREAD_DEATH = 7;
        int THREAD_END = 7;
        int CLASS_PREPARE = 8;
        int CLASS_UNLOAD = 9;
        int CLASS_LOAD = 10;
        int FIELD_ACCESS = 20;
        int FIELD_MODIFICATION = 21;
        int EXCEPTION_CATCH = 30;
        int METHOD_ENTRY = 40;
        int METHOD_EXIT = 41;
        int METHOD_EXIT_WITH_RETURN_VALUE = 42;
        int MONITOR_CONTENDED_ENTER = 43;
        int MONITOR_CONTENDED_ENTERED = 44;
        int MONITOR_WAIT = 45;
        int MONITOR_WAITED = 46;
        int VM_START = 90;
        int VM_INIT = 90;
        int VM_DEATH = 99;
        int VM_DISCONNECTED = 100;
    }

    interface ThreadStatus {
        int ZOMBIE = 0;
        int RUNNING = 1;
        int SLEEPING = 2;
        int MONITOR = 3;
        int WAIT = 4;
    }

    interface SuspendStatus {
        int SUSPEND_STATUS_SUSPENDED = 0x1;
    }

    public interface ClassStatus {
        int VERIFIED = 1;
        int PREPARED = 2;
        int INITIALIZED = 4;
        int ERROR = 8;
    }

    public interface TypeTag {
        byte CLASS = 1;
        byte INTERFACE = 2;
        byte ARRAY = 3;
    }

    public interface Tag {
        byte ARRAY = 91;
        byte BYTE = 66;
        byte CHAR = 67;
        byte OBJECT = 76;
        byte FLOAT = 70;
        byte DOUBLE = 68;
        byte INT = 73;
        byte LONG = 74;
        byte SHORT = 83;
        byte VOID = 86;
        byte BOOLEAN = 90;
        byte STRING = 115;
        byte THREAD = 116;
        byte THREAD_GROUP = 103;
        byte CLASS_LOADER = 108;
        byte CLASS_OBJECT = 99;
    }

    interface StepDepth {
        int INTO = 0;
        int OVER = 1;
        int OUT = 2;
    }

    interface StepSize {
        int MIN = 0;
        int LINE = 1;
    }

    interface SuspendPolicy {
        int NONE = 0;
        int EVENT_THREAD = 1;
        int ALL = 2;
    }

    /**
     * The invoke options are a combination of zero or more of the following bit flags:
     */
    interface InvokeOptions {
        int INVOKE_SINGLE_THREADED = 0x01;
        int INVOKE_NONVIRTUAL = 0x02;
    }

    interface ModKind {
        int Count = 1;
        int Conditional = 2;
        int ThreadOnly = 3;
        int ClassOnly = 4;
        int ClassMatch = 5;
        int ClassExclude = 6;
        int LocationOnly = 7;
        int ExceptionOnly = 8;
        int FieldOnly = 9;
        int Step = 10;
        int InstanceOnly = 11;
        int SourceNameMatch = 12;
    }

    static void notImplemented(PacketStream answer) {
        answer.pkt.errorCode = Error.NOT_IMPLEMENTED;
    }
}