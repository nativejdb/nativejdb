/*
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
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
 */

package jdwp;

import com.sun.jdi.InternalException;
import jdwp.model.MethodLocation;

import java.io.ByteArrayOutputStream;

public class PacketStream {
    final GDBControl gc;
    private int inCursor = 0;
    final Packet pkt;
    ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
    private boolean isCommitted = false;

    PacketStream(GDBControl gc, int id, int cmdSet, int cmd) {
        this.gc = gc;
        this.pkt = new Packet();
        pkt.id = id;
        pkt.cmdSet = (short) cmdSet;
        pkt.cmd = (short) cmd;
    }

    PacketStream(GDBControl gc, Packet pkt) {
        this.gc = gc;
        this.pkt = pkt;
        this.isCommitted = true; /* read only stream */
    }

    // To be used for commands going back to the IDE
    PacketStream(GDBControl gc) {
        this.gc = gc;
        this.pkt = new Packet(Packet.NoFlags);
        pkt.cmdSet = (short) 64;
        pkt.cmd = (short) 100;
    }


    int id() {
        return pkt.id;
    }

    public void send() {
        if (!isCommitted) {
            pkt.data = dataStream.toByteArray();
            gc.sendToTarget(pkt);
            isCommitted = true;
        }
    }

    public void writeBoolean(boolean data) {
        if (data) {
            dataStream.write(1);
        } else {
            dataStream.write(0);
        }
    }

    public void writeByte(byte data) {
        dataStream.write(data);
    }

    public void writeChar(char data) {
        dataStream.write((byte) ((data >>> 8) & 0xFF));
        dataStream.write((byte) ((data >>> 0) & 0xFF));
    }

    public void writeShort(short data) {
        dataStream.write((byte) ((data >>> 8) & 0xFF));
        dataStream.write((byte) ((data >>> 0) & 0xFF));
    }

    public void writeInt(int data) {
        dataStream.write((byte) ((data >>> 24) & 0xFF));
        dataStream.write((byte) ((data >>> 16) & 0xFF));
        dataStream.write((byte) ((data >>> 8) & 0xFF));
        dataStream.write((byte) ((data >>> 0) & 0xFF));
    }

    public void writeLong(long data) {
        dataStream.write((byte) ((data >>> 56) & 0xFF));
        dataStream.write((byte) ((data >>> 48) & 0xFF));
        dataStream.write((byte) ((data >>> 40) & 0xFF));
        dataStream.write((byte) ((data >>> 32) & 0xFF));

        dataStream.write((byte) ((data >>> 24) & 0xFF));
        dataStream.write((byte) ((data >>> 16) & 0xFF));
        dataStream.write((byte) ((data >>> 8) & 0xFF));
        dataStream.write((byte) ((data >>> 0) & 0xFF));
    }

    public void writeFloat(float data) {
        writeInt(Float.floatToIntBits(data));
    }

    public void writeDouble(double data) {
        writeLong(Double.doubleToLongBits(data));
    }

    void writeID(int size, long data) {
        switch (size) {
            case 8:
                writeLong(data);
                break;
            case 4:
                writeInt((int) data);
                break;
            case 2:
                writeShort((short) data);
                break;
            default:
                throw new UnsupportedOperationException("JDWP: ID size not supported: " + size);
        }
    }

    void writeNullObjectRef() {
        writeObjectRef(0);
    }

    public void writeObjectRef(long data) {
        writeID(gc.sizeofObjectRef, data);
    }

    void writeClassRef(long data) {
        writeID(gc.sizeofClassRef, data);
    }

    void writeMethodRef(long data) {
        writeID(gc.sizeofMethodRef, data);
    }

    void writeFieldRef(long data) {
        writeID(gc.sizeofFieldRef, data);
    }

    void writeFrameRef(long data) {
        writeID(gc.sizeofFrameRef, data);
    }

    void writeByteArray(byte[] data) {
        dataStream.write(data, 0, data.length);
    }

    void writeStringOrEmpty(String string) {
        if (string == null) {
            string = "";
        }
        writeString(string);
    }

    public void writeString(String string) {
        try {
            byte[] stringBytes = string.getBytes("UTF8");
            writeInt(stringBytes.length);
            writeByteArray(stringBytes);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new InternalException("Cannot convert string to UTF8 bytes");
        }
    }

    void writeLocation(MethodLocation location) {
        writeByte(JDWP.TypeTag.CLASS);
        writeClassRef(location.getMethod().getReferenceType().getUniqueID());
        writeMethodRef(location.getMethod().getUniqueID());
        writeLong(location.getLine());
    }

    public void setErrorCode(short errorCode) {
        pkt.errorCode = errorCode;
    }


    /**
     * Read byte represented as one bytes.
     */
    byte readByte() {
        byte ret = pkt.data[inCursor];
        inCursor += 1;
        return ret;
    }

    /**
     * Read boolean represented as one byte.
     */
    boolean readBoolean() {
        byte ret = readByte();
        return (ret != 0);
    }

    /**
     * Read char represented as two bytes.
     */
    char readChar() {
        int b1, b2;

        b1 = pkt.data[inCursor++] & 0xff;
        b2 = pkt.data[inCursor++] & 0xff;

        return (char) ((b1 << 8) + b2);
    }

    /**
     * Read short represented as two bytes.
     */
    short readShort() {
        int b1, b2;

        b1 = pkt.data[inCursor++] & 0xff;
        b2 = pkt.data[inCursor++] & 0xff;

        return (short) ((b1 << 8) + b2);
    }

    /**
     * Read int represented as four bytes.
     */
    int readInt() {
        int b1, b2, b3, b4;

        b1 = pkt.data[inCursor++] & 0xff;
        b2 = pkt.data[inCursor++] & 0xff;
        b3 = pkt.data[inCursor++] & 0xff;
        b4 = pkt.data[inCursor++] & 0xff;

        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
    }

    /**
     * Read long represented as eight bytes.
     */
    long readLong() {
        long b1, b2, b3, b4;
        long b5, b6, b7, b8;

        b1 = pkt.data[inCursor++] & 0xff;
        b2 = pkt.data[inCursor++] & 0xff;
        b3 = pkt.data[inCursor++] & 0xff;
        b4 = pkt.data[inCursor++] & 0xff;

        b5 = pkt.data[inCursor++] & 0xff;
        b6 = pkt.data[inCursor++] & 0xff;
        b7 = pkt.data[inCursor++] & 0xff;
        b8 = pkt.data[inCursor++] & 0xff;

        return ((b1 << 56) + (b2 << 48) + (b3 << 40) + (b4 << 32)
                + (b5 << 24) + (b6 << 16) + (b7 << 8) + b8);
    }

    /**
     * Read float represented as four bytes.
     */
    float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Read double represented as eight bytes.
     */
    double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * Read string represented as four byte length followed by
     * characters of the string.
     */
    String readString() {
        String ret;
        int len = readInt();

        try {
            ret = new String(pkt.data, inCursor, len, "UTF8");
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println(e);
            ret = "Conversion error!";
        }
        inCursor += len;
        return ret;
    }

    private long readID(int size) {
        switch (size) {
            case 8:
                return readLong();
            case 4:
                return readInt();
            case 2:
                return readShort();
            default:
                throw new UnsupportedOperationException("JDWP: ID size not supported: " + size);
        }
    }

    /**
     * Read object represented as vm specific byte sequence.
     */
    long readObjectRef() {
        return readID(gc.sizeofObjectRef);
    }

    long readClassRef() {
        return readID(gc.sizeofClassRef);
    }

    /**
     * Read method reference represented as vm specific byte sequence.
     */
    long readMethodRef() {
        return readID(gc.sizeofMethodRef);
    }

    /**
     * Read field reference represented as vm specific byte sequence.
     */
    long readFieldRef() {
        return readID(gc.sizeofFieldRef);
    }
    //
//    /**
//     * Read field represented as vm specific byte sequence.
//     */
//    Field readField() {
//        ReferenceTypeImpl refType = readReferenceType();
//        long fieldRef = readFieldRef();
//        return refType.getFieldMirror(fieldRef);
//    }
//
//    /**
//     * Read frame represented as vm specific byte sequence.
//     */
    long readFrameRef() {
        return readID(gc.sizeofFrameRef);
    }

}