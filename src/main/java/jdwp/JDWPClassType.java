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

import jdwp.jdi.ClassTypeImpl;
import jdwp.jdi.ReferenceTypeImpl;

public class JDWPClassType {
    static class ClassType {
        static final int COMMAND_SET = 3;
        private ClassType() {}  // hide constructor

        /**
         * Returns the immediate superclass of a class.
         */
        static class Superclass implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = gc.vm.getReferenceTypeById(command.readObjectRef());
                if (type instanceof ClassTypeImpl) {
                    ClassTypeImpl superclass = ((ClassTypeImpl) type).superclass();
                    if (superclass != null) {
                        answer.writeClassRef(superclass.uniqueID());
                    }
                    else {
                        answer.writeNullObjectRef();
                    }
                }
                else {
                    answer.pkt.errorCode = JDWP.Error.INVALID_CLASS;
                }
            }
        }

        /**
         * Sets the value of one or more static fields.
         * Each field must be member of the class type
         * or one of its superclasses, superinterfaces, or implemented interfaces.
         * Access control is not enforced; for example, the values of private
         * fields can be set. Final fields cannot be set.
         * For primitive values, the value's type must match the
         * field's type exactly. For object values, there must exist a
         * widening reference conversion from the value's type to the
         * field's type and the field's type must be loaded.
         */
        static class SetValues implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Invokes a static method.
         * The method must be member of the class type
         * or one of its superclasses.
         * Access control is not enforced; for example, private
         * methods can be invoked.
         * <p>
         * The method invocation will occur in the specified thread.
         * Method invocation can occur only if the specified thread
         * has been suspended by an event.
         * Method invocation is not supported
         * when the target VM has been suspended by the front-end.
         * <p>
         * The specified method is invoked with the arguments in the specified
         * argument list.
         * The method invocation is synchronous; the reply packet is not
         * sent until the invoked method returns in the target VM.
         * The return value (possibly the void value) is
         * included in the reply packet.
         * If the invoked method throws an exception, the
         * exception object ID is set in the reply packet; otherwise, the
         * exception object ID is null.
         * <p>
         * For primitive arguments, the argument value's type must match the
         * argument's type exactly. For object arguments, there must exist a
         * widening reference conversion from the argument value's type to the
         * argument's type and the argument's type must be loaded.
         * <p>
         * By default, all threads in the target VM are resumed while
         * the method is being invoked if they were previously
         * suspended by an event or by command.
         * This is done to prevent the deadlocks
         * that will occur if any of the threads own monitors
         * that will be needed by the invoked method. It is possible that
         * breakpoints or other events might occur during the invocation.
         * Note, however, that this implicit resume acts exactly like
         * the ThreadReference resume command, so if the thread's suspend
         * count is greater than 1, it will remain in a suspended state
         * during the invocation. By default, when the invocation completes,
         * all threads in the target VM are suspended, regardless their state
         * before the invocation.
         * <p>
         * The resumption of other threads during the invoke can be prevented
         * by specifying the INVOKE_SINGLE_THREADED
         * bit flag in the <code>options</code> field; however,
         * there is no protection against or recovery from the deadlocks
         * described above, so this option should be used with great caution.
         * Only the specified thread will be resumed (as described for all
         * threads above). Upon completion of a single threaded invoke, the invoking thread
         * will be suspended once again. Note that any threads started during
         * the single threaded invocation will not be suspended when the
         * invocation completes.
         * <p>
         * If the target VM is disconnected during the invoke (for example, through
         * the VirtualMachine dispose command) the method invocation continues.
         */
        static class InvokeMethod implements Command  {
            static final int COMMAND = 3;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Creates a new object of this type, invoking the specified
         * constructor. The constructor method ID must be a member of
         * the class type.
         * <p>
         * Instance creation will occur in the specified thread.
         * Instance creation can occur only if the specified thread
         * has been suspended by an event.
         * Method invocation is not supported
         * when the target VM has been suspended by the front-end.
         * <p>
         * The specified constructor is invoked with the arguments in the specified
         * argument list.
         * The constructor invocation is synchronous; the reply packet is not
         * sent until the invoked method returns in the target VM.
         * The return value (possibly the void value) is
         * included in the reply packet.
         * If the constructor throws an exception, the
         * exception object ID is set in the reply packet; otherwise, the
         * exception object ID is null.
         * <p>
         * For primitive arguments, the argument value's type must match the
         * argument's type exactly. For object arguments, there must exist a
         * widening reference conversion from the argument value's type to the
         * argument's type and the argument's type must be loaded.
         * <p>
         * By default, all threads in the target VM are resumed while
         * the method is being invoked if they were previously
         * suspended by an event or by command.
         * This is done to prevent the deadlocks
         * that will occur if any of the threads own monitors
         * that will be needed by the invoked method. It is possible that
         * breakpoints or other events might occur during the invocation.
         * Note, however, that this implicit resume acts exactly like
         * the ThreadReference resume command, so if the thread's suspend
         * count is greater than 1, it will remain in a suspended state
         * during the invocation. By default, when the invocation completes,
         * all threads in the target VM are suspended, regardless their state
         * before the invocation.
         * <p>
         * The resumption of other threads during the invoke can be prevented
         * by specifying the INVOKE_SINGLE_THREADED
         * bit flag in the <code>options</code> field; however,
         * there is no protection against or recovery from the deadlocks
         * described above, so this option should be used with great caution.
         * Only the specified thread will be resumed (as described for all
         * threads above). Upon completion of a single threaded invoke, the invoking thread
         * will be suspended once again. Note that any threads started during
         * the single threaded invocation will not be suspended when the
         * invocation completes.
         * <p>
         * If the target VM is disconnected during the invoke (for example, through
         * the VirtualMachine dispose command) the method invocation continues.
         */
        static class NewInstance implements Command  {
            static final int COMMAND = 4;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }
    }
}
