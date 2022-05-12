/*
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

import static org.junit.Assert.assertEquals;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.connect.spi.Connection;
import jdwp.*;
import jdwp.jdi.VirtualMachineImpl;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Verifies that the break insert MI command have the correct path substitution.
 */
public class TestJDWPProxy {
    @Mock
    SocketTransportService socketTransportService = new SocketTransportService();

    @Test
    public void testJDWPBreakpointInsertPackets() {
        Connection connection = Mockito.mock(Connection.class);
        //VirtualMachineImpl vm = VirtualMachineImpl.dummyVirtualMachine(); //not working HeapVisitor error
        System.setProperty("native.exec", "src/test/data/Hello");
        System.setProperty("native.src", "src/test/data/Hellosources");
        GDBControl gdbControl = new GDBControl(connection, null);
        gdbControl.startCommandProcessing(gdbControl.gdbOutput, gdbControl.gdbInput, gdbControl.gdbError);

        Packet p = new Packet(Packet.NoFlags);
        p.data = new byte[]{2, 2, 0, 0, 0, 1, 7, 1, 0, 0, 0, 8, 64, 6, 58, 72, 0, 0, 127, -47, 119, -112, 39, -16, 0, 0, 0, 0, 0, 0, 0, 0};
        PacketStream packetStream = new PacketStream(gdbControl, p.id, 15, 1);
        Command command = JDWPProxy.COMMANDS.get(15).get(1);
        try {
            //command.reply(gdbControl, packetStream, new PacketStream(gdbControl, p));
        } catch (Exception e) {
        }
    }

}
