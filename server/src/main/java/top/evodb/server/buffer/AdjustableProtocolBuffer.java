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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * When the buffer space is insufficient automatically grow. When {@link ProtocolBuffer#compact()}
 * or {@link ProtocolBuffer#clear()} called ,discardable space will be released.
 *
 * @author evodb
 */
public class AdjustableProtocolBuffer extends AbstractProtocolBuffer {

    private static final int INITIAL_SLOT_SIZE = 5;
    private static final int SLOT_INC_STEP = 5;

    private final AdjustableProtocolBufferAllocator allocator;
    private final int chunkSize;
    private ByteBuffer[] slots = new ByteBuffer[INITIAL_SLOT_SIZE];
    private int emptySlotIdx;
    private int capacity;
    private boolean recyleFlag;

    protected AdjustableProtocolBuffer(AdjustableProtocolBufferAllocator allocator) {
        this.allocator = allocator;
        emptySlotIdx = 0;
        capacity = 0;
        chunkSize = allocator.getChunkSize();
    }

    @Override
    public int transferToChannel(SocketChannel socketChannel) throws IOException {
        return transferToChannel(socketChannel, writeIndex() - readIndex());
    }

    @Override
    public int transferToChannel(SocketChannel socketChannel, int length) throws IOException {
        check(readIndex(), length);
        int totalWritedBytes = 0;
        int startSlotIdx = toSlotIndex(readIndex());
        int endSlotIdx = toSlotIndex(readIndex() + length);
        for (int i = startSlotIdx; i <= endSlotIdx; i++) {
            int startIndex = toInternalIndex(readIndex());
            int endIndex;
            int writed;
            if (startIndex + length - totalWritedBytes < chunkSize) {
                endIndex = toInternalIndex(readIndex() + length);
            } else {
                endIndex = chunkSize - 1;
            }
            ByteBuffer byteBuffer = slots[i];
            byteBuffer.limit(endIndex);
            byteBuffer.position(startIndex);
            writed = socketChannel.write(byteBuffer);
            readIndex(readIndex() + writed);
            totalWritedBytes += writed;
            if (writed == 0) {
                break;
            } else if (writed == -1) {
                return -1;
            } else if (totalWritedBytes == length) {
                break;
            }
        }
        return totalWritedBytes;
    }

    @Override
    public int transferFromChannel(SocketChannel socketChannel) throws IOException {
        check(writeIndex(), chunkSize);
        ensureSpace(writeIndex(), chunkSize);
        int totalReadBytes = 0;
        for (; ; ) {
            ByteBuffer byteBuffer = fromSlot(writeIndex());
            byteBuffer.limit(allocator.getChunkSize());
            byteBuffer.position(toInternalIndex(writeIndex()));
            int readed = socketChannel.read(byteBuffer);
            totalReadBytes += readed;
            if (readed == -1) {
                return -1;
            } else if (readed == 0) {
                return totalReadBytes;
            } else if (byteBuffer.remaining() == 0) {
                writeIndex(writeIndex() + readed);
                ensureSpace(writeIndex(), chunkSize);
            } else {
                writeIndex(writeIndex() + readed);
                return totalReadBytes;
            }
        }
    }

    @Override
    public byte[] getBytes(int index, int length) {
        check(index, length);
        byte[] bytes = new byte[length];
        ByteBuffer byteBuffer = fromSlot(index);
        int index0 = toInternalIndex(index);
        int offset = 0;
        byteBuffer.limit(chunkSize);
        byteBuffer.position(index0);
        for (; ; ) {
            int readableLength = byteBuffer.limit() - byteBuffer.position();
            if (readableLength >= length) {
                byteBuffer.get(bytes, offset, length);
                break;
            } else {
                byteBuffer.get(bytes, offset, readableLength);
                length -= readableLength;
                index0 = toInternalIndex(index + readableLength);
                offset += readableLength;
                index += readableLength;
                byteBuffer = fromSlot(index);
                byteBuffer.limit(chunkSize);
                byteBuffer.position(index0);
            }
        }
        return bytes;
    }

    @Override
    public ProtocolBuffer putBytes(int index, int length, byte[] bytes) {
        ensureSpace(index, length);
        check(index, length);
        if (length > 0 && length > bytes.length) {
            throw new IndexOutOfBoundsException();
        }
        ByteBuffer byteBuffer = fromSlot(index);
        int index0 = toInternalIndex(index);
        int offset = 0;
        byteBuffer.limit(chunkSize);
        byteBuffer.position(index0);
        for (; ; ) {
            int writeableLength = byteBuffer.limit() - byteBuffer.position();
            if (writeableLength >= length) {
                byteBuffer.put(bytes, offset, length);
                break;
            } else {
                byteBuffer.put(bytes, offset, writeableLength);
                length -= writeableLength;
                index0 = toInternalIndex(index + writeableLength);
                offset += writeableLength;
                index += writeableLength;
                byteBuffer = fromSlot(index);
                byteBuffer.limit(chunkSize);
                byteBuffer.position(index0);
            }
        }
        return this;
    }

    private void check(int index, int length) {
        if (recyleFlag) {
            throw new IllegalStateException("Protocol buffer has been recyled.");
        }
        if (index < 0 || length < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    private ByteBuffer fromSlot(int index) {
        int slotIdx = toSlotIndex(index);
        return slots[slotIdx];
    }

    private int toSlotIndex(int index) {
        return index / chunkSize;
    }

    private int toInternalIndex(int index) {
        return index % chunkSize;
    }

    private void ensureSpace(int index, int reqSpace) {
        while (index + reqSpace >= capacity) {
            ByteBuffer byteBuffer = allocator.allocateByteBuffer();
            putIntoSlot(byteBuffer);
            capacity += byteBuffer.capacity();
        }
    }

    private void putIntoSlot(ByteBuffer byteBuffer) {
        int slotIdx = requestSlot();
        slots[slotIdx] = byteBuffer;

    }

    private int requestSlot() {
        if (emptySlotIdx >= slots.length) {
            ByteBuffer[] newSlots = new ByteBuffer[slots.length + SLOT_INC_STEP];
            System.arraycopy(slots, 0, newSlots, 0, slots.length);
            slots = newSlots;
        }
        return emptySlotIdx++;
    }

    @Override
    void compactInternalBuffer() {
        int compactSize = readIndex();
        int numOfCompatBuffers = compactSize / chunkSize;
        int internalCompactSize = compactSize % chunkSize;
        int i;
        for (i = 0; i < numOfCompatBuffers; i++) {
            ByteBuffer byteBuffer = slots[i];
            byteBuffer.clear();
            allocator.recyleAllocateByteBuffer(byteBuffer);
            slots[i] = null;
        }
        if (internalCompactSize != 0) {
            ByteBuffer byteBuffer = slots[i];
            byteBuffer.position(internalCompactSize);
            byteBuffer.limit(chunkSize);
            byteBuffer.compact();
        }
        if (i > 0) {
            int newNumOfSlots = slots.length - i;
            ByteBuffer[] newSlots;
            if (newNumOfSlots < INITIAL_SLOT_SIZE) {
                newSlots = new ByteBuffer[INITIAL_SLOT_SIZE];
            } else {
                newSlots = new ByteBuffer[newNumOfSlots];
            }
            System.arraycopy(slots, i, newSlots, 0, newNumOfSlots);
            slots = newSlots;
        }
    }

    @Override
    public int capacity() {
        return capacity;
    }

    public AdjustableProtocolBufferAllocator getAllocator() {
        return allocator;
    }

    protected void setRecyleFlag(boolean recyleFlag) {
        this.recyleFlag = recyleFlag;
    }
}
