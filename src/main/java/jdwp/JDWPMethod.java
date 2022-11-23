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

import jdwp.jdi.MethodImpl;
import jdwp.jdi.ReferenceTypeImpl;

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

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                long refType = command.readObjectRef();
                var referenceType = gc.getReferenceTypes().findbyId(refType);
                if (referenceType ==null) {
                    answer.setErrorCode((short) JDWP.Error.INVALID_CLASS);
                } else {
                    var methodID = command.readMethodRef();
                    var method = referenceType.findMethodById(methodID);
                    if (method == null) {
                        answer.setErrorCode((short) JDWP.Error.INVALID_METHODID);
                    } else {
                        var olines = method.getLines();
                        olines.ifPresentOrElse(lines -> {
                            answer.writeLong(lines.ceiling(Integer.MIN_VALUE));
                            answer.writeLong(lines.floor(Integer.MAX_VALUE));
                            answer.writeInt(lines.size());
                            for(var line : lines) {
                                answer.writeLong(line);
                                answer.writeInt(line);
                            }
                        }, () -> {
                        answer.writeLong(-1);
                        answer.writeLong(-1);
                        answer.writeInt(0);
                        });
                    }
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

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                variableTable(gc, answer, command, false);
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
                answer.setErrorCode((short) JDWP.Error.NOT_IMPLEMENTED);
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

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                variableTable(gc, answer, command, true);
            }
        }
    }

    private static void variableTable(GDBControl gc, PacketStream answer, PacketStream command, boolean generic) {
        var referenceTypeID = command.readObjectRef();
        var referenceType = gc.getReferenceTypes().findbyId(referenceTypeID);
        if (referenceType != null) {
            var methodID = command.readMethodRef();
            var method = referenceType.findMethodById(methodID);
            if (method != null) {
                answer.writeInt(method.getArgumentCount());
                answer.writeInt(method.getVariables().size());
                for(var variable : method.getVariables()) {
                    answer.writeLong(variable.getStartLine());
                    answer.writeString(variable.getName());
                    answer.writeString(variable.getJNISignature());
                    if (generic) {
                        answer.writeString("");
                    }
                    answer.writeInt(variable.getEndLine() -  variable.getStartLine() + 1);
                    answer.writeInt(variable.getIndex());
                }
            } else {
                answer.setErrorCode((short) JDWP.Error.INVALID_METHODID);
            }

        } else {
            answer.setErrorCode((short) JDWP.Error.INVALID_CLASS);
        }
    }
}
