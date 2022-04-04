/*
 * Copyright (c) 2003, 2004, Oracle and/or its affiliates. All rights reserved.
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
import sun.jvm.hotspot.oops.InstanceKlass;
import sun.jvm.hotspot.oops.Klass;
import sun.jvm.hotspot.oops.LineNumberTableElement;
import sun.jvm.hotspot.oops.LocalVariableTableElement;
import sun.jvm.hotspot.tools.jcore.ByteCodeRewriter;

import java.lang.ref.SoftReference;
import java.util.*;

public class ConcreteMethodImpl extends MethodImpl {

    /*
     * A subset of the line number info that is softly cached
     */
    static public class SoftLocationXRefs {
        final String stratumID;   // The stratum of this information
        public final Map<Integer, List<LocationImpl>> lineMapper;     // Maps line number to location(s)
        final List<LocationImpl> lineLocations; // List of locations ordered by code index

        /*
         * Note: these do not necessarily correspond to
         * the line numbers of the first and last elements
         * in the lineLocations list. Use these only for bounds
         * checking and with lineMapper.
         */
        final int lowestLine;
        final int highestLine;

        SoftLocationXRefs(String stratumID, Map<Integer, List<LocationImpl>> lineMapper, List<LocationImpl> lineLocations, int lowestLine, int highestLine) {
            this.stratumID = stratumID;
            this.lineMapper = Collections.unmodifiableMap(lineMapper);
            this.lineLocations = Collections.unmodifiableList(lineLocations);
            this.lowestLine = lowestLine;
            this.highestLine = highestLine;
        }
    }

    private SoftReference<SoftLocationXRefs> softBaseLocationXRefsRef;
    private SoftReference<SoftLocationXRefs> softOtherLocationXRefsRef;
    private SoftReference<List<LocalVariableImpl>> variablesRef = null;
    private int firstIndex = -1;
    private int lastIndex = -1;
    private SoftReference<byte[]> bytecodesRef = null;

    ConcreteMethodImpl(ReferenceTypeImpl declaringType, sun.jvm.hotspot.oops.Method saMethod) {
        super(declaringType, saMethod);
    }

    public int argSlotCount() {
        return (int) saMethod.getSizeOfParameters();
    }

    private SoftLocationXRefs getLocations(SDE.Stratum stratum) {
        if (stratum.isJava()) {
            return getBaseLocations();
        }
        String stratumID = stratum.id();
        SoftLocationXRefs info = (softOtherLocationXRefsRef == null) ? null : softOtherLocationXRefsRef.get();
        if (info != null && info.stratumID.equals(stratumID)) {
            return info;
        }

        List<LocationImpl> lineLocations = new ArrayList<LocationImpl>();
        Map<Integer, List<LocationImpl>> lineMapper = new HashMap<Integer, List<LocationImpl>>();
        int lowestLine = -1;
        int highestLine = -1;
        SDE.LineStratum lastLineStratum = null;
        SDE.Stratum baseStratum = declaringType.stratum(SDE.BASE_STRATUM_NAME);
        for (LocationImpl loc : getBaseLocations().lineLocations) {
            int baseLineNumber = loc.lineNumber(baseStratum);
            SDE.LineStratum lineStratum = stratum.lineStratum(declaringType, baseLineNumber);

            if (lineStratum == null) {
                // location not mapped in this stratum
                continue;
            }

            int lineNumber = lineStratum.lineNumber();

            // remove unmapped and dup lines
            if ((lineNumber != -1) && (!lineStratum.equals(lastLineStratum))) {
                lastLineStratum = lineStratum;
                // Remember the largest/smallest line number
                if (lineNumber > highestLine) {
                    highestLine = lineNumber;
                }
                if ((lineNumber < lowestLine) || (lowestLine == -1)) {
                    lowestLine = lineNumber;
                }

                loc.addStratumLineInfo(
                        new StratumLineInfo(stratumID,
                                lineNumber,
                                lineStratum.sourceName(),
                                lineStratum.sourcePath()));

                // Add to the location list
                lineLocations.add(loc);

                // Add to the line -> locations map
                Integer key = lineNumber;
                List<LocationImpl> mappedLocs = lineMapper.get(key);
                if (mappedLocs == null) {
                    mappedLocs = new ArrayList<LocationImpl>(1);
                    lineMapper.put(key, mappedLocs);
                }
                mappedLocs.add(loc);
            }
        }

        info = new SoftLocationXRefs(stratumID,
                                lineMapper, lineLocations,
                                lowestLine, highestLine);
        softOtherLocationXRefsRef = new SoftReference<SoftLocationXRefs>(info);
        return info;
    }

    public SoftLocationXRefs getBaseLocations() {
        SoftLocationXRefs info = (softBaseLocationXRefsRef == null) ? null : softBaseLocationXRefsRef.get();
        if (info != null) {
            return info;
        }

        byte[] codeBuf = bytecodes();
        firstIndex = 0;
        lastIndex = codeBuf.length - 1;

        boolean hasLineInfo = saMethod.hasLineNumberTable();
        LineNumberTableElement[] lntab = null;
        int count;

        if (hasLineInfo) {
            lntab = saMethod.getLineNumberTable();
            count = lntab.length;
        } else {
            count = 0;
        }

        List<LocationImpl> lineLocations = new ArrayList<LocationImpl>(count);
        Map<Integer, List<LocationImpl>> lineMapper = new HashMap<Integer, List<LocationImpl>>();
        int lowestLine = -1;
        int highestLine = -1;
        for (int i = 0; i < count; i++) {
            long bci = lntab[i].getStartBCI();
            int lineNumber = lntab[i].getLineNumber();

            /*
             * Some compilers will point multiple consecutive
             * lines at the same location. We need to choose
             * one of them so that we can consistently map back
             * and forth between line and location. So we choose
             * to record only the last line entry at a particular
             * location.
             */
            if ((i + 1 == count) || (bci != lntab[i+1].getStartBCI())) {
                // Remember the largest/smallest line number
                if (lineNumber > highestLine) {
                    highestLine = lineNumber;
                }
                if ((lineNumber < lowestLine) || (lowestLine == -1)) {
                    lowestLine = lineNumber;
                }
                LocationImpl loc = new LocationImpl(this, bci);
                loc.addBaseLineInfo(
                    new BaseLineInfo(lineNumber, declaringType));

                // Add to the location list
                lineLocations.add(loc);

                // Add to the line -> locations map
                Integer key = lineNumber;
                List<LocationImpl> mappedLocs = lineMapper.get(key);
                if (mappedLocs == null) {
                    mappedLocs = new ArrayList<LocationImpl>(1);
                    lineMapper.put(key, mappedLocs);
                }
                mappedLocs.add(loc);
            }
        }

        info = new SoftLocationXRefs(SDE.BASE_STRATUM_NAME, lineMapper, lineLocations, lowestLine, highestLine);
        softBaseLocationXRefsRef = new SoftReference<SoftLocationXRefs>(info);
        return info;
    }

    public List<LocationImpl> allLineLocations(SDE.Stratum stratum)
        throws AbsentInformationException {
        List<LocationImpl> lineLocations = getLocations(stratum).lineLocations;

        if (lineLocations.size() == 0) {
            throw new AbsentInformationException();
        }

        return Collections.unmodifiableList(lineLocations);
    }

    LineInfo codeIndexToLineInfo(SDE.Stratum stratum,
                                 long codeIndex) {
        if (firstIndex == -1) {
            getBaseLocations();
        }

        /*
         * Check for invalid code index.
         */
        if (codeIndex < firstIndex || codeIndex > lastIndex) {
            throw new InternalError(
                    "Location with invalid code index");
        }

        List<LocationImpl> lineLocations = getLocations(stratum).lineLocations;

        /*
         * Check for absent line numbers.
         */
        if (lineLocations.size() == 0) {
            return super.codeIndexToLineInfo(stratum, codeIndex);
        }

        Iterator<LocationImpl> iter = lineLocations.iterator();
        /*
         * Treat code before the beginning of the first line table
         * entry as part of the first line.  javac will generate
         * code like this for some local classes. This "prolog"
         * code contains assignments from locals in the enclosing
         * scope to synthetic fields in the local class.  Same for
         * other language prolog code.
         */
        LocationImpl bestMatch = iter.next();
        while (iter.hasNext()) {
            LocationImpl current = iter.next();
            if (current.codeIndex() > codeIndex) {
                break;
            }
            bestMatch = current;
        }
        return bestMatch.getLineInfo(stratum);
    }

    public List<LocalVariableImpl> variables() throws AbsentInformationException {
        return getVariables();
    }

    public byte[] bytecodes() {
        byte[] bytecodes = (bytecodesRef == null) ? null :
                bytecodesRef.get();
        if (bytecodes == null) {
            bytecodes = saMethod.getByteCode();
            Klass klass = declaringType.ref();
            if (klass instanceof InstanceKlass) {
                new ByteCodeRewriter(saMethod, ((InstanceKlass) klass).getConstants(), bytecodes).rewrite();
            }
            bytecodesRef = new SoftReference<byte[]>(bytecodes);
        }
        /*
         * Arrays are always modifiable, so it is a little unsafe
         * to return the cached bytecodes directly; instead, we
         * make a clone at the cost of using more memory.
         */
        return bytecodes.clone();
    }

    private List<LocalVariableImpl> getVariables() throws AbsentInformationException {
        List<LocalVariableImpl> variables = (variablesRef == null) ? null :
                variablesRef.get();
        if (variables != null) {
            return variables;
        }

        // if there are no locals, there won't be a LVT
        if (saMethod.getMaxLocals() == 0) {
           variables = Collections.unmodifiableList(Collections.<LocalVariableImpl>emptyList());
           variablesRef = new SoftReference<List<LocalVariableImpl>>(variables);
           return variables;
        }

        if (! saMethod.hasLocalVariableTable()) {
            throw new AbsentInformationException();
        }
        //Build up the JDI view of local variable table.
        LocalVariableTableElement[] locals = saMethod.getLocalVariableTable();
        int localCount = locals.length;
        variables = new ArrayList<LocalVariableImpl>(localCount);
        for (LocalVariableTableElement local : locals) {
            String name =
                    saMethod.getConstants().getSymbolAt(local.getNameCPIndex()).asString();
            /*
             * Skip "this$*", "this+*", "this" entries because they are never real
             * variables from the JLS perspective. "this+*" is new with 1.5.
             * Instead of using '+', we check for java letter or digit to avoid
             * depending on javac's current choice of '+'.
             */
            boolean isInternalName = name.startsWith("this") &&
                    (name.length() == 4 || name.charAt(4) == '$' || !Character.isJavaIdentifierPart(name.charAt(4)));
            if (!isInternalName) {
                int slot = local.getSlot();
                long codeIndex = local.getStartBCI();
                int length = local.getLength();
                LocationImpl scopeStart = new LocationImpl(this, codeIndex);
                LocationImpl scopeEnd = new LocationImpl( this, codeIndex + length - 1);
                String signature =
                        saMethod.getConstants().getSymbolAt(local.getDescriptorCPIndex()).asString();

                int genericSigIndex = local.getSignatureCPIndex();
                String genericSignature = null;
                if (genericSigIndex != 0) {
                    genericSignature = saMethod.getConstants().getSymbolAt(genericSigIndex).asString();
                }

                LocalVariableImpl variable =
                        new LocalVariableImpl(this,
                                slot, scopeStart, scopeEnd,
                                name, signature, genericSignature);
                // Add to the variable list
                variables.add(variable);
            }
        }

        variables = Collections.unmodifiableList(variables);
        variablesRef = new SoftReference<List<LocalVariableImpl>>(variables);
        return variables;
    }
}
