/*
 * Copyright (c) 2002, 2005, Oracle and/or its affiliates. All rights reserved.
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

import com.sun.jdi.InvalidStackFrameException;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.runtime.BasicType;
import sun.jvm.hotspot.runtime.JavaVFrame;
import sun.jvm.hotspot.runtime.StackValueCollection;
import sun.jvm.hotspot.utilities.Assert;

public class StackFrameImpl {
    /* Once false, frame should not be used.
     * access synchronized on (vm.state())
     */
    private final boolean isValid = true;

    private final ThreadReferenceImpl thread;
    private final JavaVFrame saFrame;
    private StackValueCollection locals = null;
    private final int id;
    private final LocationImpl location;
    private ObjectReferenceImpl thisObject = null;

    StackFrameImpl(ThreadReferenceImpl thread, JavaVFrame jvf, int id) {
        this.thread = thread;
        this.saFrame = jvf;
        this.id = id;

        sun.jvm.hotspot.oops.Method SAMethod = jvf.getMethod();

        ReferenceTypeImpl rt = thread.vm().referenceType(CompatibilityHelper.INSTANCE.getMethodHolder(SAMethod));

        this.location = new LocationImpl(rt, SAMethod, jvf.getBCI());
    }

    private void validateStackFrame() {
        if (!isValid) {
            throw new InvalidStackFrameException("Thread has been resumed");
        }
    }

    VirtualMachineImpl vm() {
        return thread.vm();
    }

//    public long uniqueID() {
//        return vm.saVM().getDebugger().getAddressValue(saFrame.getFrame().getID());
//    }

    public int id() {
        return id;
    }

    /**
     * Return the frame location.
     * Need not be synchronized since it cannot be provably stale.
     */
    public LocationImpl location() {
        validateStackFrame();
        return location;
    }

    public boolean equals(Object obj) {
        if ((obj instanceof StackFrameImpl)) {
            StackFrameImpl other = (StackFrameImpl)obj;
            return (saFrame.equals(other.saFrame));
        } else {
            return false;
        }
    }

    public int hashCode() {
        return saFrame.hashCode();
    }

    public ObjectReferenceImpl thisObject() {
        validateStackFrame();
        MethodImpl currentMethod = location.method();
        if (currentMethod.isStatic() || currentMethod.isNative()) {
            return null;
        }
        if (thisObject == null) {
            StackValueCollection values = getLocals();
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(values.size() > 0, "this is missing");
            }
            // 'this' at index 0.
            if (values.get(0).getType() == BasicType.getTConflict()) {
              return null;
            }
            thisObject = vm().objectMirror(values.oopHandleAt(0));
        }
        return thisObject;
    }

    private StackValueCollection getLocals() {
        if (locals == null) {
            locals = saFrame.getLocals();
        }
        return locals;
    }

    public int getAvailableSlots() {
        return getLocals().size();
    }

    public ValueImpl getSlotValue(int slot, byte sigbyte) {
        BasicType variableType = BasicType.charToBasicType((char) sigbyte);
        return getSlotValue(getLocals(), variableType, slot);
    }

    private ValueImpl getSlotValue(StackValueCollection values, BasicType variableType, int ss) {
        ValueImpl valueImpl;
        OopHandle handle;
        if (values.get(ss).getType() == BasicType.getTConflict()) {
          // Dead locals, so just represent them as a zero of the appropriate type
          if (variableType == BasicType.T_BOOLEAN) {
            valueImpl = vm().mirrorOf(false);
          } else if (variableType == BasicType.T_CHAR) {
            valueImpl = vm().mirrorOf((char)0);
          } else if (variableType == BasicType.T_FLOAT) {
            valueImpl = vm().mirrorOf((float)0);
          } else if (variableType == BasicType.T_DOUBLE) {
            valueImpl = vm().mirrorOf((double)0);
          } else if (variableType == BasicType.T_BYTE) {
            valueImpl = vm().mirrorOf((byte)0);
          } else if (variableType == BasicType.T_SHORT) {
            valueImpl = vm().mirrorOf((short)0);
          } else if (variableType == BasicType.T_INT) {
            valueImpl = vm().mirrorOf(0);
          } else if (variableType == BasicType.T_LONG) {
            valueImpl = vm().mirrorOf((long)0);
          } else if (variableType == BasicType.T_OBJECT) {
            // we may have an [Ljava/lang/Object; - i.e., Object[] with the
            // elements themselves may be arrays because every array is an Object.
            handle = null;
            valueImpl = vm().objectMirror(handle);
          } else if (variableType == BasicType.T_ARRAY) {
            handle = null;
            valueImpl = vm().objectMirror(handle);
          } else if (variableType == BasicType.T_VOID) {
            valueImpl = vm().voidVal;
          } else {
            throw new RuntimeException("Should not read here");
          }
        } else {
          if (variableType == BasicType.T_BOOLEAN) {
            valueImpl = vm().mirrorOf(values.booleanAt(ss));
          } else if (variableType == BasicType.T_CHAR) {
            valueImpl = vm().mirrorOf(values.charAt(ss));
          } else if (variableType == BasicType.T_FLOAT) {
            valueImpl = vm().mirrorOf(values.floatAt(ss));
          } else if (variableType == BasicType.T_DOUBLE) {
            valueImpl = vm().mirrorOf(values.doubleAt(ss));
          } else if (variableType == BasicType.T_BYTE) {
            valueImpl = vm().mirrorOf(values.byteAt(ss));
          } else if (variableType == BasicType.T_SHORT) {
            valueImpl = vm().mirrorOf(values.shortAt(ss));
          } else if (variableType == BasicType.T_INT) {
            valueImpl = vm().mirrorOf(values.intAt(ss));
          } else if (variableType == BasicType.T_LONG) {
            valueImpl = vm().mirrorOf(values.longAt(ss));
          } else if (variableType == BasicType.T_OBJECT) {
            // we may have an [Ljava/lang/Object; - i.e., Object[] with the
            // elements themselves may be arrays because every array is an Object.
            handle = values.oopHandleAt(ss);
            valueImpl = vm().objectMirror(handle);
          } else if (variableType == BasicType.T_ARRAY) {
            handle = values.oopHandleAt(ss);
            valueImpl = vm().objectMirror(handle);
          } else if (variableType == BasicType.T_VOID) {
            valueImpl = new VoidValueImpl();
          } else {
            throw new RuntimeException("Should not read here");
          }
        }

        return valueImpl;
    }

    public String toString() {
        return location.toString() + " in thread " + thread.toString();
    }
}
