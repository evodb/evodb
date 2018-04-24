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

package top.evodb.core.memory.heap;

import top.evodb.core.memory.BuddyAllocator;
import top.evodb.core.util.MemoryLeak;

/**
 * @author ynfeng
 */
public class LeakedAwareByteChunk extends ByteChunk {
    private ByteChunk byteChunk;
    private MemoryLeak memoryLeak;

    @Override
    byte getElement(int i) {
        return byteChunk.getElement(i);
    }

    public LeakedAwareByteChunk(ByteChunk byteChunk, MemoryLeak memoryLeak) {
        this.byteChunk = byteChunk;
        this.memoryLeak = memoryLeak;
        buf = byteChunk.buf;
        start = byteChunk.getStart();
        end = byteChunk.getEnd();
        offset = byteChunk.getOffset();
    }

    public MemoryLeak getMemoryLeak() {
        return memoryLeak;
    }

    public ByteChunk unwarp() {
        return byteChunk;
    }

    @Override
    public void append(byte[] bytes, int offset, int size) {
        byteChunk.append(bytes, offset, size);
    }

    @Override
    protected void reuse(int start, int end, int limit, int nodeIndex) {
        byteChunk.reuse(start, end, limit, nodeIndex);
    }

    @Override
    public void setOffset(int offset) {
        byteChunk.setOffset(offset);
        this.offset = offset;
    }

    @Override
    public void recycle() {
        byteChunk.checkState();
        byteChunk.buddyAllocator.free(this);
        byteChunk.setOffset(byteChunk.getStart());
        byteChunk.recyled = true;
    }

    @Override
    public boolean equals(Object o) {
        return byteChunk.equals(o);
    }

    @Override
    public AbstractChunk clone() throws CloneNotSupportedException {
        return byteChunk.clone();
    }

    @Override
    public int getOffset() {
        return byteChunk.getOffset();
    }

    @Override
    public int getLength() {
        return byteChunk.getLength();
    }

    @Override
    public int getNodeIndex() {
        return byteChunk.getNodeIndex();
    }

    @Override
    public int getStart() {
        return byteChunk.getStart();
    }

    @Override
    public int getEnd() {
        return byteChunk.getEnd();
    }

    @Override
    public int getRawLength() {
        return byteChunk.getRawLength();
    }

    @Override
    public BuddyAllocator getAllocator() {
        return byteChunk.getAllocator();
    }

    @Override
    public boolean isRecyled() {
        return byteChunk.isRecyled();
    }

    @Override
    public byte[] getByteArray() {
        return byteChunk.getByteArray();
    }

    @Override
    public String toString() {
        return byteChunk.toString();
    }
}
