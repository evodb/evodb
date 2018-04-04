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
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.buffer.ProtocolBuffer;
import top.evodb.buffer.ProtocolBufferAllocator;
import top.evodb.mysql.handler.Handler;
import top.evodb.mysql.handler.HandlerStack;

/**
 * @author evodb
 */
public abstract class AbstractMysqlConnection implements MysqlConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMysqlConnection.class);
    private AutoCommit autoCommit = AutoCommit.OFF;
    private Isolation isolation = Isolation.REPEATED_READ;
    protected SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private String name;
    protected ProtocolBufferAllocator<ProtocolBuffer> protocolBufferAllocator;
    protected ProtocolBuffer protocolBuffer;
    protected HandlerStack handlerStack;

    public AbstractMysqlConnection(String name, SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.name = name;
        handlerStack = HandlerStack.newHandlerStack();
    }

    @Override
    public AutoCommit getAutoCommit() {
        return autoCommit;
    }

    @Override
    public Isolation getIsolation() {
        return isolation;
    }

    @Override
    public void setAutoCommit(AutoCommit autoCommit) {
        this.autoCommit = autoCommit;
    }

    @Override
    public void setIsolation(Isolation isolation) {
        this.isolation = isolation;
    }

    @Override
    public ProtocolBuffer read() throws IOException {
        protocolBuffer.transferFromChannel(socketChannel);
        return protocolBuffer;
    }

    @Override
    public void write(ProtocolBuffer protocolBuffer) throws IOException {
        protocolBuffer.transferToChannel(socketChannel);
    }

    @Override
    public void write(ProtocolBuffer protocolBuffer, int length) throws IOException {
        protocolBuffer.transferToChannel(socketChannel, length);
    }

    @Override
    public String getName() {
        return name;
    }

    public SocketAddress getRemoteAddress() {
        try {
            return socketChannel.getRemoteAddress();
        } catch (IOException e) {
            LOGGER.warn(getName() + " get remote address error.");
        }
        return null;
    }

    public void register(Selector selector, int ops) {
        try {
            selectionKey = socketChannel.register(selector, ops);
            selectionKey.attach(this);
        } catch (ClosedChannelException e) {
            LOGGER.warn(getName() + " register error.");
        }
    }

    public void setProtocolBufferAllocator(ProtocolBufferAllocator protocolBufferAllocator) {
        this.protocolBufferAllocator = protocolBufferAllocator;
    }

    public void fireIOEvent() {
        if (protocolBuffer == null) {
            protocolBuffer = protocolBufferAllocator.allocate();
        }
        Handler handler = handlerStack.popHandler();
        if (handler != null) {
            handler.handle(this);
        } else {
            throw new IllegalStateException(getName() + " have no handler,may be a bug.");
        }
    }

    public void pushHandler(Handler handler) {
        handlerStack.pushHandler(handler);
    }

    public void enableWrite() {
        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
    }

    public void disableWrite() {
        selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_WRITE);
    }

    public void enableRead() {
        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_READ);
    }

    public void disableRead() {
        selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_READ);
    }

    public void disableAll() {
        disableWrite();
        disableRead();
    }
}
