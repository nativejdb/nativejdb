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

import com.sun.jdi.VirtualMachine;

public class JDWPEvent {
    static class Event {
        static final int COMMAND_SET = 64;
        private Event() {}  // hide constructor

        /**
         * Several events may occur at a given time in the target VM.
         * For example, there may be more than one breakpoint request
         * for a given location
         * or you might single step to the same location as a
         * breakpoint request.  These events are delivered
         * together as a composite event.  For uniformity, a
         * composite event is always used
         * to deliver events, even if there is only one event to report.
         * <P>
         * The events that are grouped in a composite event are restricted in the
         * following ways:
         * <P>
         * <UL>
         * <LI>Only with other thread start events for the same thread:
         *     <UL>
         *     <LI>Thread Start Event
         *     </UL>
         * <LI>Only with other thread death events for the same thread:
         *     <UL>
         *     <LI>Thread Death Event
         *     </UL>
         * <LI>Only with other class prepare events for the same class:
         *     <UL>
         *     <LI>Class Prepare Event
         *     </UL>
         * <LI>Only with other class unload events for the same class:
         *     <UL>
         *     <LI>Class Unload Event
         *     </UL>
         * <LI>Only with other access watchpoint events for the same field access:
         *     <UL>
         *     <LI>Access Watchpoint Event
         *     </UL>
         * <LI>Only with other modification watchpoint events for the same field
         * modification:
         *     <UL>
         *     <LI>Modification Watchpoint Event
         *     </UL>
         * <LI>Only with other Monitor contended enter events for the same monitor object:
         *     <UL>
         *     <LI>Monitor Contended Enter Event
         *     </UL>
         * <LI>Only with other Monitor contended entered events for the same monitor object:
         *     <UL>
         *     <LI>Monitor Contended Entered Event
         *     </UL>
         * <LI>Only with other Monitor wait events for the same monitor object:
         *     <UL>
         *     <LI>Monitor Wait Event
         *     </UL>
         * <LI>Only with other Monitor waited events for the same monitor object:
         *     <UL>
         *     <LI>Monitor Waited Event
         *     </UL>
         * <LI>Only with other ExceptionEvents for the same exception occurrance:
         *     <UL>
         *     <LI>ExceptionEvent
         *     </UL>
         * <LI>Only with other members of this group, at the same location
         * and in the same thread:
         *     <UL>
         *     <LI>Breakpoint Event
         *     <LI>Step Event
         *     <LI>Method Entry Event
         *     <LI>Method Exit Event
         *     </UL>
         * </UL>
         * <P>
         * The VM Start Event and VM Death Event are automatically generated events.
         * This means they do not need to be requested using the
         * <a href="#JDWP_EventRequest_Set">EventRequest.Set</a> command.
         * The VM Start event signals the completion of VM initialization. The VM Death
         * event signals the termination of the VM.
         * If there is a debugger connected at the time when an automatically generated
         * event occurs it is sent from the target VM. Automatically generated events may
         * also be requested using the EventRequest.Set command and thus multiple events
         * of the same event kind will be sent from the target VM when an event occurs.
         * Automatically generated events are sent with the requestID field
         * in the Event Data set to 0. The value of the suspendPolicy field in the
         * Event Data depends on the event. For the automatically generated VM Start
         * Event the value of suspendPolicy is not defined and is therefore implementation
         * or configuration specific. In the Sun implementation, for example, the
         * suspendPolicy is specified as an option to the JDWP agent at launch-time.
         * The automatically generated VM Death Event will have the suspendPolicy set to
         * NONE.
         */
        static class Composite implements Command  {
            static final int COMMAND = 100;

            static class Events {
                abstract static class EventsCommon {
                    abstract byte eventKind();
                }

                EventsCommon aEventsCommon;

                public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                }

                /**
                 * Notification of initialization of a target VM.  This event is
                 * received before the main thread is started and before any
                 * application code has been executed. Before this event occurs
                 * a significant amount of system code has executed and a number
                 * of system classes have been loaded.
                 * This event is always generated by the target VM, even
                 * if not explicitly requested.
                 */
                static class VMStart extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.VM_START;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of step completion in the target VM. The step event
                 * is generated before the code at its location is executed.
                 */
                static class SingleStep extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.SINGLE_STEP;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a breakpoint in the target VM. The breakpoint event
                 * is generated before the code at its location is executed.
                 */
                static class Breakpoint extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.BREAKPOINT;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a method invocation in the target VM. This event
                 * is generated before any code in the invoked method has executed.
                 * Method entry events are generated for both native and non-native
                 * methods.
                 * <P>
                 * In some VMs method entry events can occur for a particular thread
                 * before its thread start event occurs if methods are called
                 * as part of the thread's initialization.
                 */
                static class MethodEntry extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.METHOD_ENTRY;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a method return in the target VM. This event
                 * is generated after all code in the method has executed, but the
                 * location of this event is the last executed location in the method.
                 * Method exit events are generated for both native and non-native
                 * methods. Method exit events are not generated if the method terminates
                 * with a thrown exception.
                 */
                static class MethodExit extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.METHOD_EXIT;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a method return in the target VM. This event
                 * is generated after all code in the method has executed, but the
                 * location of this event is the last executed location in the method.
                 * Method exit events are generated for both native and non-native
                 * methods. Method exit events are not generated if the method terminates
                 * with a thrown exception. <p>Since JDWP version 1.6.
                 */
                static class MethodExitWithReturnValue extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.METHOD_EXIT_WITH_RETURN_VALUE;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification that a thread in the target VM is attempting
                 * to enter a monitor that is already acquired by another thread.
                 * Requires canRequestMonitorEvents capability - see
                 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
                 * <p>Since JDWP version 1.6.
                 */
                static class MonitorContendedEnter extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.MONITOR_CONTENDED_ENTER;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a thread in the target VM is entering a monitor
                 * after waiting for it to be released by another thread.
                 * Requires canRequestMonitorEvents capability - see
                 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
                 * <p>Since JDWP version 1.6.
                 */
                static class MonitorContendedEntered extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.MONITOR_CONTENDED_ENTERED;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a thread about to wait on a monitor object.
                 * Requires canRequestMonitorEvents capability - see
                 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
                 * <p>Since JDWP version 1.6.
                 */
                static class MonitorWait extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.MONITOR_WAIT;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification that a thread in the target VM has finished waiting on
                 * Requires canRequestMonitorEvents capability - see
                 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
                 * a monitor object.
                 * <p>Since JDWP version 1.6.
                 */
                static class MonitorWaited extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.MONITOR_WAITED;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of an exception in the target VM.
                 * If the exception is thrown from a non-native method,
                 * the exception event is generated at the location where the
                 * exception is thrown.
                 * If the exception is thrown from a native method, the exception event
                 * is generated at the first non-native location reached after the exception
                 * is thrown.
                 */
                static class Exception extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.EXCEPTION;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a new running thread in the target VM.
                 * The new thread can be the result of a call to
                 * <code>java.lang.Thread.start</code> or the result of
                 * attaching a new thread to the VM though JNI. The
                 * notification is generated by the new thread some time before
                 * its execution starts.
                 * Because of this timing, it is possible to receive other events
                 * for the thread before this event is received. (Notably,
                 * Method Entry Events and Method Exit Events might occur
                 * during thread initialization.
                 * It is also possible for the
                 * <a href="#JDWP_VirtualMachine_AllThreads">VirtualMachine AllThreads</a>
                 * command to return
                 * a thread before its thread start event is received.
                 * <p>
                 * Note that this event gives no information
                 * about the creation of the thread object which may have happened
                 * much earlier, depending on the VM being debugged.
                 */
                static class ThreadStart extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.THREAD_START;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a completed thread in the target VM. The
                 * notification is generated by the dying thread before it terminates.
                 * Because of this timing, it is possible
                 * for {@link VirtualMachine} to return this thread
                 * after this event is received.
                 * <p>
                 * Note that this event gives no information
                 * about the lifetime of the thread object. It may or may not be collected
                 * soon depending on what references exist in the target VM.
                 */
                static class ThreadDeath extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.THREAD_DEATH;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a class prepare in the target VM. See the JVM
                 * specification for a definition of class preparation. Class prepare
                 * events are not generated for primtiive classes (for example,
                 * java.lang.Integer.TYPE).
                 */
                static class ClassPrepare extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.CLASS_PREPARE;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a class unload in the target VM.
                 * <p>
                 * There are severe constraints on the debugger back-end during
                 * garbage collection, so unload information is greatly limited.
                 */
                static class ClassUnload extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.CLASS_UNLOAD;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a field access in the target VM.
                 * Field modifications
                 * are not considered field accesses.
                 * Requires canWatchFieldAccess capability - see
                 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
                 */
                static class FieldAccess extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.FIELD_ACCESS;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                /**
                 * Notification of a field modification in the target VM.
                 * Requires canWatchFieldModification capability - see
                 * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
                 */
                static class FieldModification extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.FIELD_MODIFICATION;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }

                static class VMDeath extends EventsCommon {
                    static final byte ALT_ID = JDWP.EventKind.VM_DEATH;
                    byte eventKind() {
                        return ALT_ID;
                    }

                    public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                    }
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
            }
        }
    }
}
