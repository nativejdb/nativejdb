/*
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

import sun.jvm.hotspot.oops.Symbol;
import sun.jvm.hotspot.runtime.CompiledVFrame;
import sun.jvm.hotspot.runtime.JavaVFrame;
import sun.jvm.hotspot.utilities.AssertionFailure;

import java.util.Collections;
import java.util.List;

/**
 * @author egor
 */
class JvmUtils {
    static List getFrameMonitors(JavaVFrame frame) {
        // workaround for an NPE inside CompiledVFrame
        if (frame instanceof CompiledVFrame && ((CompiledVFrame) frame).getScope() == null) {
            return Collections.emptyList();
        }
        return frame.getMonitors();
    }

    static JavaVFrame getFrameJavaSender(JavaVFrame frame) {
        try {
            return frame.javaSender();
        } catch (AssertionFailure e) {
            return null; // do not fail
        }
    }

    static boolean nameEquals(Symbol symbol, String name) {
        return name.equals(symbol.asString());
    }
}
