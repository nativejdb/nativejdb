/*
 * Copyright (c) 2002, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
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
 */

package jdwp.jdi;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.runtime.ClassConstants;
import sun.jvm.hotspot.tools.jcore.ClassWriter;
import sun.jvm.hotspot.utilities.Assert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.*;

public abstract class ReferenceTypeImpl extends TypeImpl {
    private final Klass       saKlass;          // This can be an InstanceKlass or an ArrayKlass
    private Instance javaMirror;
    private int           modifiers = -1;
    private String        signature = null;
    private String        typeName;
    private SoftReference<SDE> sdeRef = null;
    private SoftReference<List<FieldImpl>> fieldsCache;
    private SoftReference<List<FieldImpl>> allFieldsCache;
    private SoftReference<List<MethodImpl>> methodsCache;
    private SoftReference<List<ReferenceTypeImpl>> nestedTypesCache;
    private SoftReference<List<MethodImpl>> methodInvokesCache;
    protected final VirtualMachineImpl vm;


    /* to mark when no info available */
    static final SDE NO_SDE_INFO_MARK = new SDE();

    protected ReferenceTypeImpl(VirtualMachineImpl aVm, Klass klass) {
        vm = aVm;
        saKlass = klass;
    }

    public abstract byte tag();

    public String name() {
        if (typeName == null) {
            Symbol typeNameSymbol = saKlass.getName();
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(typeNameSymbol != null, "null type name for a Klass");
            }
            typeName = typeNameSymbol.asString();
        }
        return typeName;
    }

    MethodImpl getMethodMirror(sun.jvm.hotspot.oops.Method ref) {
        // SA creates new Method objects when they are referenced which means
        // that the incoming object might not be the same object as on our
        // even though it is the same method. So do an address compare by
        // calling equals rather than just reference compare.
        for (MethodImpl method : methods()) {
            if (ref.equals(method.ref())) {
                return method;
            }
        }
        Klass methodHolder = CompatibilityHelper.INSTANCE.getMethodHolder(ref);
        if (methodHolder.equals(CompatibilityHelper.INSTANCE.getMethodHandleKlass())) {
          // invoke methods are generated as needed, so make mirrors as needed
          List<MethodImpl> mis;
          if (methodInvokesCache == null) {
            mis = new ArrayList<MethodImpl>();
            methodInvokesCache = new SoftReference<List<MethodImpl>>(mis);
          } else {
            mis = methodInvokesCache.get();
          }
            for (MethodImpl method : mis) {
                if (ref.equals(method.ref())) {
                    return method;
                }
            }

          MethodImpl method = MethodImpl.createMethodImpl(this, ref);
          mis.add(method);
          return method;
        }
        throw new IllegalArgumentException("Invalid method: "
                + methodHolder.getName().asString() + " "
                + ref.getName().asString() + " "
                + ref.getSignature().asString());
    }

    public boolean equals(Object obj) {
        if ((obj instanceof ReferenceTypeImpl)) {
            return ref().equals(((ReferenceTypeImpl)obj).ref());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return saKlass.hashCode();
    }

    public int compareTo(ReferenceTypeImpl other) {
        /*
         * Note that it is critical that compareTo() == 0
         * implies that equals() == true. Otherwise, TreeSet
         * will collapse classes.
         *
         * (Classes of the same name loaded by different class loaders
         * or in different VMs must not return 0).
         */
        int comp = name().compareTo(other.name());
        if (comp == 0) {
            Klass rf1 = ref();
            Klass rf2 = other.ref();
            // optimize for typical case: refs equal and VMs equal
            if (rf1.equals(rf2)) {
                // sequenceNumbers are always positive
                comp = vm.sequenceNumber - other.vm.sequenceNumber;
            } else {
                comp = CompatibilityHelper.INSTANCE.getAddress(rf1).minus(CompatibilityHelper.INSTANCE.getAddress(rf2)) < 0? -1 : 1;
            }
        }
        return comp;
    }

    public String signature() {
        if (signature == null) {
            signature = saKlass.signature();
        }
        return signature;
    }

    // refer to JvmtiEnv::GetClassSignature.
    // null is returned for array klasses.
    public String genericSignature() {
        if (saKlass instanceof ArrayKlass) {
            return null;
        } else {
            Symbol genSig = ((InstanceKlass)saKlass).getGenericSignature();
            return (genSig != null)? genSig.asString() : null;
        }
    }

    public ClassLoaderReferenceImpl classLoader() {
      @SuppressWarnings("RedundantCast")
      Instance xx = (Instance)(((InstanceKlass)saKlass).getClassLoader());
      return vm.classLoaderMirror(xx);
    }

    public boolean isAbstract() {
        return((modifiers() & ClassConstants.JVM_ACC_ABSTRACT) != 0);
    }

    public boolean isPrepared() {
        return (saKlass.getClassStatus() & JVMDIClassStatus.PREPARED) != 0;
    }

    final void checkPrepared() throws ClassNotPreparedException {
        if (! isPrepared()) {
            throw new ClassNotPreparedException();
        }
    }

    private boolean isThrowableBacktraceField(sun.jvm.hotspot.oops.Field fld) {
        // refer to JvmtiEnv::GetClassFields in jvmtiEnv.cpp.
        // We want to filter out java.lang.Throwable.backtrace (see 4446677).
        // It contains some Method*s that aren't quite real Objects.
        return JvmUtils.nameEquals(fld.getFieldHolder().getName(), vm.javaLangThrowable) &&
                fld.getID().getName().equals("backtrace");
    }

    public final FieldImpl fieldById(long id) throws ClassNotPreparedException {
        for (FieldImpl field : allFields()) {
            if (field.uniqueID() == id) {
                return field;
            }
        }
        throw new IllegalStateException("Field with id " + id + " not found in " + name());
    }

    public final List<FieldImpl> fields() throws ClassNotPreparedException {
        List<FieldImpl> fields = (fieldsCache != null)? fieldsCache.get() : null;
        if (fields == null) {
            checkPrepared();
            if (saKlass instanceof ArrayKlass) {
                fields = Collections.emptyList();
            } else {
                // Get a list of the sa Field types
                List saFields = ((InstanceKlass)saKlass).getImmediateFields();

                // Create a list of our Field types
                int len = saFields.size();
                fields = new ArrayList<FieldImpl>(len);
                for (Object saField : saFields) {
                    sun.jvm.hotspot.oops.Field curField = (sun.jvm.hotspot.oops.Field) saField;
                    if (!isThrowableBacktraceField(curField)) {
                        fields.add(new FieldImpl(this, curField));
                    }
                }
            }
            fields = Collections.unmodifiableList(fields);
            fieldsCache = new SoftReference<List<FieldImpl>>(fields);
        }
        return fields;
    }

    public final List<FieldImpl> allFields() throws ClassNotPreparedException {
        List<FieldImpl> allFields = (allFieldsCache != null)? allFieldsCache.get() : null;
        if (allFields == null) {
            checkPrepared();
            if (saKlass instanceof ArrayKlass) {
                // is 'length' a field of array klasses? To maintain
                // consistency with JVMDI-JDI we return 0 size.
                allFields = Collections.emptyList();
            } else {

                // Get a list of the sa Field types

                // getAllFields() is buggy and does not return all super classes
//                saFields = ((InstanceKlass)saKlass).getAllFields();

                InstanceKlass saKlass = (InstanceKlass) this.saKlass;
                List saFields = saKlass.getImmediateFields();

                // transitiveInterfaces contains all interfaces implemented
                // by this class and its superclass chain with no duplicates.

                for (InstanceKlass intf1 : CompatibilityHelper.INSTANCE.getTransitiveInterfaces(saKlass)) {
                    if (Assert.ASSERTS_ENABLED) {
                        Assert.that(intf1.isInterface(), "just checking type");
                    }
                    saFields.addAll(intf1.getImmediateFields());
                }

                // Get all fields in the superclass, recursively.  But, don't
                // include fields in interfaces implemented by superclasses;
                // we already have all those.
                if (!saKlass.isInterface()) {
                    InstanceKlass supr = saKlass;
                    while  ( (supr = (InstanceKlass) supr.getSuper()) != null) {
                        saFields.addAll(supr.getImmediateFields());
                    }
                }

                // Create a list of our Field types
                allFields = new ArrayList<FieldImpl>(saFields.size());
                for (Object saField : saFields) {
                    sun.jvm.hotspot.oops.Field curField = (sun.jvm.hotspot.oops.Field) saField;
                    if (!isThrowableBacktraceField(curField)) {
                        allFields.add(new FieldImpl(vm.referenceType(curField.getFieldHolder()), curField));
                    }
                }
            }
            allFields = Collections.unmodifiableList(allFields);
            allFieldsCache = new SoftReference<List<FieldImpl>>(allFields);
        }
        return allFields;
    }

    public final MethodImpl methodById(long id) throws ClassNotPreparedException {
        for (MethodImpl method : methods()) {
            if (method.uniqueID() == id) {
                return method;
            }
        }
        throw new IllegalStateException("Method with id " + id + " not found in " + name());
    }

    public final List<MethodImpl> methods() throws ClassNotPreparedException {
        List<MethodImpl> methods = (methodsCache != null)? methodsCache.get() : null;
        if (methods == null) {
            checkPrepared();
            if (saKlass instanceof ArrayKlass) {
                methods = Collections.emptyList();
            } else {
                // Get a list of the SA Method types
                List saMethods = ((InstanceKlass)saKlass).getImmediateMethods();

                // Create a list of our MethodImpl types
                int len = saMethods.size();
                methods = new ArrayList<MethodImpl>(len);
                for (Object saMethod : saMethods) {
                    methods.add(MethodImpl.createMethodImpl(this, (sun.jvm.hotspot.oops.Method) saMethod));
                }
            }
            methods = Collections.unmodifiableList(methods);
            methodsCache = new SoftReference<List<MethodImpl>>(methods);
        }
        return methods;
    }

    List<InterfaceTypeImpl> getInterfaces() {
        if (saKlass instanceof ArrayKlass) {
            // Actually, JLS says arrays implement Cloneable and Serializable
            // But, JVMDI-JDI just returns 0 interfaces for arrays. We follow
            // the same for consistency.
            return Collections.emptyList();
        }

        // Get a list of the sa InstanceKlass types
        List saInterfaces = ((InstanceKlass)saKlass).getDirectImplementedInterfaces();

        // Create a list of our InterfaceTypes
        List<InterfaceTypeImpl> myInterfaces = new ArrayList<InterfaceTypeImpl>(saInterfaces.size());
        for (Object saInterface : saInterfaces) {
            myInterfaces.add((InterfaceTypeImpl) vm.referenceType((Klass) saInterface));
        }
        return myInterfaces;
    }

    public final List<ReferenceTypeImpl> nestedTypes() {
        List<ReferenceTypeImpl> nestedTypes = (nestedTypesCache != null)? nestedTypesCache.get() : null;
        if (nestedTypes == null) {
            if (saKlass instanceof ArrayKlass) {
                nestedTypes = Collections.emptyList();
            } else {
                ClassLoaderReferenceImpl cl = classLoader();
                List<ReferenceTypeImpl> classes;
                if (cl != null) {
                   classes = cl.visibleClasses();
                } else {
                   classes = vm.bootstrapClasses();
                }
                nestedTypes = new ArrayList<ReferenceTypeImpl>();
                for (ReferenceTypeImpl refType : classes) {
                    Symbol candidateName = refType.ref().getName();
                    if (((InstanceKlass) saKlass).isInnerOrLocalClassName(candidateName)) {
                        nestedTypes.add(refType);
                    }
                }
            }
            nestedTypes = Collections.unmodifiableList(nestedTypes);
            nestedTypesCache = new SoftReference<List<ReferenceTypeImpl>>(nestedTypes);
        }
        return nestedTypes;
    }

    public ValueImpl getValue(FieldImpl field) {
        validateFieldAccess(field);
        // Do more validation specific to ReferenceType field getting
        if (!field.isStatic()) {
            throw new IllegalArgumentException(
              "Attempt to use non-static field with ReferenceType: " +
                field.name());
        }

        return field.getValue();
    }

    void validateFieldAccess(FieldImpl field) {
       /*
        * Field must be in this object's class, a superclass, or
        * implemented interface
        */
        ReferenceTypeImpl declType = field.declaringType();
        if (!declType.isAssignableFrom(this)) {
            throw new IllegalArgumentException("Invalid field");
        }
    }

    Instance getJavaMirror() {
        if (javaMirror == null) {
            javaMirror = saKlass.getJavaMirror();
        }
        return javaMirror;
    }

    public ClassObjectReferenceImpl classObject() {
        return vm.classObjectMirror(getJavaMirror());
    }

    SDE.Stratum stratum(String stratumID) {
        SDE sde = sourceDebugExtensionInfo();
        if (!sde.isValid()) {
            sde = NO_SDE_INFO_MARK;
        }
        return sde.stratum(stratumID);
    }

    public String baseSourceName() throws AbsentInformationException {
      if (saKlass instanceof ArrayKlass) {
            throw new AbsentInformationException();
      }
      Symbol sym = ((InstanceKlass)saKlass).getSourceFileName();
      if (sym != null) {
          return sym.asString();
      } else {
          throw new AbsentInformationException();
      }
    }

    String baseSourcePath() throws AbsentInformationException {
        return baseSourceDir() + baseSourceName();
    }

    String baseSourceDir() {
        String typeName = name();
        StringBuilder sb = new StringBuilder(typeName.length() + 10);
        int index = 0;
        int nextIndex;

        while ((nextIndex = typeName.indexOf('.', index)) > 0) {
            sb.append(typeName, index, nextIndex);
            sb.append(File.separatorChar);
            index = nextIndex + 1;
        }
        return sb.toString();
    }

    public String sourceDebugExtension()
                           throws AbsentInformationException {
        if (!vm.canGetSourceDebugExtension()) {
            throw new UnsupportedOperationException();
        }
        SDE sde = sourceDebugExtensionInfo();
        if (sde == NO_SDE_INFO_MARK) {
            throw new AbsentInformationException();
        }
        return sde.sourceDebugExtension;
    }

    private SDE sourceDebugExtensionInfo() {
        if (!vm.canGetSourceDebugExtension()) {
            return NO_SDE_INFO_MARK;
        }
        SDE sde;
        sde = (sdeRef == null) ?  null : sdeRef.get();
        if (sde == null) {
           String extension = null;
           if (saKlass instanceof InstanceKlass) {
              extension = CompatibilityHelper.INSTANCE.getSourceDebugExtension((InstanceKlass)saKlass);
           }
           if (extension == null) {
              sde = NO_SDE_INFO_MARK;
           } else {
              sde = new SDE(extension);
           }
           sdeRef = new SoftReference<SDE>(sde);
        }
        return sde;
    }

    public final int modifiers() {
        if (modifiers == -1) {
            modifiers = getModifiers();
        }
        return modifiers;
    }

    // new method since 1.6.
    // Real body will be supplied later.
    public List<ObjectReferenceImpl> instances(long maxInstances) {
        if (!vm.canGetInstanceInfo()) {
            throw new UnsupportedOperationException(
                      "target does not support getting instances");
        }

        if (maxInstances < 0) {
            throw new IllegalArgumentException("maxInstances is less than zero: "
                                              + maxInstances);
        }

        if (isAbstract() || (this instanceof InterfaceTypeImpl)) {
            return Collections.emptyList();
        }
        final List<ObjectReferenceImpl> objects = new ArrayList<ObjectReferenceImpl>(0);

        final Address givenKls = CompatibilityHelper.INSTANCE.getAddress(saKlass);
        final long max = maxInstances;
        vm.saObjectHeap().iterate(new DefaultHeapVisitor() {
                private long instCount = 0;
                public boolean doObj(Oop oop) {
                    if (givenKls.equals(CompatibilityHelper.INSTANCE.getKlassAddress(oop))) {
                        objects.add(vm.objectMirror(oop));
                        instCount++;
                    }
                    return max > 0 && instCount >= max;
                }
            });
        return objects;
    }

    int getModifiers() {
        return (int) saKlass.getClassModifiers();
    }

    public Klass ref() {
        return saKlass;
    }

    /*
     * Return true if an instance of this type
     * can be assigned to a variable of the given type
     */
    abstract boolean isAssignableTo(ReferenceTypeImpl type);

    boolean isAssignableFrom(ReferenceTypeImpl type) {
        return type.isAssignableTo(this);
    }

    int indexOf(MethodImpl method) {
        // Make sure they're all here - the obsolete method
        // won't be found and so will have index -1
        return methods().indexOf(method);
    }

    private static boolean isPrimitiveArray(String signature) {
        int i = signature.lastIndexOf('[');
        /*
         * TO DO: Centralize JNI signature knowledge.
         *
         * Ref:
         *  jdk1.4/doc/guide/jpda/jdwp.jdi/com/sun/jdwp.jdi/doc-files/signature.html
         */
        boolean isPA;
        if (i < 0) {
            isPA = false;
        } else {
            char c = signature.charAt(i + 1);
            isPA = (c != 'L');
        }
        return isPA;
    }

    String loaderString() {
        if (classLoader() != null) {
            return "loaded by " + classLoader().toString();
        } else {
            return "loaded by bootstrap loader";
        }
    }

    public static long uniqueID(Klass klass, VirtualMachineImpl vm) {
        return vm.getAddressValue(CompatibilityHelper.INSTANCE.getAddress(klass));
    }

    public long uniqueID() {
        return uniqueID(saKlass, vm);
    }

    // new method since 1.6
    public int majorVersion() {
        if (!vm.canGetClassFileVersion()) {
            throw new UnsupportedOperationException("Cannot get class file version");
        }
        return (int)((InstanceKlass)saKlass).majorVersion();
    }

    // new method since 1.6
    public int minorVersion() {
        if (!vm.canGetClassFileVersion()) {
            throw new UnsupportedOperationException("Cannot get class file version");
        }
        return (int)((InstanceKlass)saKlass).minorVersion();
    }

    // new method since 1.6
    public int constantPoolCount() {
        if (!vm.canGetConstantPool()) {
            throw new UnsupportedOperationException("Cannot get constant pool");
        }
        if (saKlass instanceof ArrayKlass) {
            return 0;
        } else {
            return ((InstanceKlass)saKlass).getConstants().getLength();
        }
    }

    // new method since 1.6
    public byte[] constantPool() {
        if (!vm.canGetConstantPool()) {
            throw new UnsupportedOperationException("Cannot get constant pool");
        }
        if (this instanceof ArrayTypeImpl) {
            return new byte[0];
        } else {
            ByteArrayOutputStream bs = new ByteArrayOutputStream() {
                @Override
                public byte[] toByteArray() {
                    // drop constant_pool_count (first 2 bytes)
                    return Arrays.copyOfRange(buf, 2, count);
                }
            };
            try {
                new ClassWriter((InstanceKlass)saKlass, bs) {
                    @Override
                    public void writeConstantPool() throws IOException {
                        super.writeConstantPool();
                    }
                }.writeConstantPool();
            } catch (IOException ex) {
                ex.printStackTrace();
                return new byte[0];
            }
            return bs.toByteArray();
        }
    }
}
