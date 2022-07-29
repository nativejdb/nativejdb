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
import gdb.mi.service.command.output.MIInstruction;
import gdb.mi.service.command.output.MIResultRecord;
import jdwp.jdi.ObjectReferenceImpl;
import jdwp.jdi.StringReferenceImpl;

import java.util.ArrayList;

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

                    // Queue GDB to get instructions
                    System.out.println("Queueing MI to get assembly instructions");
                    StringBuilder instructions = new StringBuilder();
                    MICommand cmd = gc.getCommandFactory().createMIDataDisassemble("$pc", "$pc + 40", false);
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
                } else {
                    ObjectReferenceImpl objectReference = gc.vm.objectMirror(uniqueID);
                    if (objectReference instanceof StringReferenceImpl) {
                        answer.writeString(((StringReferenceImpl) objectReference).value());
                    }
                    else {
                        answer.pkt.errorCode = JDWP.Error.INVALID_STRING;
                    }
                }
            }
        }
    }
}
