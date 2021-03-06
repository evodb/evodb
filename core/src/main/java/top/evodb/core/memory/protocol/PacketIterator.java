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
 * @author evodb
 */
public interface PacketIterator {

    /**
     * Returns {@code true} if there is a packet or {@code false} there is no more packet.
     */
    boolean hasPacket();

    /**
     * Get next packet.
     * If {@link #hasPacket()} return {@code false} this method return 0,{@code true} return a packet descriptor.
     * Packet descriptor is a 64-bit integer,the structure is as follows:
     * <pre>
     * +-------------------------------------------+----------------------------+-----------------------------------+
     * |                30 bits                    |           24 bits          |   8 bits         |      2 bits    |
     * | The packet start position in buffer       | The length of packet       |  command type    |   packet type  |
     * +-------------------------------------------+----------------------------+-----------------------------------+
     * </pre>
     * A packet up to 16MB.
     * 2-bit packet type may be {@link PacketDescriptor.PacketType#HALF} or {@link PacketDescriptor.PacketType#FULL}
     */
    long nextPacket();

    /**
     * Reset the internal state.
     */
    void reset();
}
