/*
 * Copyright 2018 The Evolution Project
 *
 * The Evolution Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package io.evolution.buffer;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ynfeng
 */
public class AdjustableProtocolBufferAllocatorTests {
    private static final int CHUNK_SIZE = 15;
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);

    @Test
    public void testAllocate() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        Assert.assertNotNull(protocolBuffer);
    }

    @Test
    public void restRecyle() {
        ProtocolBuffer protocolBuffer = allocator.allocate();
        allocator.recyle(protocolBuffer);
    }


}
