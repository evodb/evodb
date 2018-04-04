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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.buffer.AdjustableProtocolBuffer;
import top.evodb.buffer.AdjustableProtocolBufferAllocator;
import top.evodb.buffer.ProtocolBufferAllocator;
import top.evodb.mysql.AbstractMysqlConnection;

/**
 * @author evodb
 */
public final class Reactor {

    private static final String REACTOR_THREAD_NAME_PREFIX = "r_thread";
    private static final int CHUNK_SIZE = 128;
    private final int numOfReactorThreads;
    private static final int SELECT_TIMEOUT = 500;
    private int currentReactorThread;
    private final ReactorThread[] reactorThreads;
    private static final Logger LOGGER = LoggerFactory.getLogger(Reactor.class);
    private static volatile Reactor instance;
    private final ProtocolBufferAllocator<AdjustableProtocolBuffer> allocator;

    public static Reactor getInstance() throws IOException {
        if (instance == null) {
            synchronized (Reactor.class) {
                if (instance == null) {
                    instance = new Reactor();
                }
            }
        }
        return instance;
    }

    private Reactor() throws IOException {
        currentReactorThread = 0;
        numOfReactorThreads = Runtime.getRuntime().availableProcessors();
        reactorThreads = new ReactorThread[numOfReactorThreads];
        allocator = new AdjustableProtocolBufferAllocator(CHUNK_SIZE);
        for (int i = 0; i < reactorThreads.length; i++) {
            reactorThreads[i] = new ReactorThread(REACTOR_THREAD_NAME_PREFIX + i);
        }
    }

    public void start() {
        for (ReactorThread reactorThread : reactorThreads) {
            reactorThread.start();
        }
        LOGGER.info("All reactor thread have stared.");
    }

    public void register(AbstractMysqlConnection mysqlConnection) {
        ReactorThread reactorThread = nextReactorThread();
        reactorThread.register(mysqlConnection);

    }

    private ReactorThread nextReactorThread() {
        int nextRectorThread = currentReactorThread % numOfReactorThreads;
        currentReactorThread++;
        return reactorThreads[nextRectorThread];
    }

    public void shutdown() {
        for (ReactorThread reactorThread : reactorThreads) {
            reactorThread.interrupt();
        }
    }


    private class ReactorThread extends Thread {

        private Selector selector;
        private LinkedList<AbstractMysqlConnection> registerQueue = new LinkedList<>();

        private ReactorThread(String name) throws IOException {
            selector = Selector.open();
            setName(name);
            LOGGER.debug("Create reactor thread[" + name + "]");
        }

        public void register(AbstractMysqlConnection mysqlConnection) {
            registerQueue.push(mysqlConnection);
            selector.wakeup();
        }

        private void doRegister() {
            for (; ; ) {
                AbstractMysqlConnection mysqlConnection = registerQueue.poll();
                mysqlConnection.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                mysqlConnection.setProtocolBufferAllocator(allocator);
                LOGGER.debug("Register connection[" + mysqlConnection.getName() + ']');
                mysqlConnection = registerQueue.poll();
                if (mysqlConnection == null) {
                    break;
                }
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int numOfReadKey = 0;
                    try {
                        numOfReadKey = selector.select(SELECT_TIMEOUT);
                    } catch (IOException e) {
                        LOGGER.warn("Select error.", e);
                    }
                    if (numOfReadKey > 0) {
                        Set<SelectionKey> selectedKeys = selector.selectedKeys();
                        Iterator<SelectionKey> it = selectedKeys.iterator();
                        while (it.hasNext()) {
                            SelectionKey selectionKey = it.next();
                            AbstractMysqlConnection mysqlConnection = (AbstractMysqlConnection) selectionKey.attachment();
                            mysqlConnection.fireIOEvent();
                            it.remove();
                        }
                    }
                    if (!registerQueue.isEmpty()) {
                        doRegister();
                    }
                } catch (Exception e) {
                    LOGGER.error("Reactor error:", e);
                }
            }
            LOGGER.info("Shoutdown.");
        }

    }

}
