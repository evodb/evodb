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

package top.evodb.core.memory.protocol;


import java.util.HashMap;
import java.util.Map;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;

/**
 * The base class of the ProtocolBuffer.
 *
 * @author evodb
 */
public abstract class AbstractProtocolBuffer implements ProtocolBuffer {

    private int writeIndex;
    private int readIndex;
    private Map<String, PacketIterator> namedPacketIteratorMap;
    private ByteChunkAllocator byteChunkAllocator;

    protected AbstractProtocolBuffer(ByteChunkAllocator byteChunkAllocator) {
        this.byteChunkAllocator = byteChunkAllocator;
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
    public ByteChunk getFixString(int index, int length) {
        ByteChunk byteChunk = byteChunkAllocator.alloc(length);
        getBytes(byteChunk, index);
        return byteChunk;
    }

    @Override
    public ByteChunk readFixString(int length) {
        ByteChunk byteChunk = byteChunkAllocator.alloc(length);
        getBytes(byteChunk, readIndex);
        readIndex += length;
        return byteChunk;
    }

    @Override
    public ByteChunk getLenencString(int index) {
        int strLen = (int) getLenencInt(index);
        int lenencLen = getLenencLength(strLen);
        ByteChunk byteChunk = byteChunkAllocator.alloc(strLen);
        getBytes(byteChunk, index + lenencLen);
        return byteChunk;
    }

    @Override
    public ByteChunk readLenencString() {
        int strLen = (int) getLenencInt(readIndex);
        int lenencLen = getLenencLength(strLen);
        ByteChunk byteChunk = byteChunkAllocator.alloc(strLen);
        getBytes(byteChunk, readIndex + lenencLen);
        readIndex += strLen + lenencLen;
        return byteChunk;
    }

    @Override
    public ByteChunk getNULString(int index) {
        int strLength = 0;
        int scanIndex = index;
        while (scanIndex < capacity()) {
            if (getByte(scanIndex++) == 0) {
                break;
            }
            strLength++;
        }
        ByteChunk byteChunk = byteChunkAllocator.alloc(strLength);
        getBytes(byteChunk, index);
        return byteChunk;
    }

    @Override
    public ByteChunk readNULString() {
        ByteChunk byteChunk = getNULString(readIndex);
        readIndex += byteChunk.getLength() + 1;
        return byteChunk;
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
    public ProtocolBuffer putFixString(int index, ByteChunk byteChunk) {
        putBytes(index, byteChunk);
        return this;
    }

    @Override
    public ProtocolBuffer writeFixString(ByteChunk byteChunk) {
        putBytes(writeIndex, byteChunk);
        writeIndex += byteChunk.getLength();
        return this;
    }

    @Override
    public ProtocolBuffer putLenencString(int index, ByteChunk byteChunk) {
        putLenencInt(index, byteChunk.getLength());
        int lenencLen = getLenencLength(byteChunk.getLength());
        putFixString(index + lenencLen, byteChunk);
        return this;
    }

    @Override
    public ProtocolBuffer writeLenencString(ByteChunk val) {
        putLenencString(writeIndex, val);
        int lenencLen = getLenencLength(val.getLength());
        writeIndex += lenencLen + val.getLength();
        return this;
    }

    @Override
    public ProtocolBuffer putNULString(int index, ByteChunk val) {
        putFixString(index, val);
        putByte(val.getLength() + index, (byte) 0);
        return this;
    }

    @Override
    public ProtocolBuffer writeNULString(ByteChunk val) {
        putNULString(writeIndex, val);
        writeIndex += val.getLength() + 1;
        return this;
    }

    @Override
    public int readBytes(ByteChunk byteChunk) {
        int length = getBytes(byteChunk, readIndex);
        readIndex += length;
        return length;
    }

    @Override
    public ProtocolBuffer putBytes(int index, ByteChunk byteChunk) {
        return putBytes(index, byteChunk.getLength(), byteChunk);
    }

    @Override
    public ProtocolBuffer putByte(int index, byte val) {
        ByteChunk byteChunk = byteChunkAllocator.alloc(1);
        byteChunk.append(val);
        ProtocolBuffer rv = putBytes(index, byteChunk);
        byteChunk.recycle();
        return rv;
    }

    @Override
    public ProtocolBuffer writeBytes(ByteChunk byteChunk) {
        writeBytes(byteChunk.getLength(), byteChunk);
        return this;
    }

    @Override
    public ProtocolBuffer writeBytes(int length, ByteChunk byteChunk) {
        putBytes(writeIndex, length, byteChunk);
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
    public ByteChunk getLenencBytes(int index) {
        int len = (int) getLenencInt(index);
        ByteChunk byteChunk = byteChunkAllocator.alloc(len);
        getBytes(byteChunk, index + getLenencLength(len));
        return byteChunk;
    }

    @Override
    public ByteChunk readLenencBytes() {
        int len = (int) getLenencInt(readIndex);
        ByteChunk byteChunk = byteChunkAllocator.alloc(len);
        getBytes(byteChunk, readIndex + getLenencLength(len));
        readIndex += getLenencLength(len) + len;
        return byteChunk;
    }

    @Override
    public ProtocolBuffer putLenencBytes(int index, ByteChunk byteChunk) {
        putLenencInt(index, byteChunk.getLength());
        int offset = getLenencLength(byteChunk.getLength());
        putBytes(index + offset, byteChunk);
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
    public ProtocolBuffer writeLenencBytes(ByteChunk byteChunk) {
        putLenencInt(writeIndex, byteChunk.getLength());
        int offset = getLenencLength(byteChunk.getLength());
        putBytes(writeIndex + offset, byteChunk);
        writeIndex += offset + byteChunk.getLength();
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

    public ByteChunkAllocator getByteChunkAllocator() {
        return byteChunkAllocator;
    }
}
