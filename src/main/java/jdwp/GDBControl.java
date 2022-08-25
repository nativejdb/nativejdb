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

import com.sun.jdi.connect.spi.Connection;
import gdb.mi.service.command.AbstractMIControl;
import gdb.mi.service.command.commands.MICommand;
import gdb.mi.service.command.commands.MISymbolInfoFunctions;
import gdb.mi.service.command.output.MiSymbolInfoFunctionsInfo;
import jdwp.jdi.VirtualMachineImpl;
import jdwp.model.ReferenceType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GDBControl extends AbstractMIControl {
    private boolean initialized = false;
    private final Connection myConnection;
    public VirtualMachineImpl vm;


    int sizeofFieldRef = 8;
    int sizeofMethodRef = 8;
    int sizeofObjectRef = 8;
    int sizeofClassRef = 8;
    int sizeofFrameRef = 8;

    OutputStream gdbInput = null;
    InputStream  gdbOutput = null;
    InputStream  gdbError = null;
    BufferedReader outputReader = null;

    private Map<Long, ReferenceType> referenceTypes = new HashMap<>();

    public GDBControl(Connection myConnection, VirtualMachineImpl vm)  {
        super(); //AbstractMIControl sets up command factory
        this.myConnection = myConnection;
        this.vm = vm;

        try {
            String exec = System.getProperty("native.exec");
            String src = System.getProperty("native.src");
            ProcessBuilder builder = new ProcessBuilder("gdb", "--interpreter=mi", exec);
            builder.redirectErrorStream(true); // so we can ignore the error stream

            Process process = builder.start();
            gdbInput = process.getOutputStream();
            gdbOutput = process.getInputStream();
            gdbError = process.getErrorStream();
            outputReader = new BufferedReader(new InputStreamReader(gdbOutput, "UTF-8"));

            byte[] com = ("-environment-directory "+src+"\n").getBytes();
            gdbInput.write(com, 0, com.length);
            gdbInput.flush();
            System.out.println(getGDBOutput());

            com = "-gdb-set mi-async on\n".getBytes();
            gdbInput.write(com, 0, com.length);
            gdbInput.flush();
            System.out.println(getGDBOutput());

            com = "start&\n".getBytes();
            gdbInput.write(com, 0, com.length);
            gdbInput.flush();
            System.out.println(getGDBOutput());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialized() {
        initialized = true;
    }

    public void waitForInitialization() {
        while (!initialized) {
            try {
                Thread.sleep(100);

            } catch (InterruptedException e) {

            }
        }
        loadSymbols();
    }

    private void loadSymbols() {
        MICommand<MiSymbolInfoFunctionsInfo> cmd = getCommandFactory().createMiSymbolInfoFunctions();
        int token = JDWP.getNewTokenId();
        queueCommand(token, cmd);
        MiSymbolInfoFunctionsInfo response = (MiSymbolInfoFunctionsInfo) getResponse(token, JDWP.DEF_REQUEST_TIMEOUT);
        Translator.translateReferenceTypes(referenceTypes, response);

    }

    void flush() {
        try {
            int no = gdbOutput.available();
            byte[] buffer = new byte[4000];
            while (no > 0) {
                int n = gdbOutput.read(buffer, 0, Math.min(no, buffer.length));
                System.out.write(buffer);
                no = gdbOutput.available();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getGDBOutput() {
        String result = "";
        try {
            boolean done = false;
            while(!done) {
                if (outputReader.ready()) {
                    result += outputReader.readLine() + "\n";
                    if (result.contains("(gdb)")) {
                        done = true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void sendToTarget(Packet pkt) {
        try {
            myConnection.writePacket(pkt.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Long, ReferenceType> getReferenceTypes() {
        return referenceTypes;
    }
}