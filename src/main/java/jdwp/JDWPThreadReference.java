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
import jdwp.jdi.*;

import java.util.List;
import java.util.ArrayList;

public class JDWPThreadReference {

    static class ThreadReference {
        static final int COMMAND_SET = 11;
        private ThreadReference() {}  // hide constructor

        /**
         * Returns the thread name.
         */
        static class Name implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                //answer.writeString(command.readThreadReference().name());

                System.out.println("Queueing MI command to get thread name");
                MICommand cmd = gc.getCommandFactory().createMIThreadInfo();
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

                MIThreadInfoInfo reply = (MIThreadInfoInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.pkt.errorCode = JDWP.Error.VM_DEAD;
                    return;
                }

                long threadId = command.readObjectRef();
                JDWP.currentThreadID = threadId; //TODO AAV hack!
                System.out.println("Thread id is:" + threadId);
                MIThread[] allThreads = reply.getThreadList();
                for(MIThread thread: allThreads){
                    long id = Long.parseLong(thread.getThreadId());
                    System.out.println("id is:" + id);
                    if (id == threadId) {
                        System.out.println("Writing thread name:" + thread.getName());
                        answer.writeString(thread.getName());
                    }
                }
            }
        }

        /**
         * Suspends the thread.
         * <p>
         * Unlike java.lang.Thread.suspend(), suspends of both
         * the virtual machine and individual threads are counted. Before
         * a thread will run again, it must be resumed the same number
         * of times it has been suspended.
         * <p>
         * Suspending single threads with command has the same
         * dangers java.lang.Thread.suspend(). If the suspended
         * thread holds a monitor needed by another running thread,
         * deadlock is possible in the target VM (at least until the
         * suspended thread is resumed again).
         * <p>
         * The suspended thread is guaranteed to remain suspended until
         * resumed through one of the JDI resume methods mentioned above;
         * the application in the target VM cannot resume the suspended thread
         * through {@link Thread#resume}.
         * <p>
         * Note that this doesn't change the status of the thread (see the
         * <a href="#JDWP_ThreadReference_Status">ThreadStatus</a> command.)
         * For example, if it was
         * Running, it will still appear running to other threads.
         */
        static class Suspend implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                System.out.println("Thread - Queueing MI command to suspend application");
                MICommand cmd = gc.getCommandFactory().createMIExecInterrupt(true);
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

//                MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
//                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
//                    answer.pkt.errorCode = JDWP.Error.VM_DEAD; // The virtual machine is not running.
//                }
            }
        }

        /**
         * Resumes the execution of a given thread. If this thread was
         * not previously suspended by the front-end,
         * calling this command has no effect.
         * Otherwise, the count of pending suspends on this thread is
         * decremented. If it is decremented to 0, the thread will
         * continue to execute.
         */
        static class Resume implements Command  {
            static final int COMMAND = 3;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
               // try {
                    System.out.println("Thread - Queueing MI command to resume application " +  command.readThreadReference().uniqueID());
                    MICommand cmd = gc.getCommandFactory().createMIExecContinue(true);
                    int tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);

//                    MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
//                    if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
//                        answer.pkt.errorCode = JDWP.Error.VM_DEAD; // The virtual machine is not running.
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        }

        /**
         * Returns the current status of a thread. The thread status
         * reply indicates the thread status the last time it was running.
         * the suspend status provides information on the thread's
         * suspension, if any.
         */
        static class Status implements Command  {
            static final int COMMAND = 4;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                answer.writeInt(2);
                answer.writeInt(1);
//                ThreadReferenceImpl thread = command.readThreadReference();
//                answer.writeInt(thread.status());
//                answer.writeInt(thread.suspendCount());
//                System.out.println("In Thread STATUS: " + thread.status() + " - suspendCount: " + thread.suspendCount());
            }
        }

        /**
         * Returns the thread group that contains a given thread.
         */
        static class ThreadGroup implements Command  {
            static final int COMMAND = 5;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
//                String threadId = command.readObjectRef() + "";
//                System.out.println("Queueing MI command to get thread groups");
//                MICommand cmd = gc.getCommandFactory().createMIMIListThreadGroups();
//                int tokenID = JDWP.getNewTokenId();
//                gc.queueCommand(tokenID, cmd);
//
//                MIListThreadGroupsInfo reply = (MIListThreadGroupsInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
//                if (reply.getMIOutput().getMIResultRecord().getResultClass() == MIResultRecord.ERROR) {
//                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
//                }
//                MIListThreadGroupsInfo.IThreadGroupInfo[] groupList = reply.getGroupList();
//                long id = 0;
//                for (MIListThreadGroupsInfo.IThreadGroupInfo group: groupList) {
//                   MIListThreadGroupsInfo.ThreadGroupInfo groupInfo = (MIListThreadGroupsInfo.ThreadGroupInfo) group;
//                   if (groupInfo != null) {
//                       MIThread[] threads = groupInfo.getThreads();
//                       if (threads != null) {
//                           for (MIThread thread : threads) {
//                               if (thread.getThreadId().equals(threadId)) {
//                                   id = JDWPThreadGroupReference.threadGroupByName.get(group.getName());
//                               }
//                           }
//                       }
//                   }
//
//                }
//                System.out.println("Writing group id: " + id);
//                answer.writeObjectRef(id);

                answer.writeObjectRef(JDWPThreadGroupReference.threadGroupId);

//                ThreadReferenceImpl thread = command.readThreadReference();
//                answer.writeObjectRef(thread.threadGroup().uniqueID());

            }
        }

        /**
         * Returns the current call stack of a suspended thread.
         * The sequence of frames starts with
         * the currently executing frame, followed by its caller,
         * and so on. The thread must be suspended, and the returned
         * frameID is valid only while the thread is suspended.
         */
        static class Frames implements Command  {
            static final int COMMAND = 6;

            static class Frame {

                public static void write(StackFrameImpl frame, GDBControl gc, PacketStream answer) {
                    answer.writeFrameRef(frame.id());
                    answer.writeLocation(frame.location());
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                String threadId = command.readObjectRef() + "";

                System.out.println("Queueing MI command to get frames");
                //MICommand cmd = gc.getCommandFactory().createMIStackListFrames(String.valueOf(threadId));
                MICommand cmd = gc.getCommandFactory().createMIStackListFrames(threadId);
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

                MIStackListFramesInfo reply = (MIStackListFramesInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                }

                MIFrame[] frames = reply.getMIFrames();
                int framesLength = 0;
                List<Integer> frameIds = new ArrayList<>();
                List<LocationImpl> locations = new ArrayList<>();

                for (MIFrame frame: frames) {
                    int frameId = frame.getLevel();
                    JDWP.framesById.put(frameId, frame);

                    LocationImpl loc = Translator.locationLookup(frame.getFunction(), frame.getLine());
                    if (loc != null) {
                        framesLength++;
                        locations.add(loc);
                        frameIds.add(frameId);
                    }
                }
                answer.writeInt(framesLength);
                for (int i = 0; i < framesLength; i++) {
                    answer.writeFrameRef(frameIds.get(i));
                    answer.writeLocation(locations.get(i));
                }
            }
        }

        /**
         * Returns the count of frames on this thread's stack.
         * The thread must be suspended, and the returned
         * count is valid only while the thread is suspended.
         * Returns JDWP.Error.errorThreadNotSuspended if not suspended.
         */
        static class FrameCount implements Command  {
            static final int COMMAND = 7;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                String threadId = command.readObjectRef() + "";

                System.out.println("Queueing MI command to get frames");
                //MICommand cmd = gc.getCommandFactory().createMIStackListFrames(String.valueOf(threadId));
                MICommand cmd = gc.getCommandFactory().createMIStackListFrames(threadId);
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

                MIStackListFramesInfo reply = (MIStackListFramesInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                }

                MIFrame[] frames = reply.getMIFrames();
                int framesLength = 0;

                for (MIFrame frame: frames) {
                    LocationImpl loc = Translator.locationLookup(frame.getFunction(), frame.getLine());
                    if (loc != null) {
                        framesLength++;
                    }
                }
                answer.writeInt(framesLength);
            }
        }

        /**
         * Returns the objects whose monitors have been entered by this thread.
         * The thread must be suspended, and the returned information is
         * relevant only while the thread is suspended.
         * Requires canGetOwnedMonitorInfo capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class OwnedMonitors implements Command  {
            static final int COMMAND = 8;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ThreadReferenceImpl thread = command.readThreadReference();
                List<ObjectReferenceImpl> ownedMonitors;
                try {
                    ownedMonitors = thread.ownedMonitors();
                } catch (IncompatibleThreadStateException e) {
                    answer.pkt.errorCode = JDWP.Error.INVALID_THREAD;
                    return;
                }
                answer.writeInt(ownedMonitors.size());
                for (ObjectReferenceImpl ownedMonitor : ownedMonitors) {
                    answer.writeTaggedObjectReference(ownedMonitor);
                }

            }
        }

        /**
         * Returns the object, if any, for which this thread is waiting. The
         * thread may be waiting to enter a monitor, or it may be waiting, via
         * the java.lang.Object.wait method, for another thread to invoke the
         * notify method.
         * The thread must be suspended, and the returned information is
         * relevant only while the thread is suspended.
         * Requires canGetCurrentContendedMonitor capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class CurrentContendedMonitor implements Command  {
            static final int COMMAND = 9;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ThreadReferenceImpl thread = command.readThreadReference();
                try {
                    answer.writeTaggedObjectReference(thread.currentContendedMonitor());
                } catch (IncompatibleThreadStateException e) {
                    answer.pkt.errorCode = JDWP.Error.INVALID_THREAD;
                }
            }
        }

        /**
         * Stops the thread with an asynchronous exception, as if done by
         * java.lang.Thread.stop
         */
        static class Stop implements Command  {
            static final int COMMAND = 10;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Interrupt the thread, as if done by java.lang.Thread.interrupt
         */
        static class Interrupt implements Command  {
            static final int COMMAND = 11;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Get the suspend count for this thread. The suspend count is the
         * number of times the thread has been suspended through the
         * thread-level or VM-level suspend commands without a corresponding resume
         */
        static class SuspendCount implements Command  {
            static final int COMMAND = 12;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                answer.writeInt(1);
            }
        }

        /**
         * Returns monitor objects owned by the thread, along with stack depth at which
         * the monitor was acquired. Returns stack depth of -1  if
         * the implementation cannot determine the stack depth
         * (e.g., for monitors acquired by JNI MonitorEnter).
         * The thread must be suspended, and the returned information is
         * relevant only while the thread is suspended.
         * Requires canGetMonitorFrameInfo capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         * <p>Since JDWP version 1.6.
         */
        static class OwnedMonitorsStackDepthInfo implements Command  {
            static final int COMMAND = 13;

            static class monitor {

                public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ThreadReferenceImpl thread = command.readThreadReference();
                List<MonitorInfoImpl> list;
                try {
                    list = thread.ownedMonitorsAndFrames();
                } catch (IncompatibleThreadStateException e) {
                    answer.pkt.errorCode = JDWP.Error.INVALID_THREAD;
                    return;
                }
                answer.writeInt(list.size());
                for (MonitorInfoImpl o : list) {
                    answer.writeTaggedObjectReference(o.monitor());
                    answer.writeInt(o.stackDepth());
                }
            }
        }

        /**
         * Force a method to return before it reaches a return
         * statement.
         * <p>
         * The method which will return early is referred to as the
         * called method. The called method is the current method (as
         * defined by the Frames section in
         * <cite>The Java&trade; Virtual Machine Specification</cite>)
         * for the specified thread at the time this command
         * is received.
         * <p>
         * The specified thread must be suspended.
         * The return occurs when execution of Java programming
         * language code is resumed on this thread. Between sending this
         * command and resumption of thread execution, the
         * state of the stack is undefined.
         * <p>
         * No further instructions are executed in the called
         * method. Specifically, finally blocks are not executed. Note:
         * this can cause inconsistent states in the application.
         * <p>
         * A lock acquired by calling the called method (if it is a
         * synchronized method) and locks acquired by entering
         * synchronized blocks within the called method are
         * released. Note: this does not apply to JNI locks or
         * java.util.concurrent.locks locks.
         * <p>
         * Events, such as MethodExit, are generated as they would be in
         * a normal return.
         * <p>
         * The called method must be a non-native Java programming
         * language method. Forcing return on a thread with only one
         * frame on the stack causes the thread to exit when resumed.
         * <p>
         * For void methods, the value must be a void value.
         * For methods that return primitive values, the value's type must
         * match the return type exactly.  For object values, there must be a
         * widening reference conversion from the value's type to the
         * return type type and the return type must be loaded.
         * <p>
         * Since JDWP version 1.6. Requires canForceEarlyReturn capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class ForceEarlyReturn implements Command  {
            static final int COMMAND = 14;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }
    }
}
