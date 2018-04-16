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

package top.evodb.core.buffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * @author evodb
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

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetWriteIndexWithNegative() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeIndex(-1);
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
        byte[] rv = new byte[4];
        protocolBuffer.readBytes(rv);
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
        when(socketChannel.write((ByteBuffer) ArgumentMatchers.any())).thenReturn(-1);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString("test");
        int writed = protocolBuffer.transferToChannel(socketChannel);
        assertEquals(-1, writed);
    }

    @Test
    public void testTransferToChannelWith10Bytes() throws IOException {
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(socketChannel.write((ByteBuffer) ArgumentMatchers.any())).thenReturn(10);
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
        when(socketChannel.read((ByteBuffer) ArgumentMatchers.any())).thenReturn(-1);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        int readed = protocolBuffer.transferFromChannel(socketChannel);
        assertEquals(-1, readed);
    }

    @Test
    public void testTransferFromChannelFullByteBuffer() throws IOException {
        final int[] readTime = { 1 };
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(15);

        SocketChannel socketChannel = mock(SocketChannel.class);
        when(socketChannel.read((ByteBuffer) ArgumentMatchers.any())).thenReturn(1);

        AdjustableProtocolBufferAllocator spyAllocator = Mockito.spy(
                (AdjustableProtocolBufferAllocator) allocator);
        when(spyAllocator.allocateByteBuffer()).thenReturn(byteBuffer);

        doAnswer(readBuffer -> {
            if (readTime[0] == 1) {
                byteBuffer.limit(15);
                byteBuffer.position(15);
                readTime[0]++;
                return 15;
            } else {
                return 0;
            }
        }).when(socketChannel).read(byteBuffer);

        ProtocolBuffer protocolBuffer = new AdjustableProtocolBuffer(spyAllocator);
        int readed = protocolBuffer.transferFromChannel(socketChannel);
        assertEquals(15, readed);
    }

    @Test
    public void testTransferFromChannelWith10Bytes() throws IOException {
        SocketChannel socketChannel = mock(SocketChannel.class);
        when(socketChannel.read((ByteBuffer) ArgumentMatchers.any())).thenReturn(10);
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

    @Test
    public void testCompact() {
        String data = "1234567890";
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString(data);
        protocolBuffer.readFixInt(5);
        protocolBuffer.compact();
        String rv = protocolBuffer.readFixString(5);
        assertEquals("67890", rv);
    }

    @Test
    public void testCompactWithMultiBuffer() {
        String data = "123456789012345";
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);

        String rv;
        rv = protocolBuffer.readFixString(15);
        assertEquals("123456789012345", rv);
        rv = protocolBuffer.readFixString(15);
        assertEquals("123456789012345", rv);
        rv = protocolBuffer.readFixString(16);
        assertEquals("1234567890123451", rv);

        protocolBuffer.compact();

        rv = protocolBuffer.readFixString(14);
        assertEquals("23456789012345", rv);
    }

    @Test
    public void testCompactWithLargeData() {
        String data = "123456789012345";
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);
        protocolBuffer.writeFixString(data);

        String rv;
        rv = protocolBuffer.readFixString(15);
        assertEquals("123456789012345", rv);
        protocolBuffer.compact();
    }

    @Test
    public void testGetLencintLen() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        assertEquals(1, protocolBuffer.getLenencLength(1));
        assertEquals(3, protocolBuffer.getLenencLength(1 << 15));
        assertEquals(4, protocolBuffer.getLenencLength(1 << 20));
        assertEquals(9, protocolBuffer.getLenencLength(1 << 30));
    }
}
