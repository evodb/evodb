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

/**
 * Mysql Connection
 *
 * @author evodb
 */
public interface MysqlConnection extends Connection {
    /**
     * Get autocommit
     *
     * @return autocommit
     */
    AutoCommit getAutoCommit();

    /**
     * Get Isolation
     *
     * @return Isolation
     */
    Isolation getIsolation();

    /**
     * Set autocommit
     *
     * @param autoCommit autocommit
     */
    void setAutoCommit(AutoCommit autoCommit);

    /**
     * set Isolation
     *
     * @param isolation isolation
     */
    void setIsolation(Isolation isolation);

    /**
     * Get the capability of connection
     *
     * @return capability
     */
    int getCapability();

    /**
     * Get the max protocol packet size.
     *
     * @return max packet size
     */
    int getMaxPacketSize();

    /**
     * Get the charset.
     *
     * @return charset
     */
    Charset getCharset();
}
