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
public abstract class AbstractChunk implements Cloneable {
    protected int start;
    protected int end;
    protected int offset;
    protected int nodeIndex;
    protected boolean recyled;
    protected int limit;
    protected BuddyAllocator<ByteChunk> buddyAllocator;
    protected boolean hasHashCode;
    private int hashCode;

    protected void reuse(int start, int end, int limit ,int nodeIndex) {
        recyled = false;
        this.nodeIndex = nodeIndex;
        this.start = start;
        this.end = end;
        offset = start;
        this.limit = limit;
    }

    @Override
    public int hashCode() {
        if (hasHashCode) {
            return hashCode;
        }
        int code = 0;

        code = hash();
        hashCode = code;
        hasHashCode = true;
        return code;
    }


    public int hash() {
        int code = 0;
        for (int i = start; i < end; i++) {
            code = code * 37 + getElement(i);

        }
        return code;
    }

    abstract byte getElement(int i);

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return limit - start;
    }

    public int getRawLength() {
        return end - start + 1;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public void setOffset(int offset) {
        if (offset < start) {
            this.offset = start;
            return;
        }
        if (offset >= limit) {
            this.offset = limit - 1;
            return;
        }
        this.offset = offset;
    }

    public void recycle() {
        hasHashCode = false;
        offset = start;
    }

    @Override
    public AbstractChunk clone() throws CloneNotSupportedException {
        return (AbstractChunk) super.clone();
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public BuddyAllocator getAllocator() {
        return buddyAllocator;
    }

    public boolean isRecyled() {
        return recyled;
    }
}
