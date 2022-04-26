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

import jdwp.jdi.ClassObjectReferenceImpl;
import jdwp.jdi.ReferenceTypeImpl;

public class JDWPClassObjectReference {

    static class ClassObjectReference {
        static final int COMMAND_SET = 17;
        private ClassObjectReference() {}  // hide constructor

        /**
         * Returns the reference type reflected by this class object.
         */
        static class ReflectedType implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ClassObjectReferenceImpl reference = command.readClassObjectReference();
                ReferenceTypeImpl type = reference.reflectedType();
                if (type == null) {
                    answer.writeByte(JDWP.TypeTag.CLASS);
                    answer.writeClassRef(0);
                }
                else {
                    answer.writeByte(type.tag());
                    answer.writeClassRef(type.uniqueID());
                }
            }
        }
    }
}
