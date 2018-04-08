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

package top.evodb.server;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.server.network.Acceptor;
import top.evodb.server.network.Reactor;


/**
 * @author evodb
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String args[]) {
        Reactor reactor;
        Acceptor acceptor;
        try {
            reactor = Reactor.getInstance();
            acceptor = Acceptor.getInstance("127.0.0.1", 3306, reactor);
            acceptor.start();
            reactor.start();
        } catch (IOException e) {
            LOGGER.error("Start server error.", e);
        }
    }
}
