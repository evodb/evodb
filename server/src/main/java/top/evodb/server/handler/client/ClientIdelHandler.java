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

package top.evodb.server.handler.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.server.handler.Handler;
import top.evodb.server.mysql.AbstractMysqlConnection;

/**
 * @author evodb
 */
public class ClientIdelHandler implements Handler {
    public static final ClientIdelHandler INSTANCE = new ClientIdelHandler();
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientIdelHandler.class);

    private ClientIdelHandler() {
    }

    @Override
    public boolean handle(AbstractMysqlConnection mysqlConnection) {
        LOGGER.debug("idle");
        mysqlConnection.disableAll();
        return false;
    }
}
