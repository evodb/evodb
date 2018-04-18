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

package top.evodb.core.memory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author evodb
 */
public class BuddyAllocatorTest {

    @Test
    public void testAllocateWidth16Bytes() {
        BuddyAllocator buddyAllocator = new BuddyAllocator(16);
        assertEquals(16, buddyAllocator.alloc(16));
        assertEquals(0, buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocateWidth9Bytes() {
        BuddyAllocator buddyAllocator = new BuddyAllocator(16);
        assertEquals(16, buddyAllocator.alloc(9));
        assertEquals(0, buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocateWidth3Bytes() {
        BuddyAllocator buddyAllocator = new BuddyAllocator(16);
        assertEquals(4, buddyAllocator.alloc(3));
        assertEquals(1, buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocateWidth5Bytes() {
        BuddyAllocator buddyAllocator = new BuddyAllocator(16);
        assertEquals(8, buddyAllocator.alloc(5));
        assertEquals(1, buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocateWidth6Bytes() {
        BuddyAllocator buddyAllocator = new BuddyAllocator(16);
        assertEquals(8, buddyAllocator.alloc(6));
        assertEquals(1, buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocateWidth7Bytes() {
        BuddyAllocator buddyAllocator = new BuddyAllocator(16);
        assertEquals(8, buddyAllocator.alloc(7));
        assertEquals(1, buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocateWidth10Bytes() {
        BuddyAllocator buddyAllocator = new BuddyAllocator(16);
        assertEquals(16, buddyAllocator.alloc(10));
        assertEquals(0, buddyAllocator.alloc(1));
    }

    @Test
    public void testAllocateWidth8Bytes() {
        BuddyAllocator buddyAllocator = new BuddyAllocator(16);
        assertEquals(8, buddyAllocator.alloc(8));
        assertEquals(8, buddyAllocator.alloc(8));
        assertEquals(0, buddyAllocator.alloc(8));
    }

    @Test
    public void testAllocateWidth4Bytes() {
        BuddyAllocator buddyAllocator = new BuddyAllocator(16);
        assertEquals(4, buddyAllocator.alloc(4));
        assertEquals(4, buddyAllocator.alloc(4));
        assertEquals(4, buddyAllocator.alloc(4));
        assertEquals(4, buddyAllocator.alloc(4));
        assertEquals(0, buddyAllocator.alloc(4));
    }

    @Test
    public void testAllocateWidth1Byte() {
        BuddyAllocator buddyAllocator = new BuddyAllocator(16);
        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));

        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));

        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));

        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));
        assertEquals(1, buddyAllocator.alloc(1));

        assertEquals(0, buddyAllocator.alloc(1));
    }

}
