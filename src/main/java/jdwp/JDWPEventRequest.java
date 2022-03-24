package jdwp;

import gdb.mi.service.command.events.MIEvent;
import gdb.mi.service.command.Listener;
import gdb.mi.service.command.MIRunControlEventProcessor;
import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.output.MIBreakInsertInfo;
import gdb.mi.service.command.output.MIInfo;
import gdb.mi.service.command.output.MIResultRecord;
import jdwp.jdi.LocationImpl;
import jdwp.jdi.ReferenceTypeImpl;

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
                        byte suspendPolicy = command.readByte();
                        int modifiersCount = command.readInt();
                        for (int i = 0; i < modifiersCount; i++) {
                            byte modKind = command.readByte();
                            if (modKind == 7) {
                                byte typeTag = command.readByte();
                                ReferenceTypeImpl refType = command.readReferenceType();
                                long methodId = command.readMethodRef();
                                long index = command.readLong();
                                LocationImpl loc = new LocationImpl(refType.methodById(methodId), index);
                                String location = refType.baseSourceName() + ":" + loc.lineNumber();

                                System.out.println("Queueing MI command to insert breakpoint at "+location);
                                MICommand cmd = gc.getCommandFactory().createMIBreakInsert(false, false, "", 0, location, "0", false, false);
                                int tokenID = JDWP.getNewTokenId();
                                gc.queueCommand(tokenID, cmd);

                                MIBreakInsertInfo reply = (MIBreakInsertInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                                    return;
                                }

                                if (differentBreakLine(reply)) { // This is an invalid location in the source to set a breakpoint
                                    answer.pkt.errorCode = JDWP.Error.INVALID_LOCATION;

                                    // remove the breakpoint in GDB
                                    System.out.println("Queueing MI command to disable breakpoint at "+location);
                                    cmd = gc.getCommandFactory().createMIBreakDisable(reply.getMIBreakpoint().getNumber());
                                    tokenID = JDWP.getNewTokenId();
                                    gc.queueCommand(tokenID, cmd);

                                    MIInfo reply1 = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                                    if (reply1.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                                        answer.pkt.errorCode = JDWP.Error.INTERNAL;
                                        return;
                                    }


                                    return;
                                }

                                reply.setMIInfoRequestID(command.pkt.id); //TODO maybe another unique ID??
                                reply.setMIInfoEventKind(eventKind);
                                reply.setMIInfoSuspendPolicy(suspendPolicy);

                                Integer bkptNumber = Integer.valueOf(reply.getMIBreakpoint().getNumber());
                                JDWP.bkptsByRequestID.put(reply.getMIInfoRequestID(), reply);
                                JDWP.bkptsByBreakpointNumber.put(bkptNumber, reply);
                                JDWP.bkptsLocation.put(bkptNumber, loc);
                                answer.writeInt(reply.getMIInfoRequestID());
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (eventKind == JDWP.EventKind.SINGLE_STEP) {
                    try {
                        byte suspendPolicy = command.readByte();
                        int modifiersCount = command.readInt();
                        for (int i = 0; i < modifiersCount; i++) {
                            byte modKind = command.readByte();
                            if (modKind == 10) {
                                long threadId = command.readObjectRef();
                                int size = command.readInt();
                                int depth = command.readInt(); //TODO use stepdepth for GDB commands

                                System.out.println("Queueing MI command to select thread:" + threadId);
                                MICommand cmd = gc.getCommandFactory().createMISelectThread((int) threadId);
                                int tokenID = JDWP.getNewTokenId();
                                gc.queueCommand(tokenID, cmd);

                                MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                                    return;
                                }

                                System.out.println("Queueing MI command to step by step size:" + size);
                                cmd = gc.getCommandFactory().createMIExecNext(size);
                                tokenID = JDWP.getNewTokenId();
                                gc.queueCommand(tokenID, cmd);

                                reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                                    return;
                                }

                                reply.setMIInfoRequestID(command.pkt.id);
                                reply.setMIInfoEventKind(eventKind);
                                reply.setMIInfoSuspendPolicy(suspendPolicy);

                                JDWP.stepByThreadID.put(threadId, reply);
                                answer.writeInt(reply.getMIInfoRequestID());
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
                            String regex = command.readString();
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

                } else {
                    answer.writeInt(0); // to allow jdwp.jdi GDBControl to initialize
                }
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
