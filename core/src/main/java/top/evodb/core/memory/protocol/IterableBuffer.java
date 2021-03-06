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

/**
 * The class that implements this interface can create multiple iterators, Each of which keeps its
 * internal state until the {@link PacketIterator#reset()} method calls. Note:For performance
 * reasons, try to create only one iterator.
 *
 * @author evodb
 */
@SuppressWarnings("unused")
public interface IterableBuffer {

    /**
     * Gets an iterator named 'default'.
     */
    PacketIterator packetIterator();

    /**
     * Gets an iterator named {@code name}.
     *
     * @param name The name of iterator
     */
    PacketIterator packetIterator(String name);
}
