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

import top.evodb.mysql.protocol.CapabilityFlags;

/**
 * @author evodb
 */
public class Constants {
    public static final String VERSION = "5.7.1-evodb";
    public static final int SERVER_CAPABILITY = CapabilityFlags.LONG_PASSWORD |
        CapabilityFlags.FOUND_ROWS |
        CapabilityFlags.LONG_FLAG |
        CapabilityFlags.CONNECT_WITH_DB |
        CapabilityFlags.NO_SCHEMA |
        CapabilityFlags.ODBC |
        CapabilityFlags.LOCAL_FILES |
        CapabilityFlags.IGNORE_SPACE |
        CapabilityFlags.PROTOCOL_41 |
        CapabilityFlags.INTERACTIVE |
        CapabilityFlags.IGNORE_SIGPIPE |
        CapabilityFlags.TRANSACTIONS |
        CapabilityFlags.RESERVED |
        CapabilityFlags.SECURE_CONNECTION |
        CapabilityFlags.MULTI_STATEMENTS |
        CapabilityFlags.MULTI_RESULTS |
        CapabilityFlags.PS_MULTI_RESULTS |
        CapabilityFlags.PLUGIN_AUTH |
        CapabilityFlags.PLUGIN_AUTH_LENENC_CLIENT_DATA |
        CapabilityFlags.CAN_HANDLE_EXPIRED_PASSWORDS |
        CapabilityFlags.SESSION_TRACK |
        CapabilityFlags.DEPRECATE_EOF;
}
