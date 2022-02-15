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
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ThreadReference;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.oops.Instance;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.oops.OopUtilities;
import sun.jvm.hotspot.runtime.JavaThread;
import sun.jvm.hotspot.runtime.JavaVFrame;
import sun.jvm.hotspot.runtime.MonitorInfo;
import sun.jvm.hotspot.runtime.ObjectMonitor;
import sun.jvm.hotspot.utilities.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

//import sun.jvm.hotspot.runtime.StackFrameStream;

public class ThreadReferenceImpl extends ObjectReferenceImpl implements /* imports */ JVMTIThreadState {

    private final JavaThread myJavaThread;
    private ArrayList<StackFrameImpl> frames;    // StackFrames
    private List<ObjectReferenceImpl> ownedMonitors;
    private List<MonitorInfoImpl> ownedMonitorsInfo; // List<MonitorInfo>
    private ObjectReferenceImpl currentContendingMonitor;

    ThreadReferenceImpl(ReferenceTypeImpl type, Instance oRef) {
        // Instance must be of type java.lang.Thread
        super(type, oRef);

        // JavaThread retrieved from java.lang.Thread instance may be null.
        // This is the case for threads not-started and for zombies. Wherever
        // appropriate, check for null instead of resulting in NullPointerException.
        myJavaThread = OopUtilities.threadOopGetJavaThread(oRef);
    }

    // return value may be null. refer to the comment in constructor.
    JavaThread getJavaThread() {
        return myJavaThread;
    }

    protected String description() {
        return "ThreadReference " + uniqueID();
    }

    /**
     * Note that we only cache the name string while suspended because
     * it can change via Thread.setName arbitrarily
     */
    public String name() {
        return OopUtilities.threadOopGetName(ref());
    }

    public int suspendCount() {
        // all threads are "suspended" when we attach to process or core.
        // we interpret this as one suspend.
        return 1;
    }

    public int status() {
        int state = OopUtilities.threadOopGetThreadStatus(ref());
        // refer to map2jdwpThreadStatus in util.c (back-end)
        if ((state & JVMTI_THREAD_STATE_ALIVE) == 0) {
            if ((state & JVMTI_THREAD_STATE_TERMINATED) != 0) {
                return ThreadReference.THREAD_STATUS_ZOMBIE;
            } else {
                return ThreadReference.THREAD_STATUS_NOT_STARTED;
            }
        } else {
            if ((state & JVMTI_THREAD_STATE_SLEEPING) != 0) {
                return ThreadReference.THREAD_STATUS_SLEEPING;
            } else if ((state & JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER) != 0) {
                return ThreadReference.THREAD_STATUS_MONITOR;
            } else if ((state & JVMTI_THREAD_STATE_WAITING) != 0) {
                return ThreadReference.THREAD_STATUS_WAIT;
            } else if ((state & JVMTI_THREAD_STATE_RUNNABLE) != 0) {
                return ThreadReference.THREAD_STATUS_RUNNING;
            }
        }
        return ThreadReference.THREAD_STATUS_UNKNOWN;
    }

    public ThreadGroupReferenceImpl threadGroup() {
        return vm().threadGroupMirror((Instance)OopUtilities.threadOopGetThreadGroup(ref()));
    }

    public int frameCount() throws IncompatibleThreadStateException { //fixme jjh
        privateFrames(0, -1);
        return frames.size();
    }

    public List<StackFrameImpl> frames() throws IncompatibleThreadStateException  {
        return privateFrames(0, -1);
    }

    public StackFrameImpl frame(int index) throws IncompatibleThreadStateException  {
        return privateFrames(index, 1).get(0);
    }

    /**
     * Private version of frames() allows "-1" to specify all
     * remaining frames.
     */

    private List<StackFrameImpl> privateFrames(int start, int length)
                              throws IncompatibleThreadStateException  {
        if (myJavaThread == null) {
            // for zombies and yet-to-be-started threads we need to throw exception
            throw new IncompatibleThreadStateException();
        }
        if (frames == null) {
            frames = new ArrayList<StackFrameImpl>(10);
            JavaVFrame myvf = myJavaThread.getLastJavaVFrameDbg();
            int id = 0;
            while (myvf != null) {
                StackFrameImpl myFrame = new StackFrameImpl(this, myvf, id++);
                //fixme jjh null should be a Location
                frames.add(myFrame);
                myvf = JvmUtils.getFrameJavaSender(myvf);
            }
        }

        List<StackFrameImpl> retVal;
        if (frames.size() == 0) {
            retVal = Collections.emptyList();
        } else {
            int toIndex = start + length;
            if (length == -1) {
                toIndex = frames.size();
            }
            retVal = frames.subList(start, toIndex);
        }
        return Collections.unmodifiableList(retVal);
    }

    // refer to JvmtiEnvBase::get_owned_monitors
    public List<ObjectReferenceImpl> ownedMonitors()  throws IncompatibleThreadStateException {
        if (!vm().canGetOwnedMonitorInfo()) {
            throw new UnsupportedOperationException();
        }

        if (myJavaThread == null) {
           throw new IncompatibleThreadStateException();
        }

        if (ownedMonitors != null) {
            return ownedMonitors;
        }

        ownedMonitorsWithStackDepth();

        for (MonitorInfoImpl monitorInfo : ownedMonitorsInfo) {
            //FIXME : Change the MonitorInfoImpl cast to com.sun.jdwp.jdi.MonitorInfo
            //        when hotspot start building with jdk1.6.
            ownedMonitors.add(monitorInfo.monitor());
        }

        return ownedMonitors;
    }

    // new method since 1.6.
    // Real body will be supplied later.
    public List<MonitorInfoImpl> ownedMonitorsAndFrames() throws IncompatibleThreadStateException {
        if (!vm().canGetMonitorFrameInfo()) {
            throw new UnsupportedOperationException(
                "target does not support getting Monitor Frame Info");
        }

        if (myJavaThread == null) {
           throw new IncompatibleThreadStateException();
        }

        if (ownedMonitorsInfo != null) {
            return ownedMonitorsInfo;
        }

        ownedMonitorsWithStackDepth();
        return ownedMonitorsInfo;
    }

    private void ownedMonitorsWithStackDepth() {

        ownedMonitorsInfo = new ArrayList<MonitorInfoImpl>();
        List<OopHandle> lockedObjects = new ArrayList<OopHandle>(); // List<OopHandle>
        List<Integer> stackDepth = new ArrayList<Integer>(); // List<int>
        ObjectMonitor waitingMonitor = myJavaThread.getCurrentWaitingMonitor();
        ObjectMonitor pendingMonitor = myJavaThread.getCurrentPendingMonitor();
        OopHandle waitingObj = null;
        if (waitingMonitor != null) {
            // save object of current wait() call (if any) for later comparison
            waitingObj = waitingMonitor.object();
        }
        OopHandle pendingObj = null;
        if (pendingMonitor != null) {
            // save object of current enter() call (if any) for later comparison
            pendingObj = pendingMonitor.object();
        }

        JavaVFrame frame = myJavaThread.getLastJavaVFrameDbg();
        int depth=0;
        while (frame != null) {
            for (Object frameMonitor : JvmUtils.getFrameMonitors(frame)) {
                MonitorInfo mi = (MonitorInfo) frameMonitor;
                if (mi.eliminated() && frame.isCompiledFrame()) {
                    continue; // skip eliminated monitor
                }
                OopHandle obj = mi.owner();
                if (obj == null) {
                    // this monitor doesn't have an owning object so skip it
                    continue;
                }

                if (obj.equals(waitingObj)) {
                    // the thread is waiting on this monitor so it isn't really owned
                    continue;
                }

                if (obj.equals(pendingObj)) {
                    // the thread is pending on this monitor so it isn't really owned
                    continue;
                }

                boolean found = false;
                for (Object lockedObject : lockedObjects) {
                    // check for recursive locks
                    if (obj.equals(lockedObject)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    // already have this object so don't include it
                    continue;
                }
                // add the owning object to our list
                lockedObjects.add(obj);
                stackDepth.add(depth);
            }
            frame = JvmUtils.getFrameJavaSender(frame);
            depth++;
        }

        // now convert List<OopHandle> to List<ObjectReference>
        ObjectHeap heap = vm().saObjectHeap();
        Iterator<Integer> stk = stackDepth.iterator();
        for (OopHandle lockedObject : lockedObjects) {
            ownedMonitorsInfo.add(new MonitorInfoImpl(vm().objectMirror(lockedObject), this, stk.next()));
        }
    }

    // refer to JvmtiEnvBase::get_current_contended_monitor
    public ObjectReferenceImpl currentContendedMonitor()
                              throws IncompatibleThreadStateException  {
        if (!vm().canGetCurrentContendedMonitor()) {
            throw new UnsupportedOperationException();
        }

        if (myJavaThread == null) {
           throw new IncompatibleThreadStateException();
        }
        ObjectMonitor mon = myJavaThread.getCurrentWaitingMonitor();
        if (mon == null) {
           // thread is not doing an Object.wait() call
           mon = myJavaThread.getCurrentPendingMonitor();
           if (mon != null) {
               OopHandle handle = mon.object();
               // If obj == NULL, then ObjectMonitor is raw which doesn't count
               // as contended for this API
               return vm().objectMirror(handle);
           } else {
               // no contended ObjectMonitor
               return null;
           }
        } else {
           // thread is doing an Object.wait() call
           OopHandle handle = mon.object();
           if (Assert.ASSERTS_ENABLED) {
               Assert.that(handle != null, "Object.wait() should have an object");
           }
           return vm().objectMirror(handle);
        }
    }

    public String toString() {
        return "instance of " + referenceType().name() +
               "(name='" + name() + "', " + "id=" + uniqueID() + ")";
    }

    @Override
    byte typeValueKey() {
        return JDWP.Tag.THREAD;
    }
}
