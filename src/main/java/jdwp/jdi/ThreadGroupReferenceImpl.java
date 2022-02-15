/*
 * Copyright (c) 2002, 2003, Oracle and/or its affiliates. All rights reserved.
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
import sun.jvm.hotspot.oops.Instance;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.oops.OopUtilities;
import sun.jvm.hotspot.runtime.JavaThread;

import java.util.ArrayList;
import java.util.List;

public class ThreadGroupReferenceImpl extends ObjectReferenceImpl {
    ThreadGroupReferenceImpl(ReferenceTypeImpl type, Oop oRef) {
        super(type, oRef);
    }

    public String name() {
        return OopUtilities.threadGroupOopGetName(ref());
    }

    public ThreadGroupReferenceImpl parent() {
        return vm().threadGroupMirror(
               (Instance)OopUtilities.threadGroupOopGetParent(ref()));
    }

    public List<ThreadReferenceImpl> threads() {
        // Each element of this array is the Oop for a thread;
        // NOTE it is not the JavaThread that we need to create
        // a ThreadReferenceImpl.
        Oop[] myThreads = OopUtilities.threadGroupOopGetThreads(ref());

        ArrayList<ThreadReferenceImpl> myList = new ArrayList<ThreadReferenceImpl>(myThreads.length);
        for (Oop myThread : myThreads) {
            JavaThread jt = OopUtilities.threadOopGetJavaThread(myThread);
            if (jt != null) {
                myList.add(vm().threadMirror(jt));
            }
        }
        return myList;
    }

    public List<ThreadGroupReferenceImpl> threadGroups() {
        Oop[] myGroups = OopUtilities.threadGroupOopGetGroups(ref());
        ArrayList<ThreadGroupReferenceImpl> myList = new ArrayList<ThreadGroupReferenceImpl>(myGroups.length);
        for (Oop myGroup : myGroups) {
            myList.add(vm().threadGroupMirror((Instance) myGroup));
        }
        return myList;
    }

    public String toString() {
        return "instance of " + referenceType().name() +
               "(name='" + name() + "', " + "id=" + uniqueID() + ")";
    }

    @Override
    byte typeValueKey() {
        return JDWP.Tag.THREAD_GROUP;
    }
}
