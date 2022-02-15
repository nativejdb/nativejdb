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
 */

package jdwp.jdi;

import jdwp.JDWP;
import sun.jvm.hotspot.debugger.OopHandle;
import sun.jvm.hotspot.oops.Array;
import sun.jvm.hotspot.oops.ObjArray;
import sun.jvm.hotspot.oops.TypeArray;
import sun.jvm.hotspot.runtime.BasicType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayReferenceImpl extends ObjectReferenceImpl {
    private final int length;
    ArrayReferenceImpl(ReferenceTypeImpl type, Array aRef) {
        super(type, aRef);
        length = (int) aRef.getLength();
    }

    public ArrayTypeImpl arrayType() {
        return (ArrayTypeImpl)referenceType();
    }

    /**
     * Return array length.
     */
    public int length() {
        return length;
    }

    public ValueImpl getValue(int index) {
        return getValues(index, 1).get(0);
    }

    /**
     * Validate that the range to set/get is valid.
     * length of -1 (meaning rest of array) has been converted
     * before entry.
     */
    private void validateArrayAccess(int index, int len) {
        // because length can be computed from index,
        // index must be tested first for correct error message
        if ((index < 0) || (index > length())) {
            throw new IndexOutOfBoundsException(
                        "Invalid array index: " + index);
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException(
                        "Invalid array range length: " + len);
        }
        if (index + len > length()) {
            throw new IndexOutOfBoundsException(
                        "Invalid array range: " +
                        index + " to " + (index + len - 1));
        }
    }

    public List<ValueImpl> getValues(int index, int len) {
        if (len == -1) { // -1 means the rest of the array
           len = length() - index;
        }
        validateArrayAccess(index, len);
        if (len == 0) {
            return Collections.emptyList();
        }
        List<ValueImpl> vals = new ArrayList<ValueImpl>(len);

        TypeArray typeArray = null;
        ObjArray objArray = null;
        if (ref() instanceof TypeArray) {
            typeArray = (TypeArray)ref();
        } else if (ref() instanceof ObjArray) {
            objArray = (ObjArray)ref();
        } else {
            throw new RuntimeException("should not reach here");
        }

        char c = arrayType().componentSignature().charAt(0);
        BasicType variableType = BasicType.charToBasicType(c);

        final int limit = index + len;
        for (int ii = index; ii < limit; ii++) {
            ValueImpl valueImpl;
            if (variableType == BasicType.T_BOOLEAN) {
                valueImpl = vm().mirrorOf(typeArray.getBooleanAt(ii));
            } else if (variableType == BasicType.T_CHAR) {
                valueImpl = vm().mirrorOf(typeArray.getCharAt(ii));
            } else if (variableType == BasicType.T_FLOAT) {
                valueImpl = vm().mirrorOf(typeArray.getFloatAt(ii));
            } else if (variableType == BasicType.T_DOUBLE) {
                valueImpl = vm().mirrorOf(typeArray.getDoubleAt(ii));
            } else if (variableType == BasicType.T_BYTE) {
                valueImpl = vm().mirrorOf(typeArray.getByteAt(ii));
            } else if (variableType == BasicType.T_SHORT) {
                valueImpl = vm().mirrorOf(typeArray.getShortAt(ii));
            } else if (variableType == BasicType.T_INT) {
                valueImpl = vm().mirrorOf(typeArray.getIntAt(ii));
            } else if (variableType == BasicType.T_LONG) {
                valueImpl = vm().mirrorOf(typeArray.getLongAt(ii));
            } else if (variableType == BasicType.T_OBJECT || variableType == BasicType.T_ARRAY) {
                // we may have an [Ljava/lang/Object; - i.e., Object[] with the
                // elements themselves may be arrays because every array is an Object.
                valueImpl = vm().objectMirror(objArray.getOopHandleAt(ii));
            } else {
                throw new RuntimeException("should not reach here");
            }
            vals.add (valueImpl);
        }
        return vals;
    }

    protected void visitReferences(HandleVisitor visitor) {
        if (ref() instanceof ObjArray) {
            ObjArray objArray = (ObjArray) ref();
            for (int i = 0; i < length; i++) {
                OopHandle valueHandle = objArray.getOopHandleAt(i);
                if (valueHandle != null && visitor.visit(valueHandle)) {
                    return;
                }
            }
        }
    }

    public String toString() {
        return "instance of " + arrayType().componentSignature() + "[" + length() + "] (id=" + uniqueID() + ")";
    }

    @Override
    byte typeValueKey() {
        return JDWP.Tag.ARRAY;
    }
}
