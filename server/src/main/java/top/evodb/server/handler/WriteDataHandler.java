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

package top.evodb.server.handler;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.server.mysql.AbstractMysqlConnection;

/**
 * @author evodb
 */
public class WriteDataHandler implements Handler {
    public static final WriteDataHandler INSTANCE = new WriteDataHandler();
    private static final Logger LOGGER = LoggerFactory.getLogger(WriteDataHandler.class);

    private WriteDataHandler() {
    }

    @Override
    public boolean handle(AbstractMysqlConnection mysqlConnection) {
        try {
            mysqlConnection.write(mysqlConnection.getWriteOperation().getWriteBuffer());
            if (mysqlConnection.getWriteOperation().getWriteBuffer() == null) {
                return true;
            }
        } catch (IOException e) {
            LOGGER.warn(mysqlConnection.getName() + " write data error.", e);
        }
        return false;
    }
}
