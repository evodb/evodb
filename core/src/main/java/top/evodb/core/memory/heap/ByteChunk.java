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
    private byte[] buf;

    public ByteChunk(BuddyAllocator buddyAllocator, byte[] buf, int start, int end, int nodeIndex) {
        this.start = start;
        this.end = end;
        this.buf = buf;
        this.nodeIndex = nodeIndex;
        recyled = false;
        this.buddyAllocator = buddyAllocator;
    }

    public void append(byte[] bytes, int offset, int size) {
        checkState();
        System.arraycopy(bytes, offset, buf, getOffset(), size);
    }

    protected void reuse() {
        checkState();
        recyled = false;
    }

    @Override
    public void setOffset(int offset) {
        checkState();
        super.setOffset(offset);
    }

    private void checkState() {
        if (isRecyled()) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void recycle() {
        checkState();
        super.recycle();
        buddyAllocator.free(this);
        recyled = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteChunk)) {
            return false;
        }
        ByteChunk byteChunk = (ByteChunk) o;
        if (byteChunk.getLength() != getLength()) {
            return false;
        }
        int byteChunkStart = byteChunk.getStart();
        boolean isEquals = true;
        for (int i = getStart(); i < getEnd(); i++) {
            if (buf[i] != byteChunk.buf[byteChunkStart++]) {
                isEquals = false;
                break;
            }
        }
        return isEquals;
    }
}
