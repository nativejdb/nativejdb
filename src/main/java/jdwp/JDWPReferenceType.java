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

import com.sun.jdi.AbsentInformationException;
import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.output.MIResultRecord;
import gdb.mi.service.command.output.MiSymbolInfoVariablesInfo;
import jdwp.jdi.*;

import java.util.List;

public class JDWPReferenceType {
    static class ReferenceType {
        static final int COMMAND_SET = 2;
        private ReferenceType() {}  // hide constructor

        /**
         * Returns the JNI signature of a reference type.
         * JNI signature formats are described in the
         * <a href="https://docs.oracle.com/en/java/javase/13/docs/specs/jni/index.html">Java Native Inteface Specification</a>
         * <p>
         * For primitive classes
         * the returned signature is the signature of the corresponding primitive
         * type; for example, "I" is returned as the signature of the class
         * represented by java.lang.Integer.TYPE.
         */
        static class Signature implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                answer.writeString(referenceType.signature());
            }
        }

        /**
         * Returns the instance of java.lang.ClassLoader which loaded
         * a given reference type. If the reference type was loaded by the
         * system class loader, the returned object ID is null.
         */
        static class ClassLoader implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                answer.writeClassLoaderReference(referenceType.classLoader());
            }
        }

        /**
         * Returns the modifiers (also known as access flags) for a reference type.
         * The returned bit mask contains information on the declaration
         * of the reference type. If the reference type is an array or
         * a primitive class (for example, java.lang.Integer.TYPE), the
         * value of the returned bit mask is undefined.
         */
        static class Modifiers implements Command  {
            static final int COMMAND = 3;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl referenceType = command.readReferenceType();
                answer.writeInt(referenceType.modifiers());
            }
        }

        /**
         * Returns information for each field in a reference type.
         * Inherited fields are not included.
         * The field list will include any synthetic fields created
         * by the compiler.
         * Fields are returned in the order they occur in the class file.
         */
        static class Fields implements Command  {
            static final int COMMAND = 4;

            static class FieldInfo {

                public static void write(FieldImpl field, GDBControl gc, PacketStream answer) {
                    answer.writeFieldRef(field.uniqueID());
                    answer.writeString(field.name());
                    answer.writeString(field.signature());
                    answer.writeInt(field.modifiers());
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = command.readReferenceType();
                List<FieldImpl> fields = type.fields();
                answer.writeInt(fields.size());
                for (FieldImpl field : fields) {
                    FieldInfo.write(field, gc, answer);
                }
            }
        }

        /**
         * Returns information for each method in a reference type.
         * Inherited methods are not included. The list of methods will
         * include constructors (identified with the name "&lt;init&gt;"),
         * the initialization method (identified with the name "&lt;clinit&gt;")
         * if present, and any synthetic methods created by the compiler.
         * Methods are returned in the order they occur in the class file.
         */
        static class Methods implements Command  {
            static final int COMMAND = 5;

            static class MethodInfo {

                public static void write(MethodImpl method, GDBControl gc, PacketStream answer) {
                    answer.writeMethodRef(method.uniqueID());
                    answer.writeString(method.name());
                    answer.writeString(method.signature());
                    answer.writeInt(method.modifiers());
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = command.readReferenceType();
                List<MethodImpl> methods = type.methods();
                answer.writeInt(methods.size());
                for (MethodImpl method : methods) {
                    MethodInfo.write(method, gc, answer);
                }
            }
        }

        /**
         * Returns the value of one or more static fields of the
         * reference type. Each field must be member of the reference type
         * or one of its superclasses, superinterfaces, or implemented interfaces.
         * Access control is not enforced; for example, the values of private
         * fields can be obtained.
         */
        static class GetValues implements Command  {
            static final int COMMAND = 6;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
            /*
               -symbol-info-variables [--include-nondebug] (Only gives us global field names - no values. Maybe contained in 1.'s output already)
                        [--type type_regexp]
                        [--name name_regexp]
                        [--max-results limit]
                    Return a list containing the names and types for all global variables taken from the debug information. The variables are grouped by source file, and shown with the line number on which each variable is defined.
             */
                ReferenceTypeImpl referenceType = command.readReferenceType();
                int size = command.readInt();

                // -symbol-info-variables --name HelloField::
                String className = System.getProperty("program.class");
                System.out.println("Queueing MI command to list global variables (only names) for "+ className);
                MICommand cmd = gc.getCommandFactory().createMiSymbolInfoVariables("", className+"::", 0, false);
                int tokenID = JDWP.getNewTokenId();
                gc.queueCommand(tokenID, cmd);

                MiSymbolInfoVariablesInfo replyloc = (MiSymbolInfoVariablesInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                if (replyloc.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.pkt.errorCode = JDWP.Error.INTERNAL;
                }

                MiSymbolInfoVariablesInfo.SymbolVariableInfo[] files = replyloc.getSymbolVariables();
                for ( MiSymbolInfoVariablesInfo.SymbolVariableInfo file: files) { // usually only one file in the array due to className filter for GDB command
                        MiSymbolInfoVariablesInfo.Symbols[] vals = file.getSymbols();
                        answer.writeInt(size);
                        for (int i = 0; i < size; i++) {
                            FieldImpl field = referenceType.fieldById(command.readFieldRef());
                            ValueImpl value = referenceType.getValue(field);
                            MiSymbolInfoVariablesInfo.Symbols val = containsInVMFields(vals, field.name());
                            if (val != null) {
                                answer.writeValue(value); // TODO Hack
                            } else {
                                String tag = val.getType();
                                // TODO get value of static field from GDB
                            }
                        }
                }
            }

            private MiSymbolInfoVariablesInfo.Symbols containsInVMFields(MiSymbolInfoVariablesInfo.Symbols[] vals, String name) {
                for (MiSymbolInfoVariablesInfo.Symbols val : vals) {
                    String varName = val.getName().split("::")[1]; //Fib.Fib::res
                    if (varName.equals(name)) {
                        return val;
                    }
                }
                return null;
            }
        }

        /**
         * Returns the name of source file in which a reference type was
         * declared.
         */
        static class SourceFile implements Command  {
            static final int COMMAND = 7;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                long id = command.readObjectRef();
                ReferenceTypeImpl type = gc.vm.getReferenceTypeById(id);
                try {
                    answer.writeString(type.baseSourceName());
                } catch (AbsentInformationException e) {
                    answer.pkt.errorCode = JDWP.Error.ABSENT_INFORMATION;
                }
            }
        }

        /**
         * Returns the classes and interfaces directly nested within this type.
         * Types further nested within those types are not included.
         */
        static class NestedTypes implements Command  {
            static final int COMMAND = 8;

            static class TypeInfo {

                public static void write(ReferenceTypeImpl type, GDBControl gc, PacketStream answer) {
                    answer.writeByte(type.tag());
                    answer.writeClassRef(type.uniqueID());
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = command.readReferenceType();
                List<ReferenceTypeImpl> nestedTypes = type.nestedTypes();
                answer.writeInt(nestedTypes.size());
                for (ReferenceTypeImpl nestedType : nestedTypes) {
                    TypeInfo.write(nestedType, gc, answer);
                }
            }
        }

        /**
         * Returns the current status of the reference type. The status
         * indicates the extent to which the reference type has been
         * initialized, as described in section 2.1.6 of
         * <cite>The Java&trade; Virtual Machine Specification</cite>.
         * If the class is linked the PREPARED and VERIFIED bits in the returned status bits
         * will be set. If the class is initialized the INITIALIZED bit in the returned
         * status bits will be set. If an error occured during initialization then the
         * ERROR bit in the returned status bits will be set.
         * The returned status bits are undefined for array types and for
         * primitive classes (such as java.lang.Integer.TYPE).
         */
        static class Status implements Command  {
            static final int COMMAND = 9;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = command.readReferenceType();
                answer.writeInt(type.ref().getClassStatus());
            }
        }

        /**
         * Returns the interfaces declared as implemented by this class.
         * Interfaces indirectly implemented (extended by the implemented
         * interface or implemented by a superclass) are not included.
         */
        static class Interfaces implements Command  {
            static final int COMMAND = 10;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                long id = command.readObjectRef();
                ReferenceTypeImpl type = gc.vm.getReferenceTypeById(id);
                List<InterfaceTypeImpl> interfaces;
                if (type instanceof ClassTypeImpl) {
                    interfaces = ((ClassTypeImpl) type).interfaces();
                }
                else if (type instanceof InterfaceTypeImpl) {
                    interfaces = ((InterfaceTypeImpl) type).superinterfaces();
                }
                else {
                    answer.pkt.errorCode = JDWP.Error.INVALID_CLASS;
                    return;
                }
                answer.writeInt(interfaces.size());
                for (InterfaceTypeImpl iface : interfaces) {
                    answer.writeClassRef(iface.uniqueID());
                }
            }
        }

        /**
         * Returns the class object corresponding to this type.
         */
        static class ClassObject implements Command  {
            static final int COMMAND = 11;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = command.readReferenceType();
                answer.writeClassObjectReference(type.classObject());
            }
        }

        /**
         * Returns the value of the SourceDebugExtension attribute.
         * Since JDWP version 1.4. Requires canGetSourceDebugExtension capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class SourceDebugExtension implements Command  {
            static final int COMMAND = 12;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = command.readReferenceType();
                try {
                    answer.writeString(type.sourceDebugExtension());
                } catch (AbsentInformationException e) {
                    answer.pkt.errorCode = JDWP.Error.ABSENT_INFORMATION;
                }
            }
        }

        /**
         * Returns the JNI signature of a reference type along with the
         * generic signature if there is one.
         * Generic signatures are described in the signature attribute
         * section in
         * <cite>The Java&trade; Virtual Machine Specification</cite>.
         * Since JDWP version 1.5.
         * <p>
         */
        static class SignatureWithGeneric implements Command  {
            static final int COMMAND = 13;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                long uniqueID = command.readObjectRef();
                ReferenceTypeImpl type = gc.vm.getReferenceTypeById(uniqueID);
                answer.writeString(type.signature());
                answer.writeStringOrEmpty(type.genericSignature());
            }
        }

        /**
         * Returns information, including the generic signature if any,
         * for each field in a reference type.
         * Inherited fields are not included.
         * The field list will include any synthetic fields created
         * by the compiler.
         * Fields are returned in the order they occur in the class file.
         * Generic signatures are described in the signature attribute
         * section in
         * <cite>The Java&trade; Virtual Machine Specification</cite>.
         * Since JDWP version 1.5.
         */
        static class FieldsWithGeneric implements Command  {
            static final int COMMAND = 14;

            static class FieldInfo {

                public static void write(FieldImpl field, GDBControl gc, PacketStream answer) {
                    answer.writeFieldRef(field.uniqueID());
                    answer.writeString(field.name());
                    answer.writeString(field.signature());
                    answer.writeStringOrEmpty(field.genericSignature());
                    answer.writeInt(field.modifiers());
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                long id = command.readObjectRef();
                ReferenceTypeImpl type = gc.vm.getReferenceTypeById(id);
                List<FieldImpl> fields = type.fields();
                answer.writeInt(fields.size());
                for (FieldImpl field : fields) {
                    FieldInfo.write(field, gc, answer);
                }
            }
        }

        /**
         * Returns information, including the generic signature if any,
         * for each method in a reference type.
         * Inherited methods are not included. The list of methods will
         * include constructors (identified with the name "&lt;init&gt;"),
         * the initialization method (identified with the name "&lt;clinit&gt;")
         * if present, and any synthetic methods created by the compiler.
         * Methods are returned in the order they occur in the class file.
         * Generic signatures are described in the signature attribute
         * section in
         * <cite>The Java&trade; Virtual Machine Specification</cite>.
         * Since JDWP version 1.5.
         */
        static class MethodsWithGeneric implements Command  {
            static final int COMMAND = 15;

            static class MethodInfo {

                public static void write(MethodImpl method, GDBControl gc, PacketStream answer) {
                    answer.writeMethodRef(method.uniqueID());
                    answer.writeString(method.name());
                    answer.writeString(method.signature());
                    answer.writeStringOrEmpty(method.genericSignature());
                    answer.writeInt(method.modifiers());
                }
            }

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = command.readReferenceType();
                List<MethodImpl> methods = type.methods();
                answer.writeInt(methods.size());
                for (MethodImpl method : methods) {
                    MethodInfo.write(method, gc, answer);
                }
            }
        }

        /**
         * Returns instances of this reference type.
         * Only instances that are reachable for the purposes of
         * garbage collection are returned.
         * <p>Since JDWP version 1.6. Requires canGetInstanceInfo capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         */
        static class Instances implements Command  {
            static final int COMMAND = 16;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = command.readReferenceType();
                List<ObjectReferenceImpl> instances = type.instances(command.readInt());
                answer.writeInt(instances.size());
                for (ObjectReferenceImpl instance : instances) {
                    answer.writeTaggedObjectReference(instance);
                }
            }
        }

        /**
         * Returns the class file major and minor version numbers, as defined in the class
         * file format of the Java Virtual Machine specification.
         * <p>Since JDWP version 1.6.
         */
        static class ClassFileVersion implements Command  {
            static final int COMMAND = 17;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = command.readReferenceType();
                answer.writeInt(type.majorVersion());
                answer.writeInt(type.minorVersion());
            }
        }

        /**
         * Return the raw bytes of the constant pool in the format of the
         * constant_pool item of the Class File Format in
         * <cite>The Java&trade; Virtual Machine Specification</cite>.
         * <p>Since JDWP version 1.6. Requires canGetConstantPool capability - see
         * <a href="#JDWP_VirtualMachine_CapabilitiesNew">CapabilitiesNew</a>.
         *
         */
        static class ConstantPool implements Command  {
            static final int COMMAND = 18;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ReferenceTypeImpl type = command.readReferenceType();
                byte[] bytes = type.constantPool();
                answer.writeInt(type.constantPoolCount());
                answer.writeInt(bytes.length);
                answer.writeByteArray(bytes);
            }
        }

        /**
         * Returns the module that this reference type belongs to.
         * <p>Since JDWP version 9.
         */
        static class Module implements Command  {
            static final int COMMAND = 19;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
            }
        }
    }
}
