/*
 * Copyright (c) 2002, 2004, Oracle and/or its affiliates. All rights reserved.
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
 *
 */

package jdwp.jdi;

import jdwp.JDWP;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InterfaceType;
import com.sun.jdi.PrimitiveType;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.runtime.ClassConstants;

public class ArrayTypeImpl extends ReferenceTypeImpl {
    ArrayTypeImpl(VirtualMachineImpl aVm, ArrayKlass aRef) {
        super(aVm, aRef);
    }

    @Override
    public byte tag() {
        return JDWP.TypeTag.ARRAY;
    }

    public String componentSignature() {
        return signature().substring(1); // Just skip the leading '['
    }

    public ClassLoaderReferenceImpl classLoader() {
        if (ref() instanceof TypeArrayKlass) {
            // primitive array klasses are loaded by bootstrap loader
            return null;
        } else {
            Klass bottomKlass = ((ObjArrayKlass)ref()).getBottomKlass();
            if (bottomKlass instanceof TypeArrayKlass) {
                // multidimensional primitive array klasses are loaded by bootstrap loader
                return null;
            } else {
                // class loader of any other obj array klass is same as the loader
                // that loaded the bottom InstanceKlass
                @SuppressWarnings("RedundantCast")
                Instance xx = (Instance)(((InstanceKlass) bottomKlass).getClassLoader());
                return vm.classLoaderMirror(xx);
            }
        }
    }

    /*
     * Find the TypeImpl object, if any, of a component TypeImpl of this array.
     * The component TypeImpl does not have to be immediate; e.g. this method
     * can be used to find the component Foo of Foo[][].
     */
    public TypeImpl componentType() throws ClassNotLoadedException {
        ArrayKlass k = (ArrayKlass) ref();
        if (k instanceof ObjArrayKlass) {
            Klass elementKlass = ((ObjArrayKlass)k).getElementKlass();
            if (elementKlass == null) {
                throw new ClassNotLoadedException(componentSignature());
            } else {
                return vm.referenceType(elementKlass);
            }
        } else {
            // It's a primitive type
            return vm.primitiveTypeMirror(signature().charAt(1));
        }
    }

    static boolean isComponentAssignable(TypeImpl destination, TypeImpl source) {
        if (source instanceof PrimitiveType) {
            // Assignment of primitive arrays requires identical
            // component types.
            return source.equals(destination);
        } else {
           if (destination instanceof PrimitiveType) {
                return false;
            }

            ReferenceTypeImpl refSource = (ReferenceTypeImpl)source;
            ReferenceTypeImpl refDestination = (ReferenceTypeImpl)destination;
            // Assignment of object arrays requires availability
            // of widening conversion of component types
            return refSource.isAssignableTo(refDestination);
        }
    }


    /*
    * Return true if an instance of the  given reference type
    * can be assigned to a variable of this type
    */
    boolean isAssignableTo(ReferenceTypeImpl destType) {
        if (destType instanceof ArrayTypeImpl) {
            try {
                TypeImpl destComponentType = ((ArrayTypeImpl)destType).componentType();
                return isComponentAssignable(destComponentType, componentType());
            } catch (ClassNotLoadedException e) {
                // One or both component types has not yet been
                // loaded => can't assign
                return false;
            }
        } else {
            String typeName = destType.name();
            if (destType instanceof InterfaceType) {
                // Every array TypeImpl implements java.io.Serializable and
                // java.lang.Cloneable. fixme in JVMDI-JDI, includes only
                // Cloneable but not Serializable.
                return vm.javaLangCloneable.equals(typeName) || vm.javaIoSerializable.equals(typeName);
            } else {
                // Only valid ClassTypeImpl assignee is Object
                return vm.javaLangObject.equals(typeName);
            }
        }
    }

    int getModifiers() {
        /*
         * For object arrays, the return values for Interface
         * Accessible.isPrivate(), Accessible.isProtected(),
         * etc... are the same as would be returned for the
         * component type.  Fetch the modifier bits from the
         * component TypeImpl and use those.
         *
         * For primitive arrays, the modifiers are always
         *   VMModifiers.FINAL | VMModifiers.PUBLIC
         *
         * Reference com.sun.jdwp.jdi.Accessible.java.
         */
        try {
            TypeImpl t = componentType();
            if (t instanceof PrimitiveType) {
                return (int) (ClassConstants.JVM_ACC_FINAL | ClassConstants.JVM_ACC_PUBLIC);
            } else {
                ReferenceTypeImpl rt = (ReferenceTypeImpl)t;
                return rt.modifiers();
            }
        } catch (ClassNotLoadedException cnle) {
            cnle.printStackTrace();
        }
        return -1;
    }

    public String toString() {
       return "array class " + name() + " (" + loaderString() + ")";
    }

    /*
     * Save a pointless trip over the wire for these methods
     * which have undefined results for arrays.
     */
    public boolean isPrepared() { return true; }
    public boolean isAbstract() { return false; }

}
