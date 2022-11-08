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

import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.output.MIDataDisassembleInfo;
import gdb.mi.service.command.output.MIDataEvaluateExpressionInfo;
import gdb.mi.service.command.output.MIInstruction;
import gdb.mi.service.command.output.MIResultRecord;

public class JDWPStringReference {
    static class StringReference {
        static final int COMMAND_SET = 10;
        private StringReference() {}  // hide constructor

        /**
         * Returns the characters contained in the string.
         */
        static class Value implements Command  {
            static final int COMMAND = 1;

            public void reply(GDBControl gc, PacketStream answer, PacketStream command) {
                long uniqueID = command.readObjectRef();
                if (uniqueID == JDWP.asmIdCounter) {
                    StringBuilder instructions = new StringBuilder();
                    int lines = Integer.parseInt(System.getenv("ASM_LINE"));
                    String endLine = "$pc + " + lines * 4;

                    // Queue GDB to get instructions
                    System.out.printf("Queueing MI to get %d lines of assembly instructions\n", lines);
                    MICommand cmd = gc.getCommandFactory().createMIDataDisassemble("$pc", endLine, false);
                    int tokenID = JDWP.getNewTokenId();
                    gc.queueCommand(tokenID, cmd);

                    MIDataDisassembleInfo reply = (MIDataDisassembleInfo) gc.getResponse(tokenID, JDWP.DEF_REQUEST_TIMEOUT);
                    if (reply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.pkt.errorCode = JDWP.Error.INTERNAL;
                    }

                    MIInstruction[] asmCodes = reply.getMIAssemblyCode();

                    for (MIInstruction code : asmCodes) {
                        String ins = code.getInstruction();
                        instructions.append(ins);
                        instructions.append("\n");
                    }
                    answer.writeString(instructions.toString());
                } else if (uniqueID == JDWP.optimizedVarID) {
                    answer.writeString("<optimized out>");
                } else {
                    var lenCmd = gc.getCommandFactory().
                            createMIDataEvaluationExpression("(('java.lang.String'*)(" + uniqueID +
                                    "))->value->len");
                    var token = JDWP.getNewTokenId();
                    gc.queueCommand(token, lenCmd);
                    var lenReply = (MIDataEvaluateExpressionInfo) gc.getResponse(token, JDWP.DEF_REQUEST_TIMEOUT);
                    if (lenReply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                        answer.setErrorCode((short) JDWP.Error.INTERNAL);
                    } else {
                        var dataCmd = gc.getCommandFactory().
                                createMIDataEvaluationExpression("(('java.lang.String'*)(" + uniqueID +
                                        "))->value->data");
                        token = JDWP.getNewTokenId();
                        gc.queueCommand(token, dataCmd);
                        var dataReply = (MIDataEvaluateExpressionInfo) gc.getResponse(token, JDWP.DEF_REQUEST_TIMEOUT);
                        if (dataReply.getMIOutput().getMIResultRecord().getResultClass().equals(MIResultRecord.ERROR)) {
                            answer.setErrorCode((short) JDWP.Error.INTERNAL);
                        } else {
                            answer.writeString(dataReply.getString().substring(0, Integer.parseInt(lenReply.getValue())));
                        }
                    }
                }
            }
        }
    }
}
