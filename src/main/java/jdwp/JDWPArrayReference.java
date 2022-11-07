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
import gdb.mi.service.command.output.MIDataEvaluateExpressionInfo;
import gdb.mi.service.command.output.MIResultRecord;
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
                var objectID = command.readObjectRef();
                var lenCmd = gc.getCommandFactory().
                        createMIDataEvaluationExpression("(('java.lang.Object[]'*)(" + objectID + "))->len");
                var token = JDWP.getNewTokenId();
                gc.queueCommand(token, lenCmd);
                var lenReply = (MIDataEvaluateExpressionInfo) gc.getResponse(token, JDWP.DEF_REQUEST_TIMEOUT);
                if (lenReply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.setErrorCode((short) JDWP.Error.INTERNAL);
                } else {
                    answer.writeInt(Integer.parseInt(lenReply.getValue()));
                }
            }
        }

        /**
         * Returns a range of array components. The specified range must
         * be within the bounds of the array.
         */
        static class GetValues implements Command  {
            static final int COMMAND = 2;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                var objectID = command.readObjectRef();
                var firstIndex = command.readInt();
                var length = command.readInt();

                /* strings are not null terminated so we need to handle the length as well as the buffer */
                var dataCmd = gc.getCommandFactory().createMIDataEvaluationExpression("(('_objhdr'*)(" + objectID + "))->hub->name->value->data");
                var token = JDWP.getNewTokenId();
                gc.queueCommand(token, dataCmd);
                var dataReply = (MIDataEvaluateExpressionInfo) gc.getResponse(token, JDWP.DEF_REQUEST_TIMEOUT);
                if (dataReply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                    answer.setErrorCode((short) JDWP.Error.INTERNAL);
                } else {
                    var lenCmd = gc.getCommandFactory().
                            createMIDataEvaluationExpression("(('_objhdr'*)(" + objectID +
                                    "))->hub->name->value->len");
                    token = JDWP.getNewTokenId();
                    gc.queueCommand(token, lenCmd);
                    var lenReply = (MIDataEvaluateExpressionInfo) gc.getResponse(token, JDWP.DEF_REQUEST_TIMEOUT);
                    if (lenReply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.setErrorCode((short) JDWP.Error.INTERNAL);
                    } else {
                        var className = dataReply.getType().substring(0, Integer.parseInt(lenReply.getValue()));
                        var tag = Translator.arrayClassName2Tag(className);
                        answer.writeByte(tag);
                        answer.writeInt(length);
                        for(int i=firstIndex; i < length;++i) {
                            var itemCmd = gc.getCommandFactory().
                                    createMIDataEvaluationExpression("(('java.lang.Object[]'*)(" + objectID + "))->data[" +
                                            i + "]");
                            token = JDWP.getNewTokenId();
                            gc.queueCommand(token, itemCmd);
                            var itemReply = (MIDataEvaluateExpressionInfo) gc.getResponse(token, JDWP.DEF_REQUEST_TIMEOUT);
                            if (itemReply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                                answer.setErrorCode((short) JDWP.Error.INTERNAL);
                            } else {
                                JDWPStackFrame.writeValue(answer, tag, itemReply.getValue());
                            }
                        }
                    }
                }
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
