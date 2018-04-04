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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.buffer.ProtocolBuffer;
import top.evodb.buffer.ProtocolBufferAllocator;
import top.evodb.mysql.handler.Handler;
import top.evodb.mysql.handler.HandlerQueue;
import top.evodb.mysql.handler.WriteDataHandler;
import top.evodb.mysql.protocol.packet.MysqlPacketFactory;

/**
 * @author evodb
 */
public abstract class AbstractMysqlConnection implements MysqlConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMysqlConnection.class);
    private AutoCommit autoCommit = AutoCommit.OFF;
    private Isolation isolation = Isolation.REPEATED_READ;
    protected SocketChannel socketChannel;
    protected SelectionKey selectionKey;
    private String name;
    protected ProtocolBufferAllocator<ProtocolBuffer> protocolBufferAllocator;
    protected ProtocolBuffer protocolBuffer;
    protected HandlerQueue handlerQueue;
    private WriteOperationContext writeOperationContext;
    private MysqlPacketFactory mysqlPacketFactory;

    public AbstractMysqlConnection(String name, SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.name = name;
        handlerQueue = HandlerQueue.newHandlerQueue();
        writeOperationContext = new WriteOperationContext();
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
    public int write(ProtocolBuffer protocolBuffer) throws IOException {
        return write(protocolBuffer, protocolBuffer.readableBytes());
    }

    @Override
    public int write(ProtocolBuffer protocolBuffer, int length) throws IOException {
        int writed = protocolBuffer.transferToChannel(socketChannel, length);
        writeOperationContext.remaining -= writed;
        if (writeOperationContext.remaining <= 0) {
            writeOperationContext.writeBuffer = null;
            disableWrite();
            enableRead();
        }
        return writed;
    }

    public void asyncWrite(ProtocolBuffer protocolBuffer) throws IOException {
        asyncWrite(protocolBuffer, protocolBuffer.readableBytes());
    }

    public void asyncWrite(ProtocolBuffer protocolBuffer, int length) throws IOException {
        if (writeOperationContext.writeBuffer != null) {
            throw new IOException("Can't write,there is write option was not completed.");
        }
        writeOperationContext.writeBuffer = protocolBuffer;
        writeOperationContext.remaining = length;
        offerHandler(WriteDataHandler.INSTANCE);
        enableWrite();
        disableRead();
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

    /**
     * Register to selector.
     *
     * @param selector selector
     */
    public abstract void register(Selector selector);

    public void setProtocolBufferAllocator(ProtocolBufferAllocator protocolBufferAllocator) {
        this.protocolBufferAllocator = protocolBufferAllocator;
    }

    public void fireIOEvent() {
        if (protocolBuffer == null) {
            protocolBuffer = protocolBufferAllocator.allocate();
        }
        Handler handler = handlerQueue.peekHandler();
        if (handler != null) {
            if (handler.handle(this)) {
                handlerQueue.pollHandler();
            }
        } else {
            throw new IllegalStateException(getName() + " have no handler,may be a bug.");
        }
    }

    public void offerHandler(Handler handler) {
        handlerQueue.offerHandler(handler);
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

    public class WriteOperationContext {
        private ProtocolBuffer writeBuffer;
        private int remaining;

        public ProtocolBuffer getWriteBuffer() {
            return writeBuffer;
        }

        public int getRemaining() {
            return remaining;
        }
    }

    public MysqlPacketFactory getMysqlPacketFactory() {
        return mysqlPacketFactory;
    }

    public void setMysqlPacketFactory(MysqlPacketFactory mysqlPacketFactory) {
        this.mysqlPacketFactory = mysqlPacketFactory;
    }

    public WriteOperationContext getWriteOperationContext() {
        return writeOperationContext;
    }

}
