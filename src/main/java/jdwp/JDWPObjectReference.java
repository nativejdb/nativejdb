package jdwp;

import jdwp.jdi.ObjectReferenceImpl;
import jdwp.jdi.ReferenceTypeImpl;
import jdwp.jdi.ThreadReferenceImpl;

import java.util.List;

public class JDWPObjectReference {
    static class ObjectReference {
        static final int COMMAND_SET = 9;
        private ObjectReference() {}  // hide constructor

        /**
         * Returns the runtime type of the object.
         * The runtime type will be a class or an array.
         */
        static class ReferenceType implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ObjectReferenceImpl objectReference = gc.vm.objectMirror(command.readObjectRef());
                ReferenceTypeImpl referenceType = objectReference.referenceType();
                answer.writeByte(referenceType.tag());
                answer.writeClassRef(referenceType.uniqueID());
            }
        }

        /**
         * Returns the value of one or more instance fields.
         * Each field must be member of the object's type
         * or one of its superclasses, superinterfaces, or implemented interfaces.
         * Access control is not enforced; for example, the values of private
         * fields can be obtained.
         */
        static class GetValues implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ObjectReferenceImpl objectReference = gc.vm.objectMirror(command.readObjectRef());
                ReferenceTypeImpl referenceType = objectReference.referenceType();
                int count = command.readInt();
                answer.writeInt(count);
                for (int i = 0; i < count; i++) {
                    long id = command.readFieldRef();
                    answer.writeValue(objectReference.getValue(referenceType.fieldById(id)));
                }
            }
        }

        /**
         * Sets the value of one or more instance fields.
         * Each field must be member of the object's type
         * or one of its superclasses, superinterfaces, or implemented interfaces.
         * Access control is not enforced; for example, the values of private
         * fields can be set.
         * For primitive values, the value's type must match the
         * field's type exactly. For object values, there must be a
         * widening reference conversion from the value's type to the
         * field's type and the field's type must be loaded.
         */
        static class SetValues implements Command  {
            static final int COMMAND = 3;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Returns monitor information for an object. All threads int the VM must
         * be suspended.
         * Requires canGetMonitorInfo capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class MonitorInfo implements Command  {
            static final int COMMAND = 5;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ObjectReferenceImpl objectReference = gc.vm.objectMirror(command.readObjectRef());
                answer.writeThreadReference(objectReference.owningThread());
                answer.writeInt(objectReference.entryCount());
                List<ThreadReferenceImpl> waiting = objectReference.waitingThreads();
                answer.writeInt(waiting.size());
                for (ThreadReferenceImpl threadReference : waiting) {
                    answer.writeThreadReference(threadReference);
                }
            }
        }

        /**
         * Invokes a instance method.
         * The method must be member of the object's type
         * or one of its superclasses, superinterfaces, or implemented interfaces.
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
         * argument's type exactly. For object arguments, there must be a
         * widening reference conversion from the argument value's type to the
         * argument's type and the argument's type must be loaded.
         * <p>
         * By default, all threads in the target VM are resumed while
         * the method is being invoked if they were previously
         * suspended by an event or by a command.
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
            static final int COMMAND = 6;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Prevents garbage collection for the given object. By
         * default all objects in back-end replies may be
         * collected at any time the target VM is running. A call to
         * this command guarantees that the object will not be
         * collected. The
         * <a href="#JDWP_ObjectReference_EnableCollection">EnableCollection</a>
         * command can be used to
         * allow collection once again.
         * <p>
         * Note that while the target VM is suspended, no garbage
         * collection will occur because all threads are suspended.
         * The typical examination of variables, fields, and arrays
         * during the suspension is safe without explicitly disabling
         * garbage collection.
         * <p>
         * This method should be used sparingly, as it alters the
         * pattern of garbage collection in the target VM and,
         * consequently, may result in application behavior under the
         * debugger that differs from its non-debugged behavior.
         */
        static class DisableCollection implements Command  {
            static final int COMMAND = 7;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Permits garbage collection for this object. By default all
         * objects returned by JDWP may become unreachable in the target VM,
         * and hence may be garbage collected. A call to this command is
         * necessary only if garbage collection was previously disabled with
         * the <a href="#JDWP_ObjectReference_DisableCollection">DisableCollection</a>
         * command.
         */
        static class EnableCollection implements Command  {
            static final int COMMAND = 8;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }

        /**
         * Determines whether an object has been garbage collected in the
         * target VM.
         */
        static class IsCollected implements Command  {
            static final int COMMAND = 9;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                answer.writeBoolean(false);
            }
        }

        /**
         * Returns objects that directly reference this object.
         * Only objects that are reachable for the purposes
         * of garbage collection are returned.
         * Note that an object can also be referenced in other ways,
         * such as from a local variable in a stack frame, or from a JNI global
         * reference.  Such non-object referrers are not returned by this command.
         * <p>Since JDWP version 1.6. Requires canGetInstanceInfo capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class ReferringObjects implements Command  {
            static final int COMMAND = 10;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ObjectReferenceImpl objectReference = gc.vm.objectMirror(command.readObjectRef());
                List<ObjectReferenceImpl> refs = objectReference.referringObjects(command.readInt());
                answer.writeInt(refs.size());
                for (ObjectReferenceImpl ref : refs) {
                    answer.writeTaggedObjectReference(ref);
                }
            }
        }
    }
}
