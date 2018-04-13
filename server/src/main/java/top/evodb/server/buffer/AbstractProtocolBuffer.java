/*
 * Copyright 2017-2018 The Evodb Project
 *
 *  The Evodb Project licenses this file to you under the Apache License,
 *  version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package top.evodb.server.buffer;


import java.util.HashMap;
import java.util.Map;

/**
 * The base class of the ProtocolBuffer.
 *
 * @author evodb
 */
public abstract class AbstractProtocolBuffer implements ProtocolBuffer {

    private int writeIndex;
    private int readIndex;
    private Map<String, PacketIterator> namedPacketIteratorMap;

    protected AbstractProtocolBuffer() {
        writeIndex = 0;
        readIndex = 0;
    }

    protected long getInt(int index, int length) {
        long rv = 0;
        int index0 = index;
        for (int i = 0; i < length; i++) {
            rv |= ((long) getByte(index0++) & 0xFF) << i * 8;
        }
        return rv;
    }

    @Override
    public boolean hasReadableBytes() {
        return readIndex < writeIndex;
    }

    @Override
    public int readableBytes() {
        return writeIndex - readIndex;
    }

    @Override
    public int writableBytes() {
        return capacity() - writeIndex;
    }

    @Override
    public int writeIndex() {
        return writeIndex;
    }

    @Override
    public int readIndex() {
        return readIndex;
    }

    @Override
    public void writeIndex(int writeIndex) {
        if (writeIndex < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.writeIndex = writeIndex;
    }

    @Override
    public void readIndex(int readIndex) {
        if (readIndex >= 0 && readIndex > writeIndex) {
            throw new IndexOutOfBoundsException();
        }
        this.readIndex = readIndex;
    }

    @Override
    public long getFixInt(int index, int length) {
        return getInt(index, length);
    }

    @Override
    public long readFixInt(int length) {
        long val = getInt(readIndex, length);
        readIndex += length;
        return val;
    }

    @Override
    public long getLenencInt(int index) {
        long len = getInt(index, 1) & 0xff;
        if (len == 0xfc) {
            return getInt(index + 1, 2);
        } else if (len == 0xfd) {
            return getInt(index + 1, 3);
        } else if (len == 0xfe) {
            return getInt(index + 1, 8);
        } else {
            return len;
        }
    }

    @Override
    public ProtocolBuffer compact() {
        compactInternalBuffer();
        writeIndex(writeIndex() - readIndex());
        readIndex(0);
        return this;
    }

    /**
     * Compat internal buffer
     */
    abstract void compactInternalBuffer();

    @Override
    public long readLenencInt() {
        int index = readIndex;
        long len = getInt(index, 1) & 0xff;
        if (len < 251) {
            readIndex += 1;
            return len;
        } else if (len == 0xfc) {
            readIndex += 3;
            return getInt(index + 1, 2);
        } else if (len == 0xfd) {
            readIndex += 4;
            return getInt(index + 1, 3);
        } else {
            readIndex += 9;
            return getInt(index + 1, 8);
        }
    }

    @Override
    public String getFixString(int index, int length) {
        byte[] bytes = getBytes(index, length);
        return new String(bytes);
    }

    @Override
    public String readFixString(int length) {
        byte[] bytes = getBytes(readIndex, length);
        readIndex += length;
        return new String(bytes);
    }

    @Override
    public String getLenencString(int index) {
        int strLen = (int) getLenencInt(index);
        int lenencLen = getLenencLength(strLen);
        byte[] bytes = getBytes(index + lenencLen, strLen);
        return new String(bytes);
    }

    @Override
    public String readLenencString() {
        int strLen = (int) getLenencInt(readIndex);
        int lenencLen = getLenencLength(strLen);
        byte[] bytes = getBytes(readIndex + lenencLen, strLen);
        readIndex += strLen + lenencLen;
        return new String(bytes);
    }

    @Override
    public String getNULString(int index) {
        int strLength = 0;
        int scanIndex = index;
        while (scanIndex < capacity()) {
            if (getByte(scanIndex++) == 0) {
                break;
            }
            strLength++;
        }
        byte[] bytes = getBytes(index, strLength);
        return new String(bytes);
    }

    @Override
    public String readNULString() {
        String rv = getNULString(readIndex);
        readIndex += rv.getBytes().length + 1;
        return rv;
    }

    @Override
    public ProtocolBuffer putFixInt(int index, int length, long val) {
        int index0 = index;
        for (int i = 0; i < length; i++) {
            byte b = (byte) (val >> i * 8 & 0xFF);
            putByte(index0++, b);
        }
        return this;
    }

    @Override
    public ProtocolBuffer writeFixInt(int length, long val) {
        putFixInt(writeIndex, length, val);
        writeIndex += length;
        return this;
    }

    @Override
    public ProtocolBuffer putLenencInt(int index, long val) {
        if (val < 251) {
            putByte(index, (byte) val);
        } else if (val >= 251 && val < 1 << 16) {
            putByte(index, (byte) 0xfc);
            putFixInt(index + 1, 2, val);
        } else if (val >= 1 << 16 && val < 1 << 24) {
            putByte(index, (byte) 0xfd);
            putFixInt(index + 1, 3, val);
        } else {
            putByte(index, (byte) 0xfe);
            putFixInt(index + 1, 8, val);
        }
        return this;
    }

    @Override
    public ProtocolBuffer writeLenencInt(long val) {
        if (val < 251) {
            putByte(writeIndex++, (byte) val);
        } else if (val >= 251 && val < 1 << 16) {
            putByte(writeIndex++, (byte) 0xfc);
            putFixInt(writeIndex, 2, val);
            writeIndex += 2;
        } else if (val >= 1 << 16 && val < 1 << 24) {
            putByte(writeIndex++, (byte) 0xfd);
            putFixInt(writeIndex, 3, val);
            writeIndex += 3;
        } else {
            putByte(writeIndex++, (byte) 0xfe);
            putFixInt(writeIndex, 8, val);
            writeIndex += 8;
        }
        return this;
    }

    @Override
    public ProtocolBuffer putFixString(int index, String val) {
        putBytes(index, val.getBytes());
        return this;
    }

    @Override
    public ProtocolBuffer writeFixString(String val) {
        putBytes(writeIndex, val.getBytes());
        writeIndex += val.getBytes().length;
        return this;
    }

    @Override
    public ProtocolBuffer putLenencString(int index, String val) {
        putLenencInt(index, val.getBytes().length);
        int lenencLen = getLenencLength(val.getBytes().length);
        putFixString(index + lenencLen, val);
        return this;
    }

    @Override
    public ProtocolBuffer writeLenencString(String val) {
        putLenencString(writeIndex, val);
        int lenencLen = getLenencLength(val.getBytes().length);
        writeIndex += lenencLen + val.getBytes().length;
        return this;
    }

    @Override
    public ProtocolBuffer putNULString(int index, String val) {
        putFixString(index, val);
        putByte(val.getBytes().length + index, (byte) 0);
        return this;
    }

    @Override
    public ProtocolBuffer writeNULString(String val) {
        putNULString(writeIndex, val);
        writeIndex += val.getBytes().length + 1;
        return this;
    }

    @Override
    public byte[] readBytes(int length) {
        byte[] bytes = getBytes(readIndex, length);
        readIndex += length;
        return bytes;
    }

    @Override
    public ProtocolBuffer putBytes(int index, byte[] bytes) {
        return putBytes(index, bytes.length, bytes);
    }

    @Override
    public ProtocolBuffer putByte(int index, byte val) {
        return putBytes(index, new byte[] {val});
    }

    @Override
    public ProtocolBuffer writeBytes(byte[] bytes) {
        writeBytes(bytes.length, bytes);
        return this;
    }

    @Override
    public ProtocolBuffer writeBytes(int length, byte[] bytes) {
        putBytes(writeIndex, length, bytes);
        writeIndex += length;
        return this;
    }

    @Override
    public byte readByte() {
        byte val = getByte(readIndex);
        readIndex++;
        return val;
    }

    @Override
    public byte[] getLenencBytes(int index) {
        int len = (int) getLenencInt(index);
        return getBytes(index + getLenencLength(len), len);
    }

    @Override
    public byte[] readLenencBytes() {
        int len = (int) getLenencInt(readIndex);
        byte[] bytes = getBytes(readIndex + getLenencLength(len), len);
        readIndex += getLenencLength(len) + len;
        return bytes;
    }

    @Override
    public ProtocolBuffer putLenencBytes(int index, byte[] bytes) {
        putLenencInt(index, bytes.length);
        int offset = getLenencLength(bytes.length);
        putBytes(index + offset, bytes);
        return this;
    }

    @Override
    public void clear() {
        if (namedPacketIteratorMap != null) {
            namedPacketIteratorMap.clear();
            namedPacketIteratorMap = null;
        }
        writeIndex(0);
        readIndex(0);
    }

    @Override
    public ProtocolBuffer writeLenencBytes(byte[] bytes) {
        putLenencInt(writeIndex, bytes.length);
        int offset = getLenencLength(bytes.length);
        putBytes(writeIndex + offset, bytes);
        writeIndex += offset + bytes.length;
        return this;
    }

    @Override
    public ProtocolBuffer writeByte(byte val) {
        putByte(writeIndex, val);
        writeIndex++;
        return this;
    }

    @Override
    public PacketIterator packetIterator() {
        return packetIterator("default");
    }

    @Override
    public PacketIterator packetIterator(String name) {
        if (namedPacketIteratorMap == null) {
            namedPacketIteratorMap = new HashMap<>();
        }
        return namedPacketIteratorMap.computeIfAbsent(name, k -> new SimplePacketIterator(this));
    }
}
