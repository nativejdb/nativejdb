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
 *
 */


package jdwp;

import com.sun.jdi.connect.spi.Connection;
import com.sun.jdi.connect.spi.TransportService;
import jdwp.jdi.VirtualMachineImpl;

import java.io.IOException;

public class JDWPServer {
    static final String WAITING_FOR_DEBUGGER = "Waiting for debugger on: ";
    static final String SERVER_READY = "sa-jdwp server connected";

    public static void main(String[] args) throws Exception {
        final VirtualMachineImpl vm = VirtualMachineImpl.createVirtualMachineForPID(Integer.parseInt(args[0]), 0); //process ID

        // Attaching server
//        String address = "host.docker.internal:8080"; // + args[1];
//        System.out.println(SERVER_READY);
//        System.out.println("Connecting to " + address);
//
//        final SocketTransportService socketTransportService = new SocketTransportService();
//        final Connection connection = socketTransportService.attach(address, 0, 0);
//
//        System.out.println("Connected to " + address);

        // Listening server
        final SocketTransportService socketTransportService = new SocketTransportService();
        final TransportService.ListenKey listenKey = socketTransportService.startListening(args[1]); //address

        System.err.println(WAITING_FOR_DEBUGGER + listenKey.address());


        // shutdown hook to clean-up the server in case of forced exit.
        Runtime.getRuntime().addShutdownHook(new Thread(
                new Runnable() {
                    public void run() {
                        try {
                            vm.dispose();
                            //connection.close();
                            socketTransportService.stopListening(listenKey);
                        } catch (IllegalArgumentException ignored) {
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }));

        // Listening server
        Connection connection = socketTransportService.accept(listenKey, 0, 0);
        socketTransportService.stopListening(listenKey);

        JDWPProxy.reply(connection, vm);
    }
}