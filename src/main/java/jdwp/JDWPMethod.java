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

import com.sun.jdi.AbsentInformationException;
import jdwp.jdi.LocalVariableImpl;
import jdwp.jdi.LocationImpl;
import jdwp.jdi.MethodImpl;
import jdwp.jdi.ReferenceTypeImpl;
import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.output.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDWPMethod {
    static class Method {
        static final int COMMAND_SET = 6;
        private Method() {}  // hide constructor

        /**
         * Returns line number information for the method, if present.
         * The line table maps source line numbers to the initial code index
         * of the line. The line table
         * is ordered by code index (from lowest to highest). The line number
         * information is constant unless a new class definition is installed
         * using <a href="#JDWP_VirtualMachine_RedefineClasses">RedefineClasses</a>.
         */
        static class LineTable implements Command  {
            static final int COMMAND = 1;

            static class LineInfo {

                public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                MethodImpl method = referenceType.methodById(command.readMethodRef());

                if (method.isNative()) {
                    answer.pkt.errorCode = JDWP.Error.NATIVE_METHOD;
                    return;
                }

                List<LocationImpl> locations = Collections.emptyList();
                try {
                    locations = method.allLineLocations();
                } catch (AbsentInformationException ignored) {
                }
                sun.jvm.hotspot.oops.Method ref = method.ref();
                long start = 0;
                long end = ref.getCodeSize();
                if (end == 0) {
                    start = -1;
                }
                answer.writeLong(start);
                answer.writeLong(end);
                answer.writeInt(locations.size());
                for (LocationImpl location : locations) {
                    answer.writeLong(location.codeIndex());
                    answer.writeInt(location.lineNumber());
                }
            }
        }

        /**
         * Returns variable information for the method. The variable table
         * includes arguments and locals declared within the method. For
         * instance methods, the "this" reference is included in the
         * table. Also, synthetic variables may be present.
         */
        static class VariableTable implements Command  {
            static final int COMMAND = 2;

            /**
             * Information about the variable.
             */
            static class SlotInfo {

                public static void write(LocalVariableImpl var, GDBControl gc, PacketStream answer) {
                    answer.writeLong(var.getStart());
                    answer.writeString(var.name());
                    answer.writeString(var.signature());
                    answer.writeInt(var.getLength());
                    answer.writeInt(var.slot());
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                MethodImpl method = referenceType.methodById(command.readMethodRef());
                try {
                    List<LocalVariableImpl> variables = method.variables();
                    answer.writeInt(method.argSlotCount());
                    answer.writeInt(variables.size());
                    for (LocalVariableImpl variable : variables) {
                        SlotInfo.write(variable, gc, answer);
                    }

                } catch (AbsentInformationException e) {
                    answer.pkt.errorCode = JDWP.Error.ABSENT_INFORMATION;
                }
            }
        }

        /**
         * Retrieve the method's bytecodes as defined in
         * <cite>The Java&trade; Virtual Machine Specification</cite>.
         * Requires canGetBytecodes capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class Bytecodes implements Command  {
            static final int COMMAND = 3;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                MethodImpl method = referenceType.methodById(command.readMethodRef());
                byte[] bytecodes = method.bytecodes();
                answer.writeInt(bytecodes.length);
                answer.writeByteArray(bytecodes);
            }
        }

        /**
         * Determine if this method is obsolete. A method is obsolete if it has been replaced
         * by a non-equivalent method using the
         * <a href="#JDWP_VirtualMachine_RedefineClasses">RedefineClasses</a> command.
         * The original and redefined methods are considered equivalent if their bytecodes are
         * the same except for indices into the constant pool and the referenced constants are
         * equal.
         */
        static class IsObsolete implements Command  {
            static final int COMMAND = 4;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                MethodImpl method = referenceType.methodById(command.readMethodRef());
                answer.writeBoolean(method.isObsolete());
            }
        }

        /**
         * Returns variable information for the method, including
         * generic signatures for the variables. The variable table
         * includes arguments and locals declared within the method. For
         * instance methods, the "this" reference is included in the
         * table. Also, synthetic variables may be present.
         * Generic signatures are described in the signature attribute
         * section in
         * <cite>The Java&trade; Virtual Machine Specification</cite>.
         * Since JDWP version 1.5.
         */
        static class VariableTableWithGeneric implements Command  {
            static final int COMMAND = 5;
            private static final String ASM_VAR_NAME = "$asm";
            private static final String ASM_VAR_SIGNATURE = "Ljava/lang/String;";
            private static final String ASM_VAR_GEN_SIGNATURE = null;

            /**
             * Information about the variable.
             */
            static class SlotInfo {

                public static void write(LocalVariableImpl var, GDBControl gc, PacketStream answer) {
                    answer.writeLong(var.getStart());
                    answer.writeString(var.name());
                    answer.writeString(var.signature());
                    answer.writeStringOrEmpty(var.genericSignature());
                    answer.writeInt(var.getLength());
                    answer.writeInt(var.slot());

                    // Add this frame and its associated locals to the map
                    // This frame will be mapped to the current method and this command will not be called again
                    // with next breakpoint hit.
                    MIFrame frame = JDWP.framesById.get(0);
                    Map<Integer, LocalVariableImpl> frameMap = JDWP.localsByFrame.get(frame);
                    frameMap.put(var.slot(), var);
                }
            }


            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                MethodImpl method = referenceType.methodById(command.readMethodRef());
                MIFrame frame = JDWP.framesById.get(0);
                JDWP.localsByFrame.put(frame, new HashMap<>());
                try {
                    List<LocalVariableImpl> variables = method.variables();
                    answer.writeInt(method.argSlotCount());
                    int maxSlot = 0;

                    System.out.println("Queueing MI command to list local variables and function arguments");
                    MICommand cmd = gc.getCommandFactory().createMIStackListVariables(true, String.valueOf(JDWP.currentThreadID), String.valueOf(0)); // TODO: AAV No thread and frame info available here!!!
                    int tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);

                    MIStackListVariablesInfo replyloc = (MIStackListVariablesInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                    if (replyloc.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.pkt.errorCode = JDWP.Error.INTERNAL;
                    }
                    MIArg[] vals = replyloc.getVariables();

                    int gdbSize = getGDBVariablesSize(vals);
                    int vmSize = variables.size();
                    answer.writeInt(gdbSize + 1); //Number of slots from GDB, +1 for assembly variable
                    if (gdbSize != vmSize) {
                        System.out.println("GDB number of variables different from VM's. GDB: " + gdbSize + " VM:" + vmSize);
                    }
                    for (MIArg val: vals) {
                        if (val.getName().equals("this")) {
                            continue;
                        }
                        LocalVariableImpl variable = containsInVMSlots(variables, val.getName());
                        if (variable != null) {
                            if (variable.slot() > maxSlot) {
                                maxSlot = variable.slot();
                            }
                            SlotInfo.write(variable, gc, answer); // TODO Hack
                        } else {
                            LocalVariableImpl gdbVar = new LocalVariableImpl(
                                    method,
                                    maxSlot + 1,
                                    new LocationImpl(method, 0),
                                    new LocationImpl(method, method.ref().getCodeSize() - 1),
                                    val.getName(),
                                    "", // TODO need signature of variable that is not in VM list but in GDB list
                                    ""); // TODO
                            SlotInfo.write(gdbVar, gc, answer);
                        }
                    }

                    // Add assembly variable
                    LocalVariableImpl asmVar = new LocalVariableImpl(
                            method,
                            maxSlot + 1,
                            new LocationImpl(method, 0),
                            new LocationImpl(method, method.ref().getCodeSize() - 1),
                            ASM_VAR_NAME,
                            ASM_VAR_SIGNATURE,
                            ASM_VAR_GEN_SIGNATURE);
                    SlotInfo.write(asmVar, gc, answer);

                } catch (AbsentInformationException e) {
                    answer.pkt.errorCode = JDWP.Error.ABSENT_INFORMATION;
                }
            }

            private LocalVariableImpl containsInVMSlots(List<LocalVariableImpl> variables, String name) {
                for (LocalVariableImpl variable : variables) {
                    if (variable.name().equals(name)) {
                        return variable;
                    }
                }
                return null;
            }

            private int getGDBVariablesSize(MIArg[] vals) {
                int gdbSize = vals.length;
                for (MIArg val : vals) {
                    if (val.getName().equals("this")) {
                        gdbSize--;
                    }
                }
                return gdbSize;
            }
        }
    }
}
