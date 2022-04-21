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

import gdb.mi.service.command.events.MIEvent;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.connect.spi.Connection;
import gdb.mi.service.command.Listener;
import gdb.mi.service.command.MIRunControlEventProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * @author egor
 */
public class JDWPProxy {
    protected static final Map<Integer, Map<Integer, Command>> COMMANDS = new HashMap<Integer, Map<Integer, Command>>();

    static {
        try {
            List<Class> declaredClasses = new ArrayList<>();
            declaredClasses.addAll(Arrays.asList(JDWPArrayReference.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPArrayType.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPClassLoaderReference.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPClassObjectReference.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPClassType.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPEvent.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPEventRequest.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPField.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPInterfaceType.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPMethod.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPModuleReference.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPObjectReference.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPReferenceType.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPStackFrame.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPStringReference.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPThreadGroupReference.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPThreadReference.class.getDeclaredClasses()));
            declaredClasses.addAll(Arrays.asList(JDWPVirtualMachine.class.getDeclaredClasses()));

            for (Class<?> declaredClass : declaredClasses) {
                try {
                    int setId = (Integer) declaredClass.getDeclaredField("COMMAND_SET").get(null);
                    Class<?>[] commandsClasses = declaredClass.getDeclaredClasses();
                    HashMap<Integer, Command> commandsMap = new HashMap<Integer, Command>();
                    COMMANDS.put(setId, commandsMap);
                    for (Class<?> commandsClass : commandsClasses) {
                        try {
                            int commandId = (Integer) commandsClass.getDeclaredField("COMMAND").get(null);
                            commandsMap.put(commandId, (Command) commandsClass.getDeclaredConstructor().newInstance());
                        } catch (NoSuchFieldException ignored) {
                        }
                    }
                } catch (NoSuchFieldException ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reply(Connection connection, jdwp.jdi.VirtualMachineImpl vm) throws IOException {

        GDBControl gdbControl = new GDBControl(connection, vm);
        Listener asyncListener = new MIRunControlEventProcessor(gdbControl);

        // Declare that the VM has started
        PacketStream VMStartedPkt = Translator.getVMStartedPacket(gdbControl);
        VMStartedPkt.send();

        try {
            gdbControl.startCommandProcessing(gdbControl.gdbOutput, gdbControl.gdbInput, gdbControl.gdbError);

            while (true) {
                byte[] b = connection.readPacket();
                Packet p = Packet.fromByteArray(b);
                int cmdSet = p.cmdSet;
                int cmd = p.cmd;
                PacketStream packetStream = new PacketStream(gdbControl, p.id, cmdSet, cmd);
                Command command = COMMANDS.get(cmdSet).get(cmd);
                try {
                    command.reply(gdbControl, packetStream, new PacketStream(gdbControl, p));
                } catch (VMDisconnectedException vde) {
                    throw  vde;
                } catch (Exception e) {
                    e.printStackTrace();
                    packetStream.pkt.errorCode = JDWP.Error.INTERNAL;
                    packetStream.dataStream.reset();

                    // serialize the original exception as a utf8 string
                    try {
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        PrintStream printStream = new PrintStream(byteStream, false, "UTF8");
                        e.printStackTrace(printStream);
                        printStream.close();
                        packetStream.writeString(byteStream.toString("UTF8"));
                    } catch (Exception ignored) {
                    }
                }
                packetStream.send();
                for (MIEvent event: JDWPEventRequest.asyncEvents) {
                    asyncListener.onEvent(event);
                }
                JDWPEventRequest.asyncEvents.clear();
            }
        } catch (VMDisconnectedException ignored) {
        } finally {
            connection.close();
            gdbControl.vm.dispose();
        }
    }

}
