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
 */

package top.evodb.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.mysql.ClientConnection;
import top.evodb.mysql.ClientConnectionFactory;

/**
 * @author evodb
 */
public final class Acceptor extends Thread {
    private Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private static volatile Acceptor instance;
    private static final int SELECT_TIMEOUT = 1000;
    private final Reactor reactor;
    private static final Logger LOGGER = LoggerFactory.getLogger(Acceptor.class);
    private ClientConnectionFactory clientConnectionFactory = ClientConnectionFactory.getInstance();

    public static Acceptor getInstance(String bindIp, int bindPort, Reactor reactor) throws IOException {
        if (instance == null) {
            synchronized (Acceptor.class) {
                if (instance == null) {
                    instance = new Acceptor(bindIp, bindPort, reactor);
                }
            }
        }
        return instance;
    }

    @Override
    public synchronized void start() {
        super.start();
        LOGGER.info(getName() + " start.");
    }

    private Acceptor(String bindIp, int bindPort, Reactor reactor) throws IOException {
        selector = Selector.open();
        this.reactor = reactor;
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(bindIp, bindPort));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        setName("acceptor-thread");
    }

    private void shutdown() {
        interrupt();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                int numOfReadyKey = 0;
                try {
                    numOfReadyKey = selector.select(SELECT_TIMEOUT);
                } catch (IOException e) {
                    LOGGER.warn(getName() + " select error.", e);
                }
                if (numOfReadyKey > 0) {
                    Set<SelectionKey> selectKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectKeys.iterator();
                    while (it.hasNext()) {
                        SelectionKey selectionKey = it.next();
                        if (selectionKey.isAcceptable()) {
                            try {
                                SocketChannel socketChannel = serverSocketChannel.accept();
                                socketChannel.configureBlocking(false);
                                ClientConnection clientConnection = clientConnectionFactory.makeConnection(socketChannel);
                                LOGGER.debug(clientConnection + " accepted.");
                                reactor.register(clientConnection);
                            } catch (IOException e) {
                                LOGGER.warn(getName() + " accpet error.", e);
                            }
                        }
                        it.remove();
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Acceptor error.", e);
            }
        }
        LOGGER.info(getName() + " stop.");
    }
}
