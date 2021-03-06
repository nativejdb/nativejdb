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

import com.sun.jdi.ClassNotLoadedException;
import jdwp.jdi.ArrayReferenceImpl;
import jdwp.jdi.PrimitiveTypeImpl;
import jdwp.jdi.TypeImpl;
import jdwp.jdi.ValueImpl;

public class JDWPArrayReference  {
    static class ArrayReference {
        static final int COMMAND_SET = 13;
        private ArrayReference() {}  // hide constructor

        /**
         * Returns the number of components in a given array.
         */
        static class Length implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ArrayReferenceImpl arrayReference = command.readArrayReference();
                answer.writeInt(arrayReference.length());
            }
        }

        /**
         * Returns a range of array components. The specified range must
         * be within the bounds of the array.
         */
        static class GetValues implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                ArrayReferenceImpl arrayReference = command.readArrayReference();
                int start = command.readInt();
                int length = command.readInt();

                byte tag;
                try {
                    TypeImpl type = arrayReference.arrayType().componentType();
                    tag = type instanceof PrimitiveTypeImpl ? ((PrimitiveTypeImpl) type).tag() : JDWP.Tag.OBJECT;
                } catch (ClassNotLoadedException e) { // fallback to the first element type
                    tag = ValueImpl.typeValueKey(arrayReference.getValue(0));
                }

                answer.writeArrayRegion(arrayReference.getValues(start, length), tag);
            }
        }

        /**
         * Sets a range of array components. The specified range must
         * be within the bounds of the array.
         * For primitive values, each value's type must match the
         * array component type exactly. For object values, there must be a
         * widening reference conversion from the value's type to the
         * array component type and the array component type must be loaded.
         */
        static class SetValues implements Command  {
            static final int COMMAND = 3;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                JDWP.notImplemented(answer);
            }
        }
    }
}
