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

package top.evodb.core.memory.direct;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


import java.nio.ByteBuffer;
import org.junit.Test;

/**
 * @author evodb
 */
public class AdjustableProtocolBufferAllocatorTest {

    private static final int CHUNK_SIZE = 15;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);

    @Test
    public void testAllocate() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        assertNotNull(protocolBuffer);
    }

    @Test
    public void testRecyle() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        allocator.recyle(protocolBuffer);
    }

    @Test
    public void testRecyleWithFalse() {
        ProtocolBuffer protocolBuffer = new AdjustableProtocolBuffer(
            new AdjustableProtocolBufferAllocator(CHUNK_SIZE));
        boolean rv = allocator.recyle(protocolBuffer);
        assertFalse(rv);
    }

    @Test
    public void testRecyleByteBufferWith16Bytes() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(16);
        boolean rv = ((AdjustableProtocolBufferAllocator) allocator)
            .recyleAllocateByteBuffer(byteBuffer);
        assertFalse(rv);
    }
}
