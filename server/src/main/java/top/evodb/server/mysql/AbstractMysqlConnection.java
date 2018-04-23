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
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.core.memory.protocol.ProtocolBuffer;
import top.evodb.core.memory.protocol.ProtocolBufferAllocator;
import top.evodb.server.handler.Handler;
import top.evodb.server.handler.HandlerQueue;
import top.evodb.server.handler.WriteDataHandler;
import top.evodb.server.protocol.MysqlPacketFactory;

/**
 * @author evodb
 */
public abstract class AbstractMysqlConnection implements MysqlConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMysqlConnection.class);
    public static final String ATTR_PRE_PACKET_ID = "PRE_PACKET_ID";
    public static final String ATTR_AUTH_PLUGIN_DATA = "AUTH_PLUGIN_DATA";

    private AutoCommit autoCommit = AutoCommit.OFF;
    private Isolation isolation = Isolation.REPEATED_READ;
    protected SocketChannel socketChannel;
    protected SelectionKey selectionKey;
    private String name;
    protected ProtocolBufferAllocator<ProtocolBuffer> protocolBufferAllocator;
    protected ProtocolBuffer protocolBuffer;
    protected HandlerQueue handlerQueue;
    private WriteOperation writeOperation;
    private MysqlPacketFactory mysqlPacketFactory;
    private int capability;
    private int maxPacketSize;
    private Charset charset;
    private final Map<String, Object> attributes;


    public AbstractMysqlConnection(String name, SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
        this.name = name;
        handlerQueue = HandlerQueue.newHandlerQueue();
        writeOperation = new WriteOperation();
        attributes = new HashMap<>();
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
    public int getCapability() {
        return capability;
    }

    @Override
    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }

    @Override
    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public void setCapability(int capability) {
        this.capability = capability;
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
        writeOperation.remaining -= writed;
        if (writeOperation.remaining <= 0) {
            writeOperation.writeBuffer = null;
            disableWrite();
            enableRead();
        }
        return writed;
    }

    public void asyncWrite(ProtocolBuffer protocolBuffer) throws IOException {
        asyncWrite(protocolBuffer, protocolBuffer.readableBytes());
    }

    public void asyncWrite(ProtocolBuffer protocolBuffer, int length) throws IOException {
        if (writeOperation.writeBuffer != null) {
            throw new IOException(getName() + " can't write,write option was not completed.");
        }
        writeOperation.writeBuffer = protocolBuffer;
        writeOperation.remaining = length;
        offerHandler(WriteDataHandler.INSTANCE);
        enableWrite();
        disableRead();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void removeAttributes(String key) {
        attributes.remove(key);
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
            LOGGER.debug("Call handler " + getName() + '[' + handler.getClass().getName() + ']');
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

    public void free() {
        if (protocolBuffer != null) {
            protocolBufferAllocator.recyle(protocolBuffer);
        }
        selectionKey.cancel();
    }

    public class WriteOperation {
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

    public WriteOperation getWriteOperation() {
        return writeOperation;
    }

}
