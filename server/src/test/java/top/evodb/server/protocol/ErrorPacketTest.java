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

package top.evodb.server.protocol;

import static org.junit.Assert.assertEquals;


import org.junit.Test;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.memory.heap.ByteChunkAllocator;
import top.evodb.core.memory.protocol.AdjustableProtocolBufferAllocator;
import top.evodb.core.memory.protocol.ProtocolBufferAllocator;
import top.evodb.core.protocol.MysqlPacket;
import top.evodb.server.ServerContext;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.mysql.CapabilityFlags;
import top.evodb.server.mysql.ErrorCode;

/**
 * @author evodb
 */
public class ErrorPacketTest {
    private static final int CHUNK_SIZE = 15;
    private ByteChunkAllocator byteChunkAllocator = ServerContext.getContext().getByteChunkAllocator();
    private ProtocolBufferAllocator allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE, byteChunkAllocator);
    private MysqlPacketFactory factory = new MysqlPacketFactory(allocator);

    @Test
    public void testWrite() throws MysqlPacketFactoryException {
        ByteChunk message = byteChunkAllocator.alloc(4);
        message.append("test");
        ErrorPacket errorPacket = factory.getMysqlPacket(MysqlPacket.ERR_PACKET);
        errorPacket.capabilities = CapabilityFlags.PROTOCOL_41;
        errorPacket.errorCode = ErrorCode.ER_ACCESS_DENIED_ERROR;
        errorPacket.message = message;
        errorPacket.write();
        message.recycle();
    }

    @Test
    public void testRead() throws MysqlPacketFactoryException {
        ByteChunk message = byteChunkAllocator.alloc(4);
        message.append("test");

        ErrorPacket errorPacket = factory.getMysqlPacket(MysqlPacket.ERR_PACKET);
        errorPacket.capabilities = CapabilityFlags.PROTOCOL_41;
        errorPacket.errorCode = ErrorCode.ER_ACCESS_DENIED_ERROR;
        errorPacket.message = message;
        errorPacket.write();
        errorPacket.read();
        assertEquals("test", errorPacket.message.toString());
        assertEquals(ErrorCode.ER_ACCESS_DENIED_ERROR, errorPacket.errorCode);
    }
}
