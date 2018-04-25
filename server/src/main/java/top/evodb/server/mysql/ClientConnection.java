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

package top.evodb.server.mysql;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.core.memory.heap.ByteChunk;
import top.evodb.core.protocol.MysqlPacket;
import top.evodb.server.ServerContext;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.handler.client.ClientCloseHandler;
import top.evodb.server.handler.client.ClientConnectHandler;
import top.evodb.server.protocol.ErrorPacket;

/**
 * @author evodb
 */
public class ClientConnection extends AbstractMysqlConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConnection.class);

    public ClientConnection(String name, SocketChannel socketChannel) {
        super(name, socketChannel);
        offerHandler(ClientConnectHandler.INSTANCE);
    }

    @Override
    public void register(Selector selector) {
        try {
            selectionKey = socketChannel.register(selector, SelectionKey.OP_WRITE);
            selectionKey.attach(this);
        } catch (ClosedChannelException e) {
            LOGGER.warn(getName() + " register error.");
        }
    }

    @Override
    public void close(short errCode, String reason) {
        ByteChunk byteChunk = null;
        try {
            LOGGER.debug("Ready to close connection:" + getName() + " reason:" + reason);
            byteChunk = ServerContext.getContext().getByteChunkAllocator().alloc(reason.length());
            byteChunk.append(reason);

            ErrorPacket errorPacket = getMysqlPacketFactory().getMysqlPacket(MysqlPacket.ERR_PACKET);
            Byte lastPacketId = (Byte) getAttribute(AbstractMysqlConnection.ATTR_PRE_PACKET_ID);
            lastPacketId = lastPacketId == null ? 1 : lastPacketId;
            removeAttributes(AbstractMysqlConnection.ATTR_PRE_PACKET_ID);
            errorPacket.capabilities = getCapability();
            errorPacket.errorCode = errCode;
            errorPacket.message = byteChunk;
            errorPacket.setSequenceId(lastPacketId);
            protocolBuffer = errorPacket.write();
            asyncWrite(protocolBuffer);
            offerHandler(ClientCloseHandler.INSTANCE);
        } catch (MysqlPacketFactoryException | IOException e) {
            LOGGER.warn("close connection error.", e);
        } finally {
            if (byteChunk != null) {
                byteChunk.recycle();
            }
        }
    }

    @Override
    public String toString() {
        return "ClientConnection{" +
            "name=" + getName() +
            ", autoCommit=" + getAutoCommit() +
            ", isolation=" + getIsolation() +
            ", address=" + getRemoteAddress() + '\'' +
            '}';
    }

}
