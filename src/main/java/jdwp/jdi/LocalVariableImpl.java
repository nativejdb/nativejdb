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

public class LocalVariableImpl implements Comparable<LocalVariableImpl> {
    private final MethodImpl method;
    private final int slot;
    private final LocationImpl scopeStart;
    private final LocationImpl scopeEnd;
    private final String name;
    private final String signature;
    private final String genericSignature;

    public LocalVariableImpl(MethodImpl method,
                      int slot, LocationImpl scopeStart, LocationImpl scopeEnd,
                      String name, String signature, String genericSignature) {
        this.method = method;
        this.slot = slot;
        this.scopeStart = scopeStart;
        this.scopeEnd = scopeEnd;
        this.name = name;
        this.signature = signature;
        this.genericSignature = genericSignature;
    }

    public boolean equals(Object obj) {
        if ((obj instanceof LocalVariableImpl)) {
            LocalVariableImpl other = (LocalVariableImpl)obj;
            return (method.equals(other.method) &&
                    slot() == other.slot() &&
                    super.equals(obj));
        } else {
            return false;
        }
    }

    public int hashCode() {
        /*
         * TO DO: Better hash code
         */
        return method.hashCode() + slot();
    }

    public int compareTo(LocalVariableImpl localVar) {
        int rc = method.compareTo(localVar.method);
        if (rc == 0) {
            rc = slot() - localVar.slot();
        }
        return rc;
    }

    public String name() {
        return name;
    }

    public String signature() {
        return signature;
    }

    public String genericSignature() {
        return genericSignature;
    }

    public int slot() {
        return slot;
    }

    public String toString() {
       return name() + " in " + method.toString() +
              "@" + scopeStart.toString();
    }

    public long getStart() {
        return scopeStart.codeIndex();
    }

    public int getLength() {
        return (int) (scopeEnd.codeIndex() - scopeStart.codeIndex());
    }

}
