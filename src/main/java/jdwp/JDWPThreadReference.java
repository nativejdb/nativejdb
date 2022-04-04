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
                answer.writeString(command.readThreadReference().name());
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
                JDWP.notImplemented(answer);
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
                JDWP.notImplemented(answer);
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
                ThreadReferenceImpl thread = command.readThreadReference();
                answer.writeInt(thread.status());
                answer.writeInt(thread.suspendCount());
            }
        }

        /**
         * Returns the thread group that contains a given thread.
         */
        static class ThreadGroup implements Command  {
            static final int COMMAND = 5;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
//                int threadId = (int) command.readObjectRef();
//
//                System.out.println("Queueing MI command to get thread groups");
//                MICommand cmd = gc.getCommandFactory().createMIMIListThreadGroups();
//                int tokenID = getNewTokenId();
//                gc.queueCommand(tokenID, cmd);
//
//                MIListThreadGroupsInfo reply = (MIListThreadGroupsInfo) gc.getResponse(tokenID, DEF_REQUEST_TIMEOUT);
//                if (reply.getMIOutput().getMIResultRecord().getResultClass() == MIResultRecord.ERROR) {
//                    answer.pkt.errorCode = Error.INTERNAL;
//                }
//                MIListThreadGroupsInfo.IThreadGroupInfo[] groupList = reply.getGroupList();
//                int id = 0;
//                for (MIListThreadGroupsInfo.IThreadGroupInfo group: groupList) {
//                    // !!! Assuming that ids start with an i !!!
//                    id = Integer.parseInt(group.getGroupId().substring(1));
//                }
//                answer.writeObjectRef(id);

                ThreadReferenceImpl thread = command.readThreadReference();
                answer.writeObjectRef(thread.threadGroup().uniqueID());
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
                int threadId = (int) command.readObjectRef();

                System.out.println("Queueing MI command to get frames");
                MICommand cmd = gc.getCommandFactory().createMIStackListFrames(String.valueOf(threadId));
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
                int threadId = (int) command.readObjectRef();



                System.out.println("Queueing MI command to get frames");
                MICommand cmd = gc.getCommandFactory().createMIStackListFrames(String.valueOf(threadId));
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
