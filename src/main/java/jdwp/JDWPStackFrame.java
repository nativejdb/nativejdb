package jdwp;

import com.sun.jdi.IncompatibleThreadStateException;
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
        static class GetValues implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ThreadReferenceImpl thread = command.readThreadReference();
                try {
                    StackFrameImpl frame = thread.frame((int) command.readFrameRef());
                    int slots = command.readInt();
                    int available = frame.getAvailableSlots(); //HERE
                    answer.writeInt(slots);
                    for (int i = 0; i < slots; i++) {
                        int slot = command.readInt();
                        if (slot >= available) {
                            answer.pkt.errorCode = JDWP.Error.INVALID_SLOT;
                            return;
                        }
                        ValueImpl slotValue = frame.getSlotValue(slot, command.readByte());
                        answer.writeValue(slotValue);
                    }

                } catch (IndexOutOfBoundsException e) {
                    // hack
                } catch (IncompatibleThreadStateException e) {
                    e.printStackTrace();
                }
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
