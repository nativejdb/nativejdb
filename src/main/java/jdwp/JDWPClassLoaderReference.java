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

package jdwp;

public class JDWPClassLoaderReference {
    static class ClassLoaderReference {
        static final int COMMAND_SET = 14;
        private ClassLoaderReference() {}  // hide constructor

        /**
         * Returns a list of all classes which this class loader has
         * been requested to load. This class loader is considered to be
         * an <i>initiating</i> class loader for each class in the returned
         * list. The list contains each
         * reference type defined by this loader and any types for which
         * loading was delegated by this class loader to another class loader.
         * <p>
         * The visible class list has useful properties with respect to
         * the type namespace. A particular type name will occur at most
         * once in the list. Each field or variable declared with that
         * type name in a class defined by
         * this class loader must be resolved to that single type.
         * <p>
         * No ordering of the returned list is guaranteed.
         */
        static class VisibleClasses implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }
    }
}
