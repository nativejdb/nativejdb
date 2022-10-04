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

import gdb.mi.service.command.events.MIEvent;
import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.output.MIBreakInsertInfo;
import gdb.mi.service.command.output.MIInfo;
import gdb.mi.service.command.output.MIResultRecord;
import jdwp.jdi.ReferenceTypeImpl;
import jdwp.model.MethodLocation;
import jdwp.model.MethodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JDWPEventRequest {

    public static List<MIEvent> asyncEvents = new ArrayList<>();

    static class EventRequest {
        static final int COMMAND_SET = 15;
        private EventRequest() {}  // hide constructor

        /**
         * Set an event request. When the event described by this request
         * occurs, an <a href="#JDWP_Event">event</a> is sent from the
         * target VM. If an event occurs that has not been requested then it is not sent
         * from the target VM. The two exceptions to this are the VM Start Event and
         * the VM Death Event which are automatically generated events - see
         * <a href="#JDWP_Event_Composite">Composite Command</a> for further details.
         */
        static class Set implements Command  {
            static final int COMMAND = 1;

            private boolean differentBreakLine(MIBreakInsertInfo reply) {
                int line = reply.getMIBreakpoint().getLine();
                String originalLocation = reply.getMIBreakpoint().getOriginalLocation();
                String originalLineString = originalLocation.substring(originalLocation.indexOf(":") + 1);
                int originalLine = originalLineString != "" ? Integer.parseInt(originalLineString) : line;
                return line != originalLine;
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                byte eventKind = command.readByte();
                if (eventKind == JDWP.EventKind.BREAKPOINT) {
                    try {
                        processBreakpoint(gc, answer, command, eventKind);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (eventKind == JDWP.EventKind.SINGLE_STEP) {
                    try {
                        byte suspendPolicy = command.readByte();
                        int modifiersCount = command.readInt();
                        for (int i = 0; i < modifiersCount; i++) {
                            byte modKind = command.readByte();
                            if (modKind == JDWP.ModKind.Step) {
                                long threadId = command.readObjectRef();
                                /*
                                    MIN	    0	Step by the minimum possible amount (often a bytecode instruction).
                                    LINE	1	Step to the next source line unless there is no line number information in which case a MIN step is done instead.
                                 */
                                int size = command.readInt();
                                /*
                                    INTO	0	Step into any method calls that occur before the end of the step.
                                    OVER	1	Step over any method calls that occur before the end of the step.
                                    OUT	    2	Step out of the current method.
                                 */
                                int depth = command.readInt(); //TODO use stepdepth for GDB commands

//                                System.out.println("Queueing MI command to select thread:" + threadId);
//                                MICommand cmd = gc.getCommandFactory().createMISelectThread((int) threadId);
//                                int tokenID = JDWP.getNewTokenId();
//                                gc.queueCommand(tokenID, cmd);
//
//                                MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
//                                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
//                                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
//                                    return;
//                                }

                                System.out.println("Queueing MI command to step by step size:" + size);
                                MICommand cmd;
                                if (depth == JDWP.StepDepth.INTO) {
                                    cmd = gc.getCommandFactory().createMIExecStep(size);
                                } else if (depth == JDWP.StepDepth.OUT) {
                                    cmd = gc.getCommandFactory().createMIExecReturn();
                                } else { //JDWP.StepDepth.OVER
                                    cmd = gc.getCommandFactory().createMIExecNext();
                                }

                                int tokenID = JDWP.getNewTokenId();
                                gc.queueCommand(tokenID, cmd);

                                MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                                    return;
                                }

                                reply.setMIInfoRequestID(command.pkt.id);
                                reply.setMIInfoEventKind(eventKind);
                                reply.setMIInfoSuspendPolicy(suspendPolicy);

                                JDWP.stepByThreadID.put(threadId, reply);
                                answer.writeInt(reply.getMIInfoRequestID());
                                //StepOut does not generate an event but returns the updated frame
                                if (depth == JDWP.StepDepth.OUT) {
                                    Translator.translateExecReturn(gc, reply, threadId);
                                }
                                gc.setSteps(true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (eventKind == JDWP.EventKind.CLASS_PREPARE) {
                    byte suspendPolicy = command.readByte();
                    int modifiersCount = command.readInt();
                    for (int i = 0; i < modifiersCount; i++) {
                        byte modKind = command.readByte();
                        if (modKind == 5) {
                            String regex = command.readString().replace(".", "/");
                            System.out.println("In class prepare: " + regex);
                            int requestId = command.pkt.id;
                            answer.writeInt(command.pkt.id);

                            // The IDE is also expecting an async answer for this Class Prepare request.
                            ReferenceTypeImpl refType = ReferenceTypeImpl.refTypeByName.get(regex);
                            if (refType != null) {
                                MIEvent event = new ClassPrepareEvent(0, null, requestId, suspendPolicy, refType);
                                asyncEvents.add(event);
                            }
                        }
                    }
                    answer.writeInt(0);

                } else if (eventKind == JDWP.EventKind.FIELD_ACCESS || eventKind == JDWP.EventKind.FIELD_MODIFICATION) {
                    byte suspendPolicy = command.readByte();
                    int modifiersCount = command.readInt();
                    for (int i = 0; i < modifiersCount; i++) {
                        byte modKind = command.readByte();
                    }
                } else {
                    answer.writeInt(0); // to allow jdwp.jdi GDBControl to initialize
                }
            }

            private void processBreakpoint(GDBControl gc, PacketStream answer, PacketStream command, byte eventKind) {
                byte suspendPolicy = command.readByte();
                int modifiersCount = command.readInt();
                String location = null;
                String tid = "0";
                long index = 0;
                MethodInfo method = null;
                for (int i = 0; i < modifiersCount; i++) {
                    byte modKind = command.readByte();
                    if (modKind == JDWP.ModKind.LocationOnly) {
                        byte typeTag = command.readByte();
                        var classID = command.readObjectRef();
                        var referenceType = gc.getReferenceTypes().findbyId(classID);
                        if (referenceType == null) {
                            answer.setErrorCode((short) JDWP.Error.INVALID_CLASS);
                            return;
                        } else {
                            var methodId = command.readMethodRef();
                            method = referenceType.findMethodById(methodId);
                            if (method == null) {
                                answer.setErrorCode((short) JDWP.Error.INVALID_METHODID);
                                return;
                            } else {
                                index = command.readLong();
                                location = referenceType.getBaseSourceFile() + ':' + index;
                            }
                        }
                    } else if (modKind == JDWP.ModKind.ThreadOnly) {
                        tid = String.valueOf(command.readObjectRef());
                    }
                }
                System.out.println("Queueing MI command to insert breakpoint at " + location);
                MICommand cmd = gc.getCommandFactory().createMIBreakInsert(false, false, "",
                        0, location, tid, false, false);
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

                MIBreakInsertInfo reply = (MIBreakInsertInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                    return;
                }

                if (differentBreakLine(reply)) { // This is an invalid location in the source to set a breakpoint
                    //answer.pkt.errorCode = JDWP.Error.INVALID_LOCATION;

                    // remove the breakpoint in GDB
                    System.out.println("Queueing MI command to disable breakpoint at " + location);
                    cmd = gc.getCommandFactory().createMIBreakDisable(reply.getMIBreakpoint().getNumber());
                    tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);
//
//                                    MIInfo reply1 = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
//                                    if (reply1.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
//                                        answer.pkt.errorCode = JDWP.Error.INTERNAL;
//                                        return;
//                                    }

                    answer.writeInt(command.pkt.id);
                }

                reply.setMIInfoRequestID(command.pkt.id); //TODO maybe another unique ID??
                reply.setMIInfoEventKind(eventKind);
                reply.setMIInfoSuspendPolicy(suspendPolicy);

                Integer bkptNumber = Integer.valueOf(reply.getMIBreakpoint().getNumber());
                JDWP.bkptsByRequestID.put(reply.getMIInfoRequestID(), reply);
                JDWP.bkptsByBreakpointNumber.put(bkptNumber, reply);
                JDWP.bkptsLocation.put(bkptNumber, new MethodLocation(method, (int) index));
                answer.writeInt(reply.getMIInfoRequestID());

            }
        }

        /**
         * Clear an event request. See <a href="#JDWP_EventKind">JDWP.EventKind</a>
         * for a complete list of events that can be cleared. Only the event request matching
         * the specified event kind and requestID is cleared. If there isn't a matching event
         * request the command is a no-op and does not result in an error. Automatically
         * generated events do not have a corresponding event request and may not be cleared
         * using this command.
         */
        static class Clear implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                byte eventKind = command.readByte();
                if (eventKind == JDWP.EventKind.BREAKPOINT) {
                    try {
                        int requestID = command.readInt();
                        MIBreakInsertInfo bkptInfo = JDWP.bkptsByRequestID.get(requestID);

                        System.out.println("Queueing MI command to delete breakpoint");
                        String[] array = {bkptInfo.getMIBreakpoint().getNumber()};
                        MICommand cmd = gc.getCommandFactory().createMIBreakDelete(array);
                        int tokenID = JDWP.getNewTokenId();
                        gc.queueCommand(tokenID, cmd);

                        MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                        if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                            answer.pkt.errorCode = JDWP.Error.INTERNAL;
                            return;
                        }
                        JDWP.bkptsByRequestID.remove(requestID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }  else {
                    answer.writeInt(0); // to allow jdwp.jdi GDBControl to initialize
                }
            }
        }

        /**
         * Removes all set breakpoints, a no-op if there are no breakpoints set.
         */
        static class ClearAllBreakpoints implements Command  {
            static final int COMMAND = 3;


            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                byte eventKind = command.readByte();
                if (eventKind == JDWP.EventKind.BREAKPOINT) {
                    try {
                        String[] array = new String[JDWP.bkptsByBreakpointNumber.size()];
                        int i = 0;
                        for (Map.Entry<Integer, MIBreakInsertInfo> entry : JDWP.bkptsByBreakpointNumber.entrySet()) {
                            array[i] = String.valueOf(entry.getKey());
                            i++;
                        }
                        MICommand cmd = gc.getCommandFactory().createMIBreakDelete(array);
                        int tokenID = JDWP.getNewTokenId();
                        gc.queueCommand(tokenID, cmd);

                        MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                        if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                            answer.pkt.errorCode = JDWP.Error.INTERNAL;
                            return;
                        }
                        JDWP.bkptsByBreakpointNumber.clear();
                        JDWP.bkptsByRequestID.clear();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    JDWP.notImplemented(answer);
                }
            }
        }
    }
}
