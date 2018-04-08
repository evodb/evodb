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
import top.evodb.server.buffer.ProtocolBuffer;

/**
 * @author evodb
 */
public interface Connection {

    /**
     * Close connection
     */
    void close();

    /**
     * read data.
     *
     * @throws IOException read error.
     */
    ProtocolBuffer read() throws IOException;

    /**
     * write data.
     *
     * @param protocolBuffer buffer
     * @return writed bytes.
     * @throws IOException write error.
     */
    int write(ProtocolBuffer protocolBuffer) throws IOException;

    /**
     * write data.
     *
     * @param protocolBuffer buffer
     * @param length         length of data
     * @return writed bytes.
     * @throws IOException write error.
     */
    int write(ProtocolBuffer protocolBuffer, int length) throws IOException;

    /**
     * Get the name of connection.
     *
     * @return name of connection
     */
    String getName();
}
