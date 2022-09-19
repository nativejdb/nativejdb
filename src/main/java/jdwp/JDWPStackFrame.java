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

import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.output.*;
import jdwp.jdi.*;
import jdwp.model.VariableInfo;

import java.util.ArrayList;

public class JDWPStackFrame {
    static class StackFrame {
        static final int COMMAND_SET = 16;
        private StackFrame() {}  // hide constructor

        /**
         * Returns the value of one or more local variables in a
         * given frame. Each variable must be visible at the frame's code index.
         * Even if local variable information is not available, values can
         * be retrieved if the front-end is able to
         * determine the correct local variable index. (Typically, this
         * index can be determined for method arguments from the method
         * signature without access to the local variable table information.)
         */
        static class GetValues implements Command {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                /*
                    -stack-list-variables [ --no-frame-filters ] [ --skip-unavailable ] print-values
                    Display the names of local variables and function arguments for the selected frame.
                */
                long threadID = command.readObjectRef();
                int frameID = (int) command.readFrameRef();
                int slots = command.readInt();

                var threadFrames = JDWPThreadReference.executeMiStackListFramesInfo(gc, threadID);
                if (threadFrames.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.setErrorCode((short) JDWP.Error.INTERNAL);
                } else {
                    System.out.println("Queueing MI command to list local variables and function arguments");
                    MICommand cmd = gc.getCommandFactory().createMIStackListVariables(true, String.valueOf(threadID), String.valueOf(frameID));
                    int tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);

                    MIStackListVariablesInfo replyloc = (MIStackListVariablesInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                    if (replyloc.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.pkt.errorCode = JDWP.Error.INTERNAL;
                    } else {
                        var framesInfos = JDWPThreadReference.getFrameInfos(gc, threadFrames.getMIFrames());
                        var frameInfo = framesInfos.stream().filter(info -> info.getFrameID() == frameID).findFirst();
                        if (frameInfo.isPresent()) {
                            MIArg[] vals = replyloc.getVariables();

                            int gdbSize = getGDBVariablesSize(vals);
                            if (gdbSize != slots) {
                                System.out.println("GDB number of variables different from VM's. GDB: " + gdbSize + " VM:" + slots);
                            }

                            answer.writeInt(slots);

                            var location = frameInfo.get().getLocation();
                            if (location != null) {
                                for (var i = 0; i < slots; i++) {
                                    var slot = command.readInt();
                                    byte tag = command.readByte();
                                    var variable = location.getMethod().findVariableBySlot(slot);
                                    MIArg gdbVar = variable != null ? containsInGDBVariables(vals, variable.getName()) : null;
                                    String value;
                                    if (gdbVar != null) {
                                        value = gdbVar.getValue();
                                    } else {
                                        value = "<value not found>";
                                        tag = JDWP.Tag.STRING;
                                    }
                                    String name = variable != null ? variable.getName() : "<slot not found>";
                                    if (!name.equals("this") && !value.equals("<optimized out>")) {
                                        answer.writeByte(tag); // get value via GDB print cmd: print *print->value
                                        switch (tag) {
                                            case JDWP.Tag.ARRAY:
                                                answer.writeUntaggedValue(null); //TODO Implement
                                                break;
                                            case JDWP.Tag.BYTE:
                                                String[] split = value.split("\\s+");
                                                answer.writeByte(Byte.parseByte(split[0]));
                                                break;
                                            case JDWP.Tag.CHAR:
                                                answer.writeChar(value.charAt(0));
                                                break;
                                            case JDWP.Tag.FLOAT:
                                                answer.writeFloat(Float.parseFloat(value));
                                                break;
                                            case JDWP.Tag.DOUBLE:
                                                answer.writeDouble(Double.parseDouble(value));
                                                break;
                                            case JDWP.Tag.INT:
                                                answer.writeInt(Integer.parseInt(value));
                                                break;
                                            case JDWP.Tag.LONG:
                                                answer.writeLong(Long.parseLong(value));
                                                break;
                                            case JDWP.Tag.SHORT:
                                                answer.writeShort(Short.parseShort(value));
                                                break;
                                            case JDWP.Tag.BOOLEAN:
                                                answer.writeBoolean(Boolean.parseBoolean(value));
                                                break;
                                            case JDWP.Tag.STRING:
                                                answer.writeString(value);
                                                break;
                                            case JDWP.Tag.OBJECT:
                                                answer.writeNullObjectRef(); //TODO Implement
                                        }
                                    } else if (value.equals("<optimized out>")) {
                                        answer.writeByte(JDWP.Tag.STRING);
                                        answer.writeObjectRef(JDWP.optimizedVarID); // unique ID for optimized string
                                    } else if (variable.equals("$asm")) {

                                        answer.writeByte(JDWP.Tag.STRING);
                                        long newAsmId = JDWP.getNewAsmId();
                                        answer.writeObjectRef(newAsmId);
                                    }
                                }
                            } else {
                                answer.setErrorCode((short) JDWP.Error.ABSENT_INFORMATION);
                            }
                        }
                    }
                }

           }

            private MIArg containsInGDBVariables(MIArg[] vals, String name) {
                for (MIArg val : vals) {
                    if (val.getName().equals(name)) {
                        return val;
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

        /**
         * Sets the value of one or more local variables.
         * Each variable must be visible at the current frame code index.
         * For primitive values, the value's type must match the
         * variable's type exactly. For object values, there must be a
         * widening reference conversion from the value's type to the
         * variable's type and the variable's type must be loaded.
         * <p>
         * Even if local variable information is not available, values can
         * be set, if the front-end is able to
         * determine the correct local variable index. (Typically, this
         * index can be determined for method arguments from the method
         * signature without access to the local variable table information.)
         */
        static class SetValues implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Returns the value of the 'this' reference for this frame.
         * If the frame's method is static or native, the reply
         * will contain the null object reference.
         */
        static class ThisObject implements Command  {
            static final int COMMAND = 3;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                long threadID = command.readObjectRef();
                int frameID = (int) command.readFrameRef();
                //ObjectReferenceImpl objectReference = frame.thisObject();
                //answer.writeTaggedObjectReference(objectReference);

                System.out.println("Queueing MI command to get \"this\" object");
                MICommand cmd = gc.getCommandFactory().createMIStackListVariables(true, String.valueOf(threadID), String.valueOf(frameID)); //Todo: fix frame ID
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

                MIStackListVariablesInfo replyloc = (MIStackListVariablesInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                if (replyloc.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                }

                MIArg[] variables = replyloc.getVariables();
                for (var variable : variables) {
                    String name = variable.getName();
                    if (name.equals("this")) {
                        answer.writeByte(JDWP.Tag.OBJECT);
                        answer.writeObjectRef(Long.decode(variable.getValue()));
                    }
                }
            }
        }

        /**
         * Pop the top-most stack frames of the thread stack, up to, and including 'frame'.
         * The thread must be suspended to perform this command.
         * The top-most stack frames are discarded and the stack frame previous to 'frame'
         * becomes the current frame. The operand stack is restored -- the argument values
         * are added back and if the invoke was not <code>invokestatic</code>,
         * <code>objectref</code> is added back as well. The Java virtual machine
         * program counter is restored to the opcode of the invoke instruction.
         * <p>
         * Since JDWP version 1.4. Requires canPopFrames capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class PopFrames implements Command  {
            static final int COMMAND = 4;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }
    }
}
