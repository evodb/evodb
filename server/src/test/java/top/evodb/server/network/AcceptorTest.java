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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import top.evodb.server.PortRandomUtil;

/**
 * @author evodb
 */
public class AcceptorTest {
    private Reactor reactor;
    private Acceptor acceptor;
    private int port;

    @Before
    public void setUp() throws IOException {
        port = PortRandomUtil.getPort();
        reactor = Reactor.newInstance();
        acceptor = Acceptor.newInstance("127.0.0.1", port, reactor);
        acceptor.start();
    }

    @After
    public void shutdown() {
        acceptor.shutdown();
    }

    @Test(expected = SocketTimeoutException.class)
    public void connectTest() throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(1000);
        socket.connect(new InetSocketAddress("127.0.0.1", port));
        InputStream in = socket.getInputStream();
        in.read();
    }

}
