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

import com.sun.jdi.VMDisconnectedException;
import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.output.*;
import jdwp.jdi.ReferenceTypeImpl;
import jdwp.jdi.ThreadGroupReferenceImpl;
import jdwp.jdi.ThreadReferenceImpl;

import java.util.ArrayList;
import java.util.List;

public class JDWPVirtualMachine {
    static class VirtualMachine {
        static final int COMMAND_SET = 1;
        private VirtualMachine() {}  // hide constructor

        /**
         * Returns the JDWP version implemented by the target VM.
         * The version string format is implementation dependent.
         */
        static class Version implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                answer.writeString(gc.vm.description());
                answer.writeInt(gc.vm.jdwpMajor());
                answer.writeInt(gc.vm.jdwpMinor());
                answer.writeString(gc.vm.version());
                answer.writeString(gc.vm.name());
            }
        }

        /**
         * Returns reference types for all the classes loaded by the target VM
         * which match the given signature.
         * Multple reference types will be returned if two or more class
         * loaders have loaded a class of the same name.
         * The search is confined to loaded classes only; no attempt is made
         * to load a class of the given signature.
         */
        static class ClassesBySignature implements Command  {
            static final int COMMAND = 2;

            static class ClassInfo {

                public static void write(ReferenceTypeImpl referenceType, GDBControl gc, PacketStream answer) {
                    answer.writeByte(referenceType.tag());
                    answer.writeClassRef(referenceType.uniqueID());
                    answer.writeInt(referenceType.ref().getClassStatus());
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                String signature = command.readString();
                List<ReferenceTypeImpl> referenceTypes = gc.vm.findReferenceTypes(signature);
                answer.writeInt(referenceTypes.size());
                for (ReferenceTypeImpl referenceType : referenceTypes) {
                    VirtualMachine.ClassesBySignature.ClassInfo.write(referenceType, gc, answer);
                }
            }
        }

        /**
         * Returns reference types for all classes currently loaded by the
         * target VM.
         */
        static class AllClasses implements Command  {
            static final int COMMAND = 3;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                answer.writeInt(gc.getReferenceTypes().size());
                for(var type : gc.getReferenceTypes().values()) {
                    type.write(answer, false);
                }
            }
        }

        /**
         * Returns all threads currently running in the target VM .
         * The returned list contains threads created through
         * java.lang.Thread, all native threads attached to
         * the target VM through JNI, and system threads created
         * by the target VM. Threads that have not yet been started
         * and threads that have completed their execution are not
         * included in the returned list.
         */
        static class AllThreads implements Command  {
            static final int COMMAND = 4;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
//                List<ThreadReferenceImpl> list = gc.vm.allThreads();
//                answer.writeInt(list.size());
//                for (ThreadReferenceImpl thread : list) {
//                    answer.writeObjectRef(thread.uniqueID());
//                }

                System.out.println("Queueing MI command to get all threads application");
                MICommand cmd = gc.getCommandFactory().createMIThreadInfo();
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

                MIThreadInfoInfo reply = (MIThreadInfoInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.pkt.errorCode = JDWP.Error.VM_DEAD;
                    return;
                }

                MIThread[] allThreads = reply.getThreadList();
                answer.writeInt(allThreads.length);
                for(MIThread thread: allThreads){
                    answer.writeObjectRef(Long.parseLong(thread.getThreadId()));
                }

            }
        }

        /**
         * Returns all thread groups that do not have a parent. This command
         * may be used as the first step in building a tree (or trees) of the
         * existing thread grouanswer.
         */
        static class TopLevelThreadGroups implements Command  {
            static final int COMMAND = 5;


            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                // Assuming a single thread group
//                answer.writeInt(1);
//                answer.writeObjectRef(JDWPThreadReference.threadGroupId);



                System.out.println("Queueing MI command to get top level thread groups");
                MICommand cmd = gc.getCommandFactory().createMIMIListThreadGroups();
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

                MIListThreadGroupsInfo reply = (MIListThreadGroupsInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                if (reply.getMIOutput().getMIResultRecord().getResultClass() == MIResultRecord.ERROR) {
                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                }
                MIListThreadGroupsInfo.IThreadGroupInfo[] groupList = reply.getGroupList();
                answer.writeInt(groupList.length);

                for (MIListThreadGroupsInfo.IThreadGroupInfo group: groupList) {
                    String groupName = group.getName();
                    long groupId = JDWPThreadGroupReference.getNewThreadGroupId();
                    System.out.println("Writing thread group id: " + groupId);
                    answer.writeObjectRef(groupId);
                    JDWPThreadGroupReference.threadGroupById.put(groupId, group);
                    JDWPThreadGroupReference.threadGroupByName.put(group.getName(), groupId);

                }




//                System.out.println("Top Level thread groups");
//                List<ThreadGroupReferenceImpl> list = gc.vm.topLevelThreadGroups();
//                answer.writeInt(list.size());
//                for (ThreadGroupReferenceImpl group : list) {
//                    System.out.println("writing thread group id: " + group.uniqueID());
//                    answer.writeObjectRef(group.uniqueID());
//                }
           }
        }

        /**
         * Invalidates this virtual machine mirror.
         * The communication channel to the target VM is closed, and
         * the target VM prepares to accept another subsequent connection
         * from this debugger or another debugger, including the
         * following tasks:
         * <ul>
         * <li>All event requests are cancelled.
         * <li>All threads suspended by the thread-level
         * <a href="#JDWP_ThreadReference_Resume">resume</a> command
         * or the VM-level
         * <a href="#JDWP_VirtualMachine_Resume">resume</a> command
         * are resumed as many times as necessary for them to run.
         * <li>Garbage collection is re-enabled in all cases where it was
         * <a href="#JDWP_ObjectReference_DisableCollection">disabled</a>
         * </ul>
         * Any current method invocations executing in the target VM
         * are continued after the disconnection. Upon completion of any such
         * method invocation, the invoking thread continues from the
         * location where it was originally stopped.
         * <p>
         * Resources originating in
         * this VirtualMachine (ObjectReferences, ReferenceTypes, etc.)
         * will become invalid.
         */
        static class Dispose implements Command  {
            static final int COMMAND = 6;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                throw new VMDisconnectedException();
            }
        }

        /**
         * Returns the sizes of variably-sized data types in the target VM.
         * The returned values indicate the number of bytes used by the
         * identifiers in command and reply packets.
         */
        static class IDSizes implements Command  {
            static final int COMMAND = 7;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                answer.writeInt(gc.sizeofFieldRef);
                answer.writeInt(gc.sizeofMethodRef);
                answer.writeInt(gc.sizeofObjectRef);
                answer.writeInt(gc.sizeofClassRef);
                answer.writeInt(gc.sizeofFrameRef);
            }
        }

        /**
         * Suspends the execution of the application running in the target VM. All Java threads
         * currently running will be suspended.
         *
         * <p>Unlike java.lang.Thread.suspend, suspends of both the virtual machine and individual
         * threads are counted. Before a thread will run again, it must be resumed through the <a
         * href="#JDWP_VirtualMachine_Resume">VM-level resume</a> command or the <a
         * href="#JDWP_ThreadReference_Resume">thread-level resume</a> command the same number of times
         * it has been suspended.
         */
        static class Suspend implements Command {
            static final int COMMAND = 8;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {

                try {
                    System.out.println("Queueing MI command to suspend application");
                    MICommand cmd = gc.getCommandFactory().createMIExecInterrupt(true);
                    int tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);

                    MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                    if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.pkt.errorCode = JDWP.Error.INTERNAL; // The virtual machine is not running.
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Resumes execution of the application after the suspend
         * command or an event has stopped it.
         * Suspensions of the Virtual Machine and individual threads are
         * counted. If a particular thread is suspended n times, it must
         * resumed n times before it will continue.
         */
        static class Resume implements Command  {
            static final int COMMAND = 9;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                try {
                    System.out.println("Queueing MI command to resume application");
                    MICommand cmd = gc.getCommandFactory().createMIExecContinue(true);
                    int tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);

                    MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                    if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.pkt.errorCode = JDWP.Error.INTERNAL; // The virtual machine is not running.
                    }

//                    MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
//                    if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
//                        if (reply.getErrorMsg().equals("The program is not being run.")) {
//                            // Need to run the program
//                            cmd = gc.getCommandFactory().createMIExecRun();
//                            tokenID = JDWP.getNewTokenId();
//                            gc.queueCommand(tokenID, cmd);
//
//                            MIInfo reply1 = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
//                            if (reply1.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
//                                answer.pkt.errorCode = JDWP.Error.INTERNAL;
//                            }
//
//                        } else {
//                            answer.pkt.errorCode = JDWP.Error.VM_DEAD; // The virtual machine is not running.
//                        }
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Terminates the target VM with the given exit code.
         * On some platforms, the exit code might be truncated, for
         * example, to the low order 8 bits.
         * All ids previously returned from the target VM become invalid.
         * Threads running in the VM are abruptly terminated.
         * A thread death exception is not thrown and
         * finally blocks are not run.
         */
        static class Exit implements Command  {
            static final int COMMAND = 10;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                System.out.println("Queueing MI command to exit application");
                MICommand cmd = gc.getCommandFactory().createMIGDBExit();
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

                MIInfo reply = gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                }
            }
        }

        /**
         * Creates a new string object in the target VM and returns
         * its id.
         */
        static class CreateString implements Command  {
            static final int COMMAND = 11;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
            }
        }

        /**
         * Retrieve this VM's capabilities. The capabilities are returned
         * as booleans, each indicating the presence or absence of a
         * capability. The commands associated with each capability will
         * return the NOT_IMPLEMENTED error if the cabability is not
         * available.
         */
        static class Capabilities implements Command  {
            static final int COMMAND = 12;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                answer.writeBoolean(gc.vm.canWatchFieldModification());
                answer.writeBoolean(gc.vm.canWatchFieldAccess());
                answer.writeBoolean(gc.vm.canGetBytecodes());
                answer.writeBoolean(gc.vm.canGetSyntheticAttribute());
                answer.writeBoolean(gc.vm.canGetOwnedMonitorInfo());
                answer.writeBoolean(gc.vm.canGetCurrentContendedMonitor());
                answer.writeBoolean(gc.vm.canGetMonitorInfo());
            }
        }

        /**
         * Retrieve the classpath and bootclasspath of the target VM.
         * If the classpath is not defined, returns an empty list. If the
         * bootclasspath is not defined returns an empty list.
         */
        static class ClassPaths implements Command  {
            static final int COMMAND = 13;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                answer.writeString(gc.vm.baseDirectory());

                List<String> classPath = gc.vm.classPath();
                answer.writeInt(classPath.size());
                for (String s : classPath) {
                    answer.writeString(s);
                }

                List<String> bootClassPath = gc.vm.bootClassPath();
                answer.writeInt(bootClassPath.size());
                for (String s : bootClassPath) {
                    answer.writeString(s);
                }
            }
        }

        /**
         * Releases a list of object IDs. For each object in the list, the
         * following applies.
         * The count of references held by the back-end (the reference
         * count) will be decremented by refCnt.
         * If thereafter the reference count is less than
         * or equal to zero, the ID is freed.
         * Any back-end resources associated with the freed ID may
         * be freed, and if garbage collection was
         * disabled for the object, it will be re-enabled.
         * The sender of this command
         * promises that no further commands will be sent
         * referencing a freed ID.
         * <p>
         * Use of this command is not required. If it is not sent,
         * resources associated with each ID will be freed by the back-end
         * at some time after the corresponding object is garbage collected.
         * It is most useful to use this command to reduce the load on the
         * back-end if a very large number of
         * objects has been retrieved from the back-end (a large array,
         * for example) but may not be garbage collected any time soon.
         * <p>
         * IDs may be re-used by the back-end after they
         * have been freed with this command.
         * This description assumes reference counting,
         * a back-end may use any implementation which operates
         * equivalently.
         */
        static class DisposeObjects implements Command  {
            static final int COMMAND = 14;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
            }
        }

        /**
         * Tells the target VM to stop sending events. Events are not discarded;
         * they are held until a subsequent ReleaseEvents command is sent.
         * This command is useful to control the number of events sent
         * to the debugger VM in situations where very large numbers of events
         * are generated.
         * While events are held by the debugger back-end, application
         * execution may be frozen by the debugger back-end to prevent
         * buffer overflows on the back end.
         * Responses to commands are never held and are not affected by this
         * command. If events are already being held, this command is
         * ignored.
         */
        static class HoldEvents implements Command  {
            static final int COMMAND = 15;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
            }
        }

        /**
         * Tells the target VM to continue sending events. This command is
         * used to restore normal activity after a HoldEvents command. If
         * there is no current HoldEvents command in effect, this command is
         * ignored.
         */
        static class ReleaseEvents implements Command  {
            static final int COMMAND = 16;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
            }
        }

        /**
         * Retrieve all of this VM's capabilities. The capabilities are returned
         * as booleans, each indicating the presence or absence of a
         * capability. The commands associated with each capability will
         * return the NOT_IMPLEMENTED error if the cabability is not
         * available.
         * Since JDWP version 1.4.
         */
        static class CapabilitiesNew implements Command  {
            static final int COMMAND = 17;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                answer.writeBoolean(gc.vm.canWatchFieldModification());
                answer.writeBoolean(gc.vm.canWatchFieldAccess());
                answer.writeBoolean(gc.vm.canGetBytecodes());
                answer.writeBoolean(gc.vm.canGetSyntheticAttribute());
                answer.writeBoolean(gc.vm.canGetOwnedMonitorInfo());
                answer.writeBoolean(gc.vm.canGetCurrentContendedMonitor());
                answer.writeBoolean(gc.vm.canGetMonitorInfo());
                answer.writeBoolean(gc.vm.canRedefineClasses());
                answer.writeBoolean(gc.vm.canAddMethod());
                answer.writeBoolean(gc.vm.canUnrestrictedlyRedefineClasses());
                answer.writeBoolean(gc.vm.canPopFrames());
                answer.writeBoolean(gc.vm.canUseInstanceFilters());
                answer.writeBoolean(gc.vm.canGetSourceDebugExtension());
                answer.writeBoolean(gc.vm.canRequestVMDeathEvent());
                answer.writeBoolean(false);
                answer.writeBoolean(gc.vm.canGetInstanceInfo());
                answer.writeBoolean(gc.vm.canRequestMonitorEvents());
                answer.writeBoolean(gc.vm.canGetMonitorFrameInfo());
                answer.writeBoolean(gc.vm.canUseSourceNameFilters());
                answer.writeBoolean(gc.vm.canGetConstantPool());
                answer.writeBoolean(gc.vm.canForceEarlyReturn());
                answer.writeBoolean(false);
                answer.writeBoolean(false);
                answer.writeBoolean(false);
                answer.writeBoolean(false);
                answer.writeBoolean(false);
                answer.writeBoolean(false);
                answer.writeBoolean(false);
                answer.writeBoolean(false);
                answer.writeBoolean(false);
                answer.writeBoolean(false);
                answer.writeBoolean(false);
            }
        }

        /**
         * Installs new class definitions.
         * If there are active stack frames in methods of the redefined classes in the
         * target VM then those active frames continue to run the bytecodes of the
         * original method. These methods are considered obsolete - see
         * <a href="#JDWP_Method_IsObsolete">IsObsolete</a>. The methods in the
         * redefined classes will be used for new invokes in the target VM.
         * The original method ID refers to the redefined method.
         * All breakpoints in the redefined classes are cleared.
         * If resetting of stack frames is desired, the
         * <a href="#JDWP_StackFrame_PopFrames">PopFrames</a> command can be used
         * to pop frames with obsolete methods.
         * <p>
         * Requires canRedefineClasses capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         * In addition to the canRedefineClasses capability, the target VM must
         * have the canAddMethod capability to add methods when redefining classes,
         * or the canUnrestrictedlyRedefineClasses to redefine classes in arbitrary
         * ways.
         */
        static class RedefineClasses implements Command  {
            static final int COMMAND = 18;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Set the default stratum. Requires canSetDefaultStratum capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class SetDefaultStratum implements Command  {
            static final int COMMAND = 19;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Returns reference types for all classes currently loaded by the
         * target VM.
         * Both the JNI signature and the generic signature are
         * returned for each class.
         * Generic signatures are described in the signature attribute
         * section in
         * <cite>The Java&trade; Virtual Machine Specification</cite>.
         * Since JDWP version 1.5.
         */
        static class AllClassesWithGeneric implements Command  {
            static final int COMMAND = 20;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                var types = gc.getReferenceTypes();
                answer.writeInt(types.size());
                for(var type : types.values()) {
                    type.write(answer, true);
                }
            }
        }

        /**
         * Returns the number of instances of each reference type in the input list.
         * Only instances that are reachable for the purposes of
         * garbage collection are counted.  If a reference type is invalid,
         * eg. it has been unloaded, zero is returned for its instance count.
         * <p>Since JDWP version 1.6. Requires canGetInstanceInfo capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class InstanceCounts implements Command  {
            static final int COMMAND = 21;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                int count = command.readInt();
                List<ReferenceTypeImpl> refs = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    refs.add(command.readReferenceType());
                }
                long[] counts = gc.vm.instanceCounts(refs);
                answer.writeInt(counts.length);
                for (long l : counts) {
                    answer.writeLong(l);
                }
            }
        }

        /**
         * Returns all modules in the target VM.
         * <p>Since JDWP version 9.
         */
        static class AllModules implements Command  {
            static final int COMMAND = 22;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
            }
        }
    }
}
