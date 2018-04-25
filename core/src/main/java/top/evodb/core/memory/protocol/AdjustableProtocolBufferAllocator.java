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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import top.evodb.core.memory.heap.ByteChunkAllocator;

/**
 * @author evodb
 */
public class AdjustableProtocolBufferAllocator implements ProtocolBufferAllocator<AdjustableProtocolBuffer> {

    private final int chunkSize;
    private final LinkedList<AdjustableProtocolBuffer> freeProtocolBufferList;
    private final LinkedList<ByteBuffer> freeByteBufferList;
    private final ByteChunkAllocator byteChunkAllocator;

    public AdjustableProtocolBufferAllocator(int chunkSize, ByteChunkAllocator byteChunkAllocator) {
        this.chunkSize = chunkSize;
        freeProtocolBufferList = new LinkedList<>();
        freeByteBufferList = new LinkedList<>();
        this.byteChunkAllocator = byteChunkAllocator;
    }

    @Override
    public AdjustableProtocolBuffer allocate() {
        AdjustableProtocolBuffer adjustableProtocolBuffer = freeProtocolBufferList.poll();
        if (adjustableProtocolBuffer == null) {
            adjustableProtocolBuffer = new AdjustableProtocolBuffer(this, byteChunkAllocator);
        }
        adjustableProtocolBuffer.setRecyleFlag(false);
        return adjustableProtocolBuffer;
    }

    @Override
    public boolean recyle(AdjustableProtocolBuffer buffer) {
        if (buffer.getAllocator() == this) {
            buffer.clear();
            buffer.setRecyleFlag(true);
            return freeProtocolBufferList.offer(buffer);
        }
        return false;
    }

    protected ByteBuffer allocateByteBuffer() {
        return ByteBuffer.allocateDirect(chunkSize);
    }

    protected boolean recyleAllocateByteBuffer(ByteBuffer byteBuffer) {
        if (byteBuffer.capacity() == chunkSize) {
            return freeByteBufferList.offer(byteBuffer);
        }
        return false;
    }

    protected int getChunkSize() {
        return chunkSize;
    }
}
