/*
 * Copyright (c) 2002, 2011, Oracle and/or its affiliates. All rights reserved.
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
import sun.jvm.hotspot.oops.Method;
import sun.jvm.hotspot.oops.Symbol;

import java.util.List;

public abstract class MethodImpl extends TypeComponentImpl {
    Method saMethod;

    public abstract int argSlotCount();
    abstract List<LocationImpl> allLineLocations(SDE.Stratum stratum) throws AbsentInformationException;

    static MethodImpl createMethodImpl(ReferenceTypeImpl declaringType,
                                       Method saMethod) {
        // Someday might have to add concrete and non-concrete subclasses.
        if (saMethod.isNative() || saMethod.isAbstract()) {
            return new NonConcreteMethodImpl(declaringType, saMethod);
        } else {
            return new ConcreteMethodImpl(declaringType, saMethod);
        }
    }

    MethodImpl(ReferenceTypeImpl declaringType, Method saMethod) {
        super(declaringType);
        this.saMethod = saMethod;
        signature = saMethod.getSignature().asString();
    }

    // Object ref() {
    public Method ref() {
        return saMethod;
    }

    public long uniqueID() {
        return uniqueID(saMethod, vm());
    }

    public static long uniqueID(Method method, VirtualMachineImpl vm) {
        return vm.getAddressValue(CompatibilityHelper.INSTANCE.getAddress(method));
    }

    public String genericSignature() {
        Symbol genSig = saMethod.getGenericSignature();
        return (genSig != null)? genSig.asString() : null;
    }

    public boolean isNative() {
        return saMethod.isNative();
    }

    public boolean isObsolete() {
        return saMethod.isObsolete();
    }

    public final List<LocationImpl> allLineLocations() throws AbsentInformationException {
        return allLineLocations(declaringType.stratum(null));
    }

    LineInfo codeIndexToLineInfo(SDE.Stratum stratum,
                                 long codeIndex) {
        if (stratum.isJava()) {
            return new BaseLineInfo(-1, declaringType);
        } else {
            return new StratumLineInfo(stratum.id(), -1, null, null);
        }
    }

    public boolean equals(Object obj) {
        if ((obj instanceof MethodImpl)) {
            MethodImpl other = (MethodImpl)obj;
            return (declaringType().equals(other.declaringType())) &&
                (ref().equals(other.ref())) &&
                super.equals(obj);
        } else {
            return false;
        }
    }

    // From interface Comparable
    int compareTo(MethodImpl method) {
        ReferenceTypeImpl declaringType = declaringType();
         int rc = declaringType.compareTo(method.declaringType());
         if (rc == 0) {
           rc = declaringType.indexOf(this) -
               declaringType.indexOf(method);
         }
         return rc;
    }

    // from interface Mirror
    public String toString() {
        return declaringType().name() + "." + name() + signature;
    }

    public String name() {
        Symbol myName = saMethod.getName();
        return myName.asString();
    }

    public int modifiers() {
        return saMethod.getAccessFlagsObj().getStandardFlags();
    }

    public boolean isStatic() {
        return saMethod.isStatic();
    }

    public int hashCode() {
        return saMethod.hashCode();
    }

    abstract public List<LocalVariableImpl> variables() throws AbsentInformationException;

    abstract public byte[] bytecodes();
}
