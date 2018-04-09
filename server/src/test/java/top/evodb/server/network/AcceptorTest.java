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

package top.evodb.server.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author ynfeng
 */
public class AcceptorTest {
    private static Reactor reactor;
    private static Acceptor acceptor;

    @BeforeClass
    public static void setUp() throws IOException {
        reactor = Reactor.getInstance();
        acceptor = Acceptor.getInstance("127.0.0.1", 6666, reactor);
        acceptor.start();
    }

    @AfterClass
    public static void shutdown() {
        acceptor.shutdown();
    }

    @Test(expected = SocketTimeoutException.class)
    public void connectTest() throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(1000);
        socket.connect(new InetSocketAddress("127.0.0.1", 6666));
        InputStream in = socket.getInputStream();
        in.read();
    }

}
