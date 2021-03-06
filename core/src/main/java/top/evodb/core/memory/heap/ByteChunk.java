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

/**
 * @author evodb
 */
public class ByteChunk extends AbstractChunk {
    protected byte[] buf;

    protected ByteChunk(BuddyAllocator buddyAllocator, byte[] buf, int start, int end, int limit, int nodeIndex) {
        this.start = start;
        this.end = end;
        this.buf = buf;
        this.nodeIndex = nodeIndex;
        recyled = false;
        this.limit = limit;
        setOffset(start);
        this.buddyAllocator = buddyAllocator;
    }

    protected ByteChunk() {
    }

    public void append(byte b) {
        checkState();
        buf[getOffset()] = b;
    }

    public void append(ByteChunk byteChunk) {
        append(byteChunk.buf, byteChunk.getStart(), byteChunk.getLength());
    }

    public void append(String str) {
        append(str.getBytes(), 0, str.getBytes().length);
    }

    public void append(byte[] bytes, int offset, int size) {
        checkState();
        int size0 = size;
        if (getOffset() + size0 >= limit) {
            size0 -= getOffset() + size0 - limit;
        }
        System.arraycopy(bytes, offset, buf, getOffset(), size0);
    }

    @Override
    public String toString() {
        return new String(buf, start, getLength());
    }

    @Override
    byte getElement(int i) {
        return buf[i];
    }

    @Override
    public void setOffset(int offset) {
        checkState();
        super.setOffset(offset);
    }

    protected void checkState() {
        if (isRecyled()) {
            throw new IllegalStateException();
        }
    }

    public byte[] getRaw() {
        return buf;
    }

    @Override
    public void recycle() {
        checkState();
        super.recycle();
        buddyAllocator.free(this);
        recyled = true;
    }

    /**
     * Becareful GC!
     *
     * @return bytes
     */
    public byte[] getByteArray() {
        byte[] bytes = new byte[getLength()];
        System.arraycopy(buf, start, bytes, 0, getLength());
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractChunk)) {
            return false;
        }
        ByteChunk byteChunk = (ByteChunk) o;
        if (byteChunk.getLength() != getLength()) {
            return false;
        }
        int byteChunkStart = byteChunk.getStart();
        boolean isEquals = true;
        for (int i = getStart(); i < limit; i++) {
            if (buf[i] != byteChunk.buf[byteChunkStart++]) {
                isEquals = false;
                break;
            }
        }
        return isEquals;
    }
}
