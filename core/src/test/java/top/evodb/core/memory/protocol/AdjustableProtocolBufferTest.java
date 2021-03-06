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
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;

/**
 * @author evodb
 */
public class AdjustableProtocolBufferTest {

    private static final int CHUNK_SIZE = 15;
    private ByteChunkAllocator byteChunkAllocator = new ByteChunkAllocator(1024 * 1024);
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE, byteChunkAllocator);

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
        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);
        protocolBuffer.putFixString(0, byteChunk);
        byteChunk.recycle();
    }

    @Test
    public void testGetFixString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);
        protocolBuffer.putFixString(0, byteChunk);
        ByteChunk rv = protocolBuffer.getFixString(0, 11);
        assertEquals(byteChunk, rv);
        rv.recycle();
        byteChunk.recycle();
    }

    @Test
    public void testWriteFixString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);
        protocolBuffer.writeFixString(byteChunk);
        byteChunk.recycle();
    }

    @Test
    public void testReadFixString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();

        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);

        protocolBuffer.writeFixString(byteChunk);

        ByteChunk rv = protocolBuffer.readFixString(11);
        assertEquals(byteChunk, rv);

        rv.recycle();
        byteChunk.recycle();
    }

    @Test
    public void testPutLenencString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();

        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);

        protocolBuffer.putLenencString(0, byteChunk);
        byteChunk.recycle();
    }

    @Test
    public void testGetLenencString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);

        protocolBuffer.putLenencString(0, byteChunk);
        ByteChunk rv = protocolBuffer.getLenencString(0);
        assertEquals(byteChunk, rv);
        byteChunk.recycle();
        rv.recycle();
    }

    @Test
    public void testWriteLenencString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();

        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);

        protocolBuffer.writeLenencString(byteChunk);
        byteChunk.recycle();
    }

    @Test
    public void testReadLenencString() {
        ByteChunk hello = byteChunkAllocator.alloc(11);
        hello.append("hello world".getBytes(), 0, 11);

        ByteChunk world = byteChunkAllocator.alloc(5);
        world.append("world".getBytes(), 0, 5);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencString(hello);
        protocolBuffer.writeLenencString(world);

        ByteChunk rv = protocolBuffer.readLenencString();
        assertEquals(hello, rv);
        rv = protocolBuffer.readLenencString();
        assertEquals(world, rv);
    }

    @Test
    public void testPutNULString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);

        protocolBuffer.putNULString(0, byteChunk);
        byteChunk.recycle();
    }

    @Test
    public void testGetNULString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();

        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);

        protocolBuffer.putNULString(0, byteChunk);
        ByteChunk rv = protocolBuffer.getNULString(0);

        assertEquals(byteChunk, rv);
        byteChunk.recycle();
        rv.recycle();
    }

    @Test
    public void testWriteNULString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();

        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);

        protocolBuffer.writeNULString(byteChunk);
        byteChunk.recycle();
    }

    @Test
    public void testReadNULString() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        ByteChunk byteChunk = byteChunkAllocator.alloc(11);
        byteChunk.append("test string".getBytes(), 0, 11);

        protocolBuffer.writeNULString(byteChunk);
        ByteChunk rv = protocolBuffer.readNULString();
        assertEquals(byteChunk, rv);
        rv.recycle();
        byteChunk.recycle();
    }

    @Test
    public void testWriteBytes() {
        byte[] bytes = {1, 2, 3, 4};
        ByteChunk byteChunk = byteChunkAllocator.alloc(4);
        byteChunk.append(bytes, 0, 4);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeBytes(byteChunk);
        protocolBuffer.writeBytes(2, byteChunk);
        byteChunk.recycle();
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
        byte[] bytes = {1, 2, 3, 4};
        ByteChunk byteChunk = byteChunkAllocator.alloc(4);
        byteChunk.append(bytes, 0, 4);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeBytes(byteChunk);
        ByteChunk rv = byteChunkAllocator.alloc(4);
        protocolBuffer.readBytes(rv);
        assertEquals(byteChunk.getLength(), rv.getLength());
        assertEquals(byteChunk, rv);
        byteChunk.recycle();
        rv.recycle();
    }

    @Test
    public void testPutLenencBytes() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        byte[] bytes = {1, 2, 3, 4};
        ByteChunk byteChunk = byteChunkAllocator.alloc(4);
        byteChunk.append(bytes, 0, 4);

        protocolBuffer.putLenencBytes(0, byteChunk);
        byteChunk.recycle();
    }

    @Test
    public void testGetLenencBytes() {
        byte[] bytes = {1, 2, 3, 4};
        ByteChunk byteChunk = byteChunkAllocator.alloc(4);
        byteChunk.append(bytes, 0, 4);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putLenencBytes(0, byteChunk);

        ByteChunk rv = protocolBuffer.getLenencBytes(0);
        assertEquals(rv.getLength(), rv.getLength());
        assertEquals(byteChunk, rv);

        byteChunk.recycle();
        rv.recycle();
    }

    @Test
    public void testWriteLenencBytes() {
        byte[] bytes = {1, 2, 3, 4};
        ByteChunk byteChunk = byteChunkAllocator.alloc(4);
        byteChunk.append(bytes, 0, 4);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencBytes(byteChunk);
    }

    @Test
    public void testReadLenencBytes() {
        byte[] bytes = {1, 2, 3, 4};
        ByteChunk byteChunk = byteChunkAllocator.alloc(4);
        byteChunk.append(bytes, 0, 4);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencBytes(byteChunk);

        ByteChunk rv = protocolBuffer.readLenencBytes();
        assertEquals(rv.getLength(), byteChunk.getLength());
        assertEquals(byteChunk, rv);
        rv.recycle();
        byteChunk.recycle();
    }

    @Test
    public void testClear() {
        byte[] bytes = {1, 2, 3, 4};
        ByteChunk byteChunk = byteChunkAllocator.alloc(4);
        byteChunk.append(bytes, 0, 4);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencBytes(byteChunk);

        assertEquals(5, protocolBuffer.writeIndex());
        protocolBuffer.clear();
        assertEquals(0, protocolBuffer.readIndex());
        assertEquals(0, protocolBuffer.writeIndex());

        byteChunk.recycle();
    }

    @Test
    public void testTransferToChannel() throws IOException {
        byte[] bytes = {1, 2, 3, 4};
        ByteChunk byteChunk = byteChunkAllocator.alloc(4);
        byteChunk.append(bytes, 0, 4);

        SocketChannel socketChannel = mock(SocketChannel.class);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeLenencBytes(byteChunk);

        int writed = protocolBuffer.transferToChannel(socketChannel);
        assertTrue(writed != -1);
        byteChunk.recycle();
    }

    @Test
    public void testTransferToChannelWith200Bytes() throws IOException {
        byte[] bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        ByteChunk byteChunk = byteChunkAllocator.alloc(10);
        byteChunk.append(bytes, 0, 10);

        SocketChannel socketChannel = mock(SocketChannel.class);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        for (int i = 0; i < 20; i++) {
            protocolBuffer.writeBytes(byteChunk);
        }
        int writed = protocolBuffer.transferToChannel(socketChannel);
        assertTrue(writed != -1);
        byteChunk.recycle();
    }

    @Test
    public void testTransferToChannelWithError() throws IOException {
        ByteChunk byteChunk = byteChunkAllocator.alloc(4);
        byteChunk.append("test".getBytes(), 0, 4);

        SocketChannel socketChannel = mock(SocketChannel.class);
        when(socketChannel.write((ByteBuffer) ArgumentMatchers.any())).thenReturn(-1);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString(byteChunk);
        int writed = protocolBuffer.transferToChannel(socketChannel);
        assertEquals(-1, writed);
        byteChunk.recycle();
    }

    @Test
    public void testTransferToChannelWith10Bytes() throws IOException {
        byte[] bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        ByteChunk byteChunk = byteChunkAllocator.alloc(10);
        byteChunk.append(bytes, 0, 10);

        SocketChannel socketChannel = mock(SocketChannel.class);
        when(socketChannel.write((ByteBuffer) ArgumentMatchers.any())).thenReturn(10);
        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeBytes(10, byteChunk);
        int writed = protocolBuffer.transferToChannel(socketChannel);
        assertEquals(10, writed);
        byteChunk.recycle();
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
        final int[] readTime = {1};
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

        ProtocolBuffer protocolBuffer = new AdjustableProtocolBuffer(spyAllocator, byteChunkAllocator);
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
        byte[] bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        ByteChunk byteChunk = byteChunkAllocator.alloc(10);
        byteChunk.append(bytes, 0, 10);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putBytes(-1, 10, byteChunk);
        byteChunk.recycle();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPutBytesWithWrongLength() throws IOException {
        byte[] bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        ByteChunk byteChunk = byteChunkAllocator.alloc(10);
        byteChunk.append(bytes, 0, 10);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.putBytes(0, 12, byteChunk);
        byteChunk.recycle();
    }

    @Test(expected = IllegalStateException.class)
    public void testPutBytesWithRecyled() throws IOException {
        byte[] bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        ByteChunk byteChunk = byteChunkAllocator.alloc(10);
        byteChunk.append(bytes, 0, 10);

        ProtocolBuffer protocolBuffer = allocator.allocate();

        allocator.recyle(protocolBuffer);
        protocolBuffer.putBytes(-1, 10, byteChunk);
        byteChunk.recycle();
    }

    @Test
    public void testCompact() {
        ByteChunk byteChunk = byteChunkAllocator.alloc(10);
        byteChunk.append("1234567890".getBytes(), 0, 10);

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.readFixInt(5);
        protocolBuffer.compact();
        ByteChunk rv = protocolBuffer.readFixString(5);
        assertEquals("67890", rv.toString());
        byteChunk.recycle();
        rv.recycle();
    }

    @Test
    public void testCompactWithMultiBuffer() {
        String data = "123456789012345";
        ByteChunk byteChunk = byteChunkAllocator.alloc(data.length());
        byteChunk.append(data.getBytes(), 0, data.length());

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);

        ByteChunk rv;
        rv = protocolBuffer.readFixString(15);
        assertEquals("123456789012345", rv.toString());
        rv = protocolBuffer.readFixString(15);
        assertEquals("123456789012345", rv.toString());
        rv = protocolBuffer.readFixString(16);
        assertEquals("1234567890123451", rv.toString());

        protocolBuffer.compact();

        rv = protocolBuffer.readFixString(14);
        assertEquals("23456789012345", rv.toString());
        rv.recycle();
        byteChunk.recycle();
    }

    @Test
    public void testCompactWithLargeData() {
        String data = "123456789012345";
        ByteChunk byteChunk = byteChunkAllocator.alloc(data.length());
        byteChunk.append(data.getBytes(), 0, data.length());

        ProtocolBuffer protocolBuffer = allocator.allocate();
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);
        protocolBuffer.writeFixString(byteChunk);

        ByteChunk rv;
        rv = protocolBuffer.readFixString(15);
        assertEquals("123456789012345", rv.toString());
        protocolBuffer.compact();
        rv.recycle();
        byteChunk.recycle();
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
