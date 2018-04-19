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
public class ByteChunkAllocator extends BuddyAllocator<ByteChunk> {
    private final byte[] buf;
    private int offset;

    public ByteChunkAllocator(int size) {
        super(size);
        buf = new byte[size];
    }

    @Override
    protected void doFree(ByteChunk byteChunk) {

    }

    @Override
    protected ByteChunk doAlloc(int size) {
        if (size == 0) {
            return null;
        }
        ByteChunk byteChunk = new ByteChunk(buf, offset, offset + size - 1);
        offset += size;
        return byteChunk;
    }
}
