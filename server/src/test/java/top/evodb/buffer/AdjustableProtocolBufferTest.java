/*
 * Copyright 2017-2018 The Evodb Project
 *
 * The Evodb Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package top.evodb.buffer;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author ynfeng
 */
public class AdjustableProtocolBufferTest {
    private static final int CHUNK_SIZE = 15;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);

    @Test
    public void testPutFixInt() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putFixInt(0, 1, 200);
    }

    @Test
    public void testGetFixInt() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putFixInt(0, 10, 200);
        long fixInt = protocolBuffer.getFixInt(0, 10);
        assertEquals(200, fixInt);
    }

    @Test
    public void testHasReadableBytes() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        boolean isReadable = protocolBuffer.hasReadableBytes();
        assertFalse(isReadable);
    }

    @Test
    public void testReadableBytes() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixInt(1, 200);
        int readableBytes = protocolBuffer.readableBytes();
        assertEquals(1, readableBytes);
    }

    @Test
    public void testWritableBytes() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixInt(1, 200);
        int writeableBytes = protocolBuffer.writableBytes();
        assertEquals(CHUNK_SIZE - 1, writeableBytes);
    }

    @Test
    public void testReadIndex() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixInt(1, 200);
        protocolBuffer.readFixInt(1);
        int readIndex = protocolBuffer.readIndex();
        assertEquals(1, readIndex);
    }

    @Test
    public void testSetWriteIndex() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixInt(1, 200);
        protocolBuffer.writeIndex(10);
    }

    @Test
    public void testSetReadIndex() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixInt(1, 200);
        protocolBuffer.readFixInt(1);
        protocolBuffer.readIndex(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetReadIndexWithBigIndex() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixInt(1, 200);
        protocolBuffer.readFixInt(1);
        protocolBuffer.readIndex(2);
    }

    @Test
    public void testPutLenencInt() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putLenencInt(0, 10);
        protocolBuffer.putLenencInt(0, 300);
        protocolBuffer.putLenencInt(0, (1 << 16) + 10);
        protocolBuffer.putLenencInt(0, (1 << 24) + 10);
        protocolBuffer.putLenencInt(0, (1 << 15) + 10);
    }

    @Test
    public void testGetLenencInt() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putLenencInt(0, 10);
        long rv = protocolBuffer.getLenencInt(0);
        assertEquals(10, rv);

        protocolBuffer.putLenencInt(0, 300);
        rv = protocolBuffer.getLenencInt(0);
        assertEquals(300, rv);

        protocolBuffer.putLenencInt(0, (1 << 16) + 10);
        rv = protocolBuffer.getLenencInt(0);
        assertEquals((1 << 16) + 10, rv);

        protocolBuffer.putLenencInt(0, (1 << 24) + 10);
        rv = protocolBuffer.getLenencInt(0);
        assertEquals((1 << 24) + 10, rv);

        protocolBuffer.putLenencInt(0, (1 << 25) + 10);
        rv = protocolBuffer.getLenencInt(0);
        assertEquals((1 << 25) + 10, rv);
    }

    @Test
    public void testWriteLenencInt() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencInt(10);
        protocolBuffer.writeLenencInt(300);
        protocolBuffer.writeLenencInt((1 << 16) + 10);
        protocolBuffer.writeLenencInt((1 << 24) + 10);
        protocolBuffer.writeLenencInt((1 << 25) + 10);
    }

    @Test
    public void testReadLenencInt() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencInt(10);
        protocolBuffer.writeLenencInt(300);
        protocolBuffer.writeLenencInt((1 << 16) + 1);
        protocolBuffer.writeLenencInt((1 << 24) + 1);
        protocolBuffer.writeLenencInt((1 << 25) + 1);
        long rv;

        rv = protocolBuffer.readLenencInt();
        assertEquals(10, rv);

        rv = protocolBuffer.readLenencInt();
        assertEquals(300, rv);

        rv = protocolBuffer.readLenencInt();
        assertEquals((1 << 16) + 1, rv);

        rv = protocolBuffer.readLenencInt();
        assertEquals((1 << 24) + 1, rv);

        rv = protocolBuffer.readLenencInt();
        assertEquals((1 << 25) + 1, rv);
    }


    @Test
    public void testPutFixString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putFixString(0, "test string");
    }

    @Test
    public void testGetFixString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();

        protocolBuffer.putFixString(0, "test string");
        String rv = protocolBuffer.getFixString(0, 11);
        assertEquals("test string", rv);
    }

    @Test
    public void testWriteFixString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString("test string");
    }

    @Test
    public void testReadFixString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString("test string");

        String rv = protocolBuffer.readFixString(11);
        assertEquals("test string", rv);
    }

    @Test
    public void testPutLenencString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putLenencString(0, "test string");
    }

    @Test
    public void testGetLenencString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putLenencString(0, "test string");
        String rv = protocolBuffer.getLenencString(0);
        assertEquals("test string", rv);
    }

    @Test
    public void testWriteLenencString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencString("test string");
    }

    @Test
    public void testReadLenencString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencString("hello world");
        protocolBuffer.writeLenencString("world");
        String rv = protocolBuffer.readLenencString();
        assertEquals("hello world", rv);
        rv = protocolBuffer.readLenencString();
        assertEquals("world", rv);
    }

    @Test
    public void testPutNULString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putNULString(0, "test string");
    }

    @Test
    public void testGetNULString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putNULString(0, "test string");
        String rv = protocolBuffer.getNULString(0);
        assertEquals("test string", rv);
    }

    @Test
    public void testWriteNULString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeNULString("test string");
    }

    @Test
    public void testReadNULString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeNULString("test string");
        String rv = protocolBuffer.readNULString();
        assertEquals("test string", rv);
    }

    @Test
    public void testWriteBytes() {
        byte[] bytes = { 1, 2, 3, 4 };
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeBytes(bytes);
        protocolBuffer.writeBytes(2, bytes);
    }

    @Test
    public void testWriteByte() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeByte((byte) 1);
    }

    @Test
    public void testReadByte() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeByte((byte) 1);

        byte b = protocolBuffer.readByte();
        assertEquals(1, b);
    }

    @Test
    public void testReadBytes() {
        byte[] bytes = { 1, 2, 3, 4 };
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeBytes(bytes);
        byte[] rv = protocolBuffer.readBytes(4);
        assertEquals(bytes.length, rv.length);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], rv[i]);
        }
    }

    @Test
    public void testPutLenencBytes() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putLenencBytes(0, new byte[] { 1, 2, 3, 4 });
    }

    @Test
    public void testGetLenencBytes() {
        byte[] bytes = { 1, 2, 3, 4 };
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putLenencBytes(0, bytes);
        byte[] rv = protocolBuffer.getLenencBytes(0);
        assertEquals(rv.length, bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], rv[i]);
        }
    }

    @Test
    public void testWriteLenencBytes() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencBytes(new byte[] { 1, 2, 3, 4 });
    }

    @Test
    public void testReadLenencBytes() {
        byte[] bytes = { 1, 2, 3, 4 };
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencBytes(bytes);
        byte[] rv = protocolBuffer.readLenencBytes();
        assertEquals(rv.length, bytes.length);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals(bytes[i], rv[i]);
        }
    }

    @Test
    public void testClear() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencBytes(new byte[] { 1, 2, 3, 4 });
        assertEquals(5, protocolBuffer.writeIndex());
        protocolBuffer.clear();
        assertEquals(0, protocolBuffer.readIndex());
        assertEquals(0, protocolBuffer.writeIndex());
    }

    @Test
    public void testTransferToChannel() throws IOException {
        SocketChannel socketChannel = mock(SocketChannel.class);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencBytes(new byte[] { 1, 2, 3, 4 });
        int writed = protocolBuffer.transferToChannel(socketChannel);
        assertTrue(writed != -1);
    }

    @Test
    public void testTransferToChannelWith200Bytes() throws IOException {
        SocketChannel socketChannel = mock(SocketChannel.class);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        for (int i = 0; i < 20; i++) {
            protocolBuffer.writeBytes(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });
        }
        int writed = protocolBuffer.transferToChannel(socketChannel);
        assertTrue(writed != -1);
    }

    @Test
    public void testTransferToChannelWithError() throws IOException {
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(socketChannel.write((ByteBuffer) any())).thenReturn(-1);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString("test");
        int writed = protocolBuffer.transferToChannel(socketChannel);
        assertEquals(-1, writed);
    }

    @Test
    public void testTransferToChannelWith10Bytes() throws IOException {
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(socketChannel.write((ByteBuffer) any())).thenReturn(10);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeBytes(10, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });
        int writed = protocolBuffer.transferToChannel(socketChannel);
        assertEquals(10, writed);
    }

    @Test
    public void testTransferFromChannel() throws IOException {
        SocketChannel socketChannel = mock(SocketChannel.class);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        int readed = protocolBuffer.transferFromChannel(socketChannel);
        assertEquals(0, readed);
    }

    @Test
    public void testTransferFromChannelWithError() throws IOException {
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(socketChannel.read((ByteBuffer) any())).thenReturn(-1);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        int readed = protocolBuffer.transferFromChannel(socketChannel);
        assertEquals(-1, readed);
    }

    @Test
    public void testTransferFromChannelWith10Bytes() throws IOException {
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(socketChannel.read((ByteBuffer) any())).thenReturn(10);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        int readed = protocolBuffer.transferFromChannel(socketChannel);
        assertEquals(10, readed);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutBytesWithWrongIndex() throws IOException {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putBytes(-1, 10, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutBytesWithWrongLength() throws IOException {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putBytes(0, 12, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });
    }

    @Test(expected = IllegalStateException.class)
    public void testPutBytesWithRecyled() throws IOException {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        allocator.recyle(protocolBuffer);
        protocolBuffer.putBytes(-1, 10, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0 });
    }
}
