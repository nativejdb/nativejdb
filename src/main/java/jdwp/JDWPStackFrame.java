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

import java.util.Map;

public class JDWPStackFrame {

    /**
     * Obtains the frame that has the same function name as the target frame.
     */
    private static Map<Integer, LocalVariableImpl> getFrame(MIFrame targetFrame) {

        for (Map.Entry<MIFrame, Map<Integer, LocalVariableImpl>> entry : JDWP.localsByFrame.entrySet()) {
            String targetFunc = targetFrame.getFunction();
            MIFrame currFrame = entry.getKey();
            if (targetFunc.equals(currFrame.getFunction())) {
                return entry.getValue();
            }
        }
        return null;
    }

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

                System.out.println("Queueing MI command to list local variables and function arguments");
                MICommand cmd = gc.getCommandFactory().createMIStackListVariables(true, String.valueOf(threadID), String.valueOf(frameID));
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

                MIStackListVariablesInfo replyloc = (MIStackListVariablesInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                if (replyloc.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                }
                MIArg[] vals = replyloc.getVariables();
                MIFrame topFrame = JDWP.framesById.get(0);      // the top-level frame is the current method on the call stack

                int gdbSize = getGDBVariablesSize(vals);
                answer.writeInt(slots);
                if (gdbSize != slots) {
                    System.out.println("GDB number of variables different from VM's. GDB: " + gdbSize + " VM:" + slots);
                }
                for (int i = 0; i < slots; i++) {
                    int slot = command.readInt();
                    byte tag = command.readByte();
                    LocalVariableImpl vmVar = getFrame(topFrame).get(slot);
                    MIArg gdbVar = containsInGDBVariables(vals, vmVar.name());

                    if (gdbVar != null) {
                        String value = gdbVar.getValue();
                        if (!gdbVar.getName().equals("this") && !value.equals("<optimized out>")) {
                            answer.writeByte(tag); // get value via GDB print cmd: print *print->value
                            switch (tag) {
                                case JDWP.Tag.ARRAY:
                                    answer.writeUntaggedValue(null); //TODO Implement
                                    break;
                                case JDWP.Tag.BYTE:
                                    // GDB returns "127 \b"
                                    String[] splited = value.split("\\s+");
                                    answer.writeByte(Byte.parseByte(splited[0]));
                                    break;
                                case JDWP.Tag.CHAR:
                                    // GDB returns "97"
                                    char charValue = (char) Integer.parseInt(value);
                                    answer.writeChar(charValue);
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
                                    break;
                            }
                        } else if (value.equals("<optimized out>")) {
                            answer.writeByte(JDWP.Tag.STRING);
                            answer.writeObjectRef(JDWP.optimizedVarID); // unique ID for optimized string
                        }
                    } else if (vmVar.name().equals("$asm")) {
                        answer.writeByte(JDWP.Tag.STRING);
                        long newAsmId = JDWP.getNewAsmId();
                        answer.writeObjectRef(newAsmId);
                    }
                }
                // TODO write GDB variables that are not in the VM slots
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

                MIArg[] vals = replyloc.getVariables();
                for (int j = 0; j < vals.length; j++) {
                    String name = vals[j].getName();
                    if (name.equals("this")) {
                        System.out.println("Queueing MI command to print \"this\" object");
                        cmd = gc.getCommandFactory().createMIPrint("*this");
                        tokenID = JDWP.getNewTokenId();
                        gc.queueCommand(tokenID, cmd);

                        MIInfo replyprint = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                        if (replyprint.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                            answer.pkt.errorCode = JDWP.Error.INTERNAL;
                        }

                        String val = replyprint.getMIOutput().getMIOOBRecords()[1].toString();
                        answer.writeByte(JDWP.Tag.OBJECT);
                        answer.writeNullObjectRef(); // TODO Need to retrieve object at address
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
