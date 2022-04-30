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

import com.sun.jdi.IncompatibleThreadStateException;
import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.output.*;
import jdwp.jdi.StackFrameImpl;
import jdwp.jdi.ThreadReferenceImpl;
import jdwp.jdi.ValueImpl;

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
                /* This will be a combination of a few different GDB commands:
                1. -stack-list-variables [ --no-frame-filters ] [ --skip-unavailable ] print-values
                    Display the names of local variables and function arguments for the selected frame.
                2. -stack-list-locals [ --no-frame-filters ] [ --skip-unavailable ] print-values  (Maybe contained in 1.'s output already)
                    Display the local variable names for the selected frame.
                3. -symbol-info-variables [--include-nondebug] (Only gives us global field names - no values. Maybe contained in 1.'s output already)
                        [--type type_regexp]
                        [--name name_regexp]
                        [--max-results limit]
                    Return a list containing the names and types for all global variables taken from the debug information. The variables are grouped by source file, and shown with the line number on which each variable is defined.
                */
                //long threadId = command.readObjectRef();
//                long threadId = 1;
//                int frameId = (int) command.readFrameRef();
//                int slots = command.readInt();
//                answer.writeInt(slots);
//                for (int i = 0; i < slots; i++) {
//                    int slot = command.readInt();
//                    byte sigbyte = command.readByte();
//
//                    System.out.println("Queueing MI command to list local variables and function arguments");
//                    MICommand cmd = gc.getCommandFactory().createMIStackListVariables(true, String.valueOf(threadId), String.valueOf(frameId));
//                    int tokenID = JDWP.getNewTokenId();
//                    gc.queueCommand(tokenID, cmd);
//
//                    MIStackListVariablesInfo replyloc = (MIStackListVariablesInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
//                    if (replyloc.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
//                        answer.pkt.errorCode = JDWP.Error.INTERNAL;
//                    }

                    /*System.out.println("Queueing MI command to list global variables (only names)");
                    cmd = gc.getCommandFactory().createMiSymbolInfoVariables();
                    tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);

                    MiSymbolInfoVariablesInfo replyloc1 = (MiSymbolInfoVariablesInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                    if (replyloc1.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.pkt.errorCode = JDWP.Error.INTERNAL;
                    }*/

//                    MIArg[] vals = replyloc.getVariables();
//                    for (int j = 0; j < vals.length; j++) {
//                        String name = vals[j].getName();
//                        String value = vals[j].getValue();
//                        answer.writeString(value);
//                    }

                   /* MIFrame frame = JDWP.framesById.get(frameId);
                    MiSymbolInfoVariablesInfo.SymbolVariableInfo[] files = replyloc1.getSymbolVariables();
                    for ( MiSymbolInfoVariablesInfo.SymbolVariableInfo file: files) {
                        if (file.getFilename() == frame.getFile()) {
                            MiSymbolInfoVariablesInfo.Symbols[] vals1 = file.getSymbols();
                            for (MiSymbolInfoVariablesInfo.Symbols val: vals1) {
                                String name = val.getName();
                                //getValue of this global file field via GDB cmd
                                System.out.println(name);
                            }
                        }
                    }*/
                    // }

//                ThreadReferenceImpl thread = command.readThreadReference();
//                try {
//                    StackFrameImpl frame = thread.frame((int) command.readFrameRef());
//                    int slots = command.readInt();
//                    int available = frame.getAvailableSlots(); //HERE
//                    //answer.writeInt(slots);
//                    answer.writeInt(available);
//                    //for (int i = 0; i < slots; i++) {
//                    for (int i = 0; i < available; i++) {
//                        int slot = command.readInt();
//                        if (slot >= available) {
//                            answer.pkt.errorCode = JDWP.Error.INVALID_SLOT;
//                            return;
//                        }
//                        ValueImpl slotValue = frame.getSlotValue(slot, command.readByte());
//                        answer.writeValue(slotValue);
//                    }
//
//                } catch (IndexOutOfBoundsException e) {
//                    // hack
//                } catch (IncompatibleThreadStateException e) {
//                    e.printStackTrace();
//                }
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
                long threadId = command.readObjectRef();
                long frameId = command.readFrameRef();
                int slots = command.readInt();
                for (int i = 0; i < slots; i++) {
                    int slotId = command.readInt();
                    String value = command.readString();

                    System.out.println("Queueing MI command to select thread:" + threadId);
                    MICommand cmd = gc.getCommandFactory().createMISelectThread((int) threadId);
                    int tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);

                    MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                    if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.pkt.errorCode = JDWP.Error.INTERNAL;
                        return;
                    }

                    System.out.println("Queueing MI command to select frame");
                    cmd = gc.getCommandFactory().createMIStackSelectFrame((int) frameId);
                    tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);

                    reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                    if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.pkt.errorCode = JDWP.Error.INTERNAL;
                    }

                    System.out.println("Queueing MI command to set local variable value");
                    cmd = gc.getCommandFactory().createMIVarAssign("", value);
                    tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);

                    MIStackListVariablesInfo replyloc = (MIStackListVariablesInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                    if (replyloc.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.pkt.errorCode = JDWP.Error.INTERNAL;
                    }
                }
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
                ThreadReferenceImpl thread = command.readThreadReference();
                try {
                    StackFrameImpl frame = thread.frame((int) command.readFrameRef());
                    answer.writeTaggedObjectReference(frame.thisObject()); //HERE
                } catch (IncompatibleThreadStateException e) {
                    e.printStackTrace();
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
