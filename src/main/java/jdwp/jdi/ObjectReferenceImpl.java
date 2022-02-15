/*
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
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
import jdwp.PacketStream;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.runtime.JavaThread;
import sun.jvm.hotspot.runtime.JavaVFrame;
import sun.jvm.hotspot.runtime.MonitorInfo;
import sun.jvm.hotspot.runtime.ObjectMonitor;
import sun.jvm.hotspot.utilities.Assert;

import java.util.*;

public class ObjectReferenceImpl extends ValueImpl {
    protected final ReferenceTypeImpl referenceType;
    private final Oop saObject;
    private boolean monitorInfoCached = false;
    private ThreadReferenceImpl owningThread = null;
    private List<ThreadReferenceImpl> waitingThreads = null;
    private int entryCount = 0;

    ObjectReferenceImpl(ReferenceTypeImpl type, Oop oRef) {
        referenceType = type;
        saObject = oRef;
    }

    protected Oop ref() {
        return saObject;
    }

    VirtualMachineImpl vm() {
        return referenceType.vm;
    }

    public ReferenceTypeImpl referenceType() {
        return referenceType;
    }

    interface HandleVisitor {
        boolean visit(OopHandle handle);
    }

    protected void visitReferences(HandleVisitor visitor) {
        ReferenceTypeImpl referenceType = referenceType();
        Instance typeMirror = referenceType.getJavaMirror();
        for (FieldImpl field : referenceType.allFields()) {
            if (field.ref().getFieldType().isOop()) {
                OopHandle valueHandle = ((OopField) field.ref()).getValueAsOopHandle(field.isStatic() ? typeMirror : saObject);
                if (valueHandle != null && visitor.visit(valueHandle)) {
                    return;
                }
            }
        }
    }

    public ValueImpl getValue(FieldImpl field) {
        if (field.isStatic()) {
            return referenceType.getValue(field);
        } else {
            // Make sure the field is valid
            referenceType.validateFieldAccess(field);
            return field.getValue(saObject);
        }
    }

    static long uniqueID(OopHandle handle, VirtualMachineImpl vm) {
        return vm.getAddressValue(handle);
    }

    public long uniqueID() {
        return uniqueID(saObject.getHandle(), vm());
    }

    public List<ThreadReferenceImpl> waitingThreads() {
        if (!vm().canGetMonitorInfo()) {
            throw new UnsupportedOperationException();
        }

        if (! monitorInfoCached) {
            computeMonitorInfo();
        }
        return waitingThreads;
    }


    public ThreadReferenceImpl owningThread() {
        if (!vm().canGetMonitorInfo()) {
            throw new UnsupportedOperationException();
        }

        if (! monitorInfoCached) {
            computeMonitorInfo();
        }
        return owningThread;
    }


    public int entryCount() {
        if (!vm().canGetMonitorInfo()) {
            throw new UnsupportedOperationException();
        }

        if (! monitorInfoCached) {
            computeMonitorInfo();
        }
        return entryCount;
    }

    // new method since 1.6.
    // Real body will be supplied later.
    public List<ObjectReferenceImpl> referringObjects(long maxReferrers) {
        if (!vm().canGetInstanceInfo()) {
            throw new UnsupportedOperationException("target does not support getting instances");
        }
        if (maxReferrers < 0) {
            throw new IllegalArgumentException("maxReferrers is less than zero: " + maxReferrers);
        }
        final OopHandle thisHandle = saObject.getHandle();
        final List<ObjectReferenceImpl> objects = new ArrayList<ObjectReferenceImpl>(0);
        final long max = maxReferrers;
        vm().saObjectHeap().iterate(new DefaultHeapVisitor() {
            private long refCount = 0;

            public boolean doObj(Oop oop) {
                try {
                    final ObjectReferenceImpl objref = vm().objectMirror(oop);
                    objref.visitReferences(new HandleVisitor() {
                        @Override
                        public boolean visit(OopHandle handle) {
                            if (thisHandle.equals(handle)) {
                                objects.add(objref);
                                refCount++;
                                return true;
                            }
                            return false;
                        }
                    });
                    if (max > 0 && refCount >= max) {
                        return true;
                    }
                } catch (RuntimeException x) {
                    // Ignore RuntimeException thrown from vm().objectMirror(oop)
                    // for bad oop. It is possible to see some bad oop
                    // because heap might be iterating at no safepoint.
                }
                return false;

            }
        });
        return objects;
    }

    // refer to JvmtiEnvBase::count_locked_objects.
    // Count the number of objects for a lightweight monitor. The obj
    // parameter is object that owns the monitor so this routine will
    // count the number of times the same object was locked by frames
    // in JavaThread. i.e., we count total number of times the same
    // object is (lightweight) locked by given thread.
    private int countLockedObjects(JavaThread jt, Oop obj) {
        int res = 0;
        JavaVFrame frame = jt.getLastJavaVFrameDbg();
        while (frame != null) {
            OopHandle givenHandle = obj.getHandle();
            for (Object monitor : JvmUtils.getFrameMonitors(frame)) {
                MonitorInfo mi = (MonitorInfo) monitor;
                if (mi.eliminated() && frame.isCompiledFrame()) continue; // skip eliminated monitor
                if (givenHandle.equals(mi.owner())) {
                    res++;
                }
            }
            frame = JvmUtils.getFrameJavaSender(frame);
        }
        return res;
    }

    // wrappers on same named method of Threads class
    // returns List<JavaThread>
    private List getPendingThreads(ObjectMonitor mon) {
        return vm().saVM().getThreads().getPendingThreads(mon);
    }

    // returns List<JavaThread>
    private List getWaitingThreads(ObjectMonitor mon) {
        return vm().saVM().getThreads().getWaitingThreads(mon);
    }

    private JavaThread owningThreadFromMonitor(Address addr) {
        return vm().saVM().getThreads().owningThreadFromMonitor(addr);
    }

    // refer to JvmtiEnv::GetObjectMonitorUsage
    private void computeMonitorInfo() {
        monitorInfoCached = true;
        Mark mark = saObject.getMark();
        ObjectMonitor mon = null;
        Address owner = null;
        // check for heavyweight monitor
        if (! mark.hasMonitor()) {
            // check for lightweight monitor
            if (mark.hasLocker()) {
                owner = mark.locker().getAddress(); // save the address of the Lock word
            }
            // implied else: no owner
        } else {
            // this object has a heavyweight monitor
            mon = mark.monitor();

            // The owner field of a heavyweight monitor may be NULL for no
            // owner, a JavaThread * or it may still be the address of the
            // Lock word in a JavaThread's stack. A monitor can be inflated
            // by a non-owning JavaThread, but only the owning JavaThread
            // can change the owner field from the Lock word to the
            // JavaThread * and it may not have done that yet.
            owner = mon.owner();
        }

        // find the owning thread
        if (owner != null) {
            owningThread = vm().threadMirror(owningThreadFromMonitor(owner));
        }

        // compute entryCount
        if (owningThread != null) {
            if (owningThread.getJavaThread().getAddress().equals(owner)) {
                // the owner field is the JavaThread *
                if (Assert.ASSERTS_ENABLED) {
                    Assert.that(false, "must have heavyweight monitor with JavaThread * owner");
                }
                entryCount = (int) mark.monitor().recursions() + 1;
            } else {
                // The owner field is the Lock word on the JavaThread's stack
                // so the recursions field is not valid. We have to count the
                // number of recursive monitor entries the hard way.
                entryCount = countLockedObjects(owningThread.getJavaThread(), saObject);
            }
        }

        // find the contenders & waiters
        waitingThreads = new ArrayList<ThreadReferenceImpl>();
        if (mon != null) {
            // this object has a heavyweight monitor. threads could
            // be contenders or waiters
            // add all contenders
            List pendingThreads = getPendingThreads(mon);
            // convert the JavaThreads to ThreadReferenceImpls
            for (Object pendingThread : pendingThreads) {
                waitingThreads.add(vm().threadMirror((JavaThread) pendingThread));
            }

            // add all waiters (threads in Object.wait())
            // note that we don't do this JVMTI way. To do it JVMTI way,
            // we would need to access ObjectWaiter list maintained in
            // ObjectMonitor::_queue. But we don't have this struct exposed
            // in vmStructs. We do waiters list in a way similar to getting
            // pending threads list
            List objWaitingThreads = getWaitingThreads(mon);
            // convert the JavaThreads to ThreadReferenceImpls
            for (Object objWaitingThread : objWaitingThreads) {
                waitingThreads.add(vm().threadMirror((JavaThread) objWaitingThread));
            }
        }
    }

    public boolean equals(Object obj) {
        if ((obj instanceof ObjectReferenceImpl)) {
            ObjectReferenceImpl other = (ObjectReferenceImpl)obj;
            return (ref().equals(other.ref())) &&
                   super.equals(obj);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return saObject.hashCode();
    }

    public String toString() {
        return  "instance of " + referenceType().name() + "(id=" + uniqueID() + ")";
    }

    @Override
    byte typeValueKey() {
        return JDWP.Tag.OBJECT;
    }

    @Override
    public void writeUntaggedValue(PacketStream packetStream) {
        packetStream.writeObjectRef(uniqueID());
    }
}
