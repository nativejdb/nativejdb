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

import java.io.IOException;

public class JDWPServer {
    static final String WAITING_FOR_DEBUGGER = "Waiting for debugger on: ";

    public static void main(String[] args) throws Exception {
        final SocketTransportService socketTransportService = new SocketTransportService();
        final TransportService.ListenKey listenKey = socketTransportService.startListening(args[0]); //address

        System.err.println(WAITING_FOR_DEBUGGER + listenKey.address());

        Connection connection = socketTransportService.accept(listenKey, 0, 0);

        socketTransportService.stopListening(listenKey);

        JDWPProxy.reply(connection);
    }
}