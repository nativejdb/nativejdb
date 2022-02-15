/*
 * Copyright (c) 2002, 2007, Oracle and/or its affiliates. All rights reserved.
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

import jdwp.JDWP;
import sun.jvm.hotspot.oops.InstanceKlass;

import java.lang.ref.SoftReference;
import java.util.*;

public class ClassTypeImpl extends ReferenceTypeImpl {
    private SoftReference<List<InterfaceTypeImpl>> interfacesCache    = null;

    ClassTypeImpl(VirtualMachineImpl aVm, InstanceKlass aRef) {
        super(aVm, aRef);
    }

    @Override
    public byte tag() {
        return JDWP.TypeTag.CLASS;
    }

    public ClassTypeImpl superclass() {
        InstanceKlass kk = (InstanceKlass)ref().getSuper();
        if (kk == null) {
            return null;
        }
        return (ClassTypeImpl) vm.referenceType(kk);
    }

    public List<InterfaceTypeImpl> interfaces()  {
        List<InterfaceTypeImpl> interfaces = (interfacesCache != null)? interfacesCache.get() : null;
        if (interfaces == null) {
            checkPrepared();
            interfaces = Collections.unmodifiableList(getInterfaces());
            interfacesCache = new SoftReference<List<InterfaceTypeImpl>>(interfaces);
        }
        return interfaces;
    }

    boolean isAssignableTo(ReferenceTypeImpl type) {
        if (this.equals(type)) {
            return true;
        }
        ClassTypeImpl superclazz = superclass();
        if ((superclazz != null) && superclazz.isAssignableTo(type)) {
            return true;
        } else {
            List<InterfaceTypeImpl> interfaces = interfaces();
            for (InterfaceTypeImpl anInterface : interfaces) {
                if (anInterface.isAssignableTo(type)) {
                    return true;
                }
            }
            return false;
        }
    }

    public String toString() {
       return "class " + name() + "(" + loaderString() + ")";
    }
}
