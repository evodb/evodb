/*
 * Copyright 2017-2018 The Evodb Project
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
 * @author evodb
 */
public abstract class AbstractMysqlPacket implements MysqlPacket {
    protected ProtocolBuffer protocolBuffer;
    protected int startIndex;
    protected int endIndex;
    protected int payloadLength;
    protected byte sequenceId;
    protected byte cmd;

    public AbstractMysqlPacket(ProtocolBuffer protocolBuffer, Integer startIndex, Integer endIndex) {
        this.protocolBuffer = protocolBuffer;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public byte getCmd() {
        return cmd;
    }

    @Override
    public int getPayloadLength() {
        return payloadLength;
    }

    @Override
    public byte getSequenceId() {
        return sequenceId;
    }

    @Override
    public void setSequenceId(byte sequenceId) {
        this.sequenceId = sequenceId;
    }

    @Override
    public ProtocolBuffer getPayload() {
        return protocolBuffer;
    }
}
