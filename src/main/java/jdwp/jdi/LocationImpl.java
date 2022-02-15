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
 */

package jdwp.jdi;

import sun.jvm.hotspot.oops.Method;

public class LocationImpl {
    private final ReferenceTypeImpl declaringType;
    private MethodImpl method;
    private Method methodRef;
    private long codeIndex;
    private LineInfo baseLineInfo = null;
    private LineInfo otherLineInfo = null;

    public LocationImpl(MethodImpl method, long codeIndex) {
        this.method = method;
        this.codeIndex = method.isNative()? -1 : codeIndex;
        this.declaringType = method.declaringType();
    }

    /*
     * This constructor allows lazy creation of the method mirror. This
     * can be a performance savings if the method mirror does not yet
     * exist.
     */
    public LocationImpl(ReferenceTypeImpl declaringType, Method methodRef, long codeIndex) {
        this.method = null;
        this.codeIndex = codeIndex;
        this.declaringType = declaringType;
        this.methodRef = methodRef;
    }

    public boolean equals(Object obj) {
        if ((obj instanceof LocationImpl)) {
            LocationImpl other = (LocationImpl)obj;
            return (method().equals(other.method())) &&
                   (codeIndex() == other.codeIndex()) &&
                   super.equals(obj);
        } else {
            return false;
        }
    }

    public int hashCode() {
        /*
         * TO DO: better hash code?
         */
        return method().hashCode() + (int)codeIndex();
    }

    public int compareTo(LocationImpl other) {
        int rc = method().compareTo(other.method());
        if (rc == 0) {
            long diff = codeIndex() - other.codeIndex();
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;
            else
                return 0;
        }
        return rc;
    }

    public ReferenceTypeImpl declaringType() {
        return declaringType;
    }

    public MethodImpl method() {
        if (method == null) {
            method = declaringType.getMethodMirror(methodRef);
            if (method.isNative()) {
                codeIndex = -1;
            }
        }
        return method;
    }

    public long methodRef() {
        if (method != null) {
            return method.uniqueID();
        }
        else {
            return MethodImpl.uniqueID(methodRef, declaringType.vm);
        }
    }

    public long codeIndex() {
        method();  // be sure information is up-to-date
        return codeIndex;
    }

    public long codeIndexInt() {
        return codeIndex;
    }

    LineInfo getBaseLineInfo(SDE.Stratum stratum) {
        LineInfo lineInfo;

        /* check if there is cached info to use */
        if (baseLineInfo != null) {
            return baseLineInfo;
        }

        /* compute the line info */
        MethodImpl methodImpl = method();
         lineInfo = methodImpl.codeIndexToLineInfo(stratum,
                                                   codeIndex());

        /* cache it */
        addBaseLineInfo(lineInfo);

        return lineInfo;
    }

    LineInfo getLineInfo(SDE.Stratum stratum) {
        LineInfo lineInfo;

        /* base stratum is done slighly differently */
        if (stratum.isJava()) {
            return getBaseLineInfo(stratum);
        }

        /* check if there is cached info to use */
        lineInfo = otherLineInfo; // copy because of concurrency
        if (lineInfo != null &&
                           stratum.id().equals(lineInfo.liStratum())) {
            return lineInfo;
        }
        int baseLineNumber = lineNumber(SDE.BASE_STRATUM_NAME);
        SDE.LineStratum lineStratum =
                  stratum.lineStratum(declaringType, baseLineNumber);

        if (lineStratum != null && lineStratum.lineNumber() != -1) {
            lineInfo = new StratumLineInfo(stratum.id(),
                                           lineStratum.lineNumber(),
                                           lineStratum.sourceName(),
                                           lineStratum.sourcePath());
        } else {
            /* find best match */
            MethodImpl methodImpl = method();
            lineInfo = methodImpl.codeIndexToLineInfo(stratum,
                                                      codeIndex());
        }

        /* cache it */
        addStratumLineInfo(lineInfo);

        return lineInfo;
    }

    void addStratumLineInfo(LineInfo lineInfo) {
        otherLineInfo = lineInfo;
    }

    void addBaseLineInfo(LineInfo lineInfo) {
        baseLineInfo = lineInfo;
    }

    public int lineNumber() {
        return lineNumber((String)null);
    }

    public int lineNumber(String stratumID) {
        return lineNumber(declaringType.stratum(stratumID));
    }

    int lineNumber(SDE.Stratum stratum) {
        return getLineInfo(stratum).liLineNumber();
    }

    public String toString() {
        if (lineNumber() == -1) {
            return method().toString() + "+" + codeIndex();
        } else {
            return declaringType().name() + ":" + lineNumber();
        }
    }
}
