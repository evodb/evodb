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

import java.nio.channels.SocketChannel;
import top.evodb.util.IDGenerator;

/**
 * @author evodb
 */
public class ClientConnectionFactory {
    private static String CLIENT_NAME_PREFIX = "client-connection-";
    private static volatile ClientConnectionFactory instance;
    private final IDGenerator idGenerator;

    public static ClientConnectionFactory getInstance() {
        if (instance == null) {
            synchronized (ClientConnectionFactory.class) {
                if (instance == null) {
                    instance = new ClientConnectionFactory();
                }
            }
        }
        return instance;
    }

    private ClientConnectionFactory() {
        idGenerator = IDGenerator.newInstance();
    }

    public ClientConnection makeConnection(SocketChannel socketChannel) {
        return new ClientConnection(CLIENT_NAME_PREFIX + idGenerator.getId(), socketChannel);
    }
}
