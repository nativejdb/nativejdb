package jdwp;

import com.sun.jdi.AbsentInformationException;
import jdwp.jdi.LocalVariableImpl;
import jdwp.jdi.LocationImpl;
import jdwp.jdi.MethodImpl;
import jdwp.jdi.ReferenceTypeImpl;

import java.util.Collections;
import java.util.List;

public class JDWPMethod {
    static class Method {
        static final int COMMAND_SET = 6;
        private Method() {}  // hide constructor

        /**
         * Returns line number information for the method, if present.
         * The line table maps source line numbers to the initial code index
         * of the line. The line table
         * is ordered by code index (from lowest to highest). The line number
         * information is constant unless a new class definition is installed
         * using <a href="#JDWP_VirtualMachine_RedefineClasses">RedefineClasses</a>.
         */
        static class LineTable implements Command  {
            static final int COMMAND = 1;

            static class LineInfo {

                public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                MethodImpl method = referenceType.methodById(command.readMethodRef());

                if (method.isNative()) {
                    answer.pkt.errorCode = JDWP.Error.NATIVE_METHOD;
                    return;
                }

                List<LocationImpl> locations = Collections.emptyList();
                try {
                    locations = method.allLineLocations();
                } catch (AbsentInformationException ignored) {
                }
                sun.jvm.hotspot.oops.Method ref = method.ref();
                long start = 0;
                long end = ref.getCodeSize();
                if (end == 0) {
                    start = -1;
                }
                answer.writeLong(start);
                answer.writeLong(end);
                answer.writeInt(locations.size());
                for (LocationImpl location : locations) {
                    answer.writeLong(location.codeIndex());
                    answer.writeInt(location.lineNumber());
                }
            }
        }

        /**
         * Returns variable information for the method. The variable table
         * includes arguments and locals declared within the method. For
         * instance methods, the "this" reference is included in the
         * table. Also, synthetic variables may be present.
         */
        static class VariableTable implements Command  {
            static final int COMMAND = 2;

            /**
             * Information about the variable.
             */
            static class SlotInfo {

                public static void write(LocalVariableImpl var, GDBControl gc, PacketStream answer) {
                    answer.writeLong(var.getStart());
                    answer.writeString(var.name());
                    answer.writeString(var.signature());
                    answer.writeInt(var.getLength());
                    answer.writeInt(var.slot());
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                MethodImpl method = referenceType.methodById(command.readMethodRef());
                try {
                    List<LocalVariableImpl> variables = method.variables();
                    answer.writeInt(method.argSlotCount());
                    answer.writeInt(variables.size());
                    for (LocalVariableImpl variable : variables) {
                        SlotInfo.write(variable, gc, answer);
                    }

                } catch (AbsentInformationException e) {
                    answer.pkt.errorCode = JDWP.Error.ABSENT_INFORMATION;
                }
            }
        }

        /**
         * Retrieve the method's bytecodes as defined in
         * <cite>The Java&trade; Virtual Machine Specification</cite>.
         * Requires canGetBytecodes capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class Bytecodes implements Command  {
            static final int COMMAND = 3;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                MethodImpl method = referenceType.methodById(command.readMethodRef());
                byte[] bytecodes = method.bytecodes();
                answer.writeInt(bytecodes.length);
                answer.writeByteArray(bytecodes);
            }
        }

        /**
         * Determine if this method is obsolete. A method is obsolete if it has been replaced
         * by a non-equivalent method using the
         * <a href="#JDWP_VirtualMachine_RedefineClasses">RedefineClasses</a> command.
         * The original and redefined methods are considered equivalent if their bytecodes are
         * the same except for indices into the constant pool and the referenced constants are
         * equal.
         */
        static class IsObsolete implements Command  {
            static final int COMMAND = 4;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                MethodImpl method = referenceType.methodById(command.readMethodRef());
                answer.writeBoolean(method.isObsolete());
            }
        }

        /**
         * Returns variable information for the method, including
         * generic signatures for the variables. The variable table
         * includes arguments and locals declared within the method. For
         * instance methods, the "this" reference is included in the
         * table. Also, synthetic variables may be present.
         * Generic signatures are described in the signature attribute
         * section in
         * <cite>The Java&trade; Virtual Machine Specification</cite>.
         * Since JDWP version 1.5.
         */
        static class VariableTableWithGeneric implements Command  {
            static final int COMMAND = 5;

            /**
             * Information about the variable.
             */
            static class SlotInfo {

                public static void write(LocalVariableImpl var, GDBControl gc, PacketStream answer) {
                    answer.writeLong(var.getStart());
                    answer.writeString(var.name());
                    answer.writeString(var.signature());
                    answer.writeStringOrEmpty(var.genericSignature());
                    answer.writeInt(var.getLength());
                    answer.writeInt(var.slot());
                }
            }


            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                MethodImpl method = referenceType.methodById(command.readMethodRef());
                try {
                    List<LocalVariableImpl> variables = method.variables();
                    answer.writeInt(method.argSlotCount());
                    answer.writeInt(variables.size());
                    for (LocalVariableImpl variable : variables) {
                        SlotInfo.write(variable, gc, answer);
                    }

                } catch (AbsentInformationException e) {
                    answer.pkt.errorCode = JDWP.Error.ABSENT_INFORMATION;
                }
            }
        }
    }
}
