/*
 * Copyright 2018 The Evodb Project
 *
 * The Evodb Project licenses this file to you under the Apache License,
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

package top.evodb.mysql.protocol.packet;

import top.evodb.buffer.ProtocolBuffer;

/**
 * Data packet.
 *
 * @author ynfeng
 */
public interface Packet {
    int LARGE_PACKET_SIZE = (1 << 24) - 1;

    /**
     * Get the length of data packet length.
     *
     * @return the length of mysql protocol packet
     */
    int getPayloadLength();

    /**
     * Get sequence id
     *
     * @return sequence id
     */
    byte getSequenceId();

    /**
     * Get pay load.
     *
     * @return protocol buffer
     */
    ProtocolBuffer getPayload();
}
