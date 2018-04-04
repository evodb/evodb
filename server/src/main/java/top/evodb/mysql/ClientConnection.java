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

package top.evodb.mysql;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.mysql.handler.client.ClientConnectHandler;

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
    public void close() {
        disableAll();
        if (protocolBufferAllocator != null) {
            protocolBufferAllocator.recyle(protocolBuffer);
        }
        try {
            socketChannel.close();
        } catch (IOException e) {
            LOGGER.warn(getName() + " socket channel close error", e);
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
