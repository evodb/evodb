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
 *
 */

package top.evodb.mysql.protocol;

/**
 * @author evodb
 */
public class CapabilityFlags {

    public static final int LONG_PASSWORD = 0x00000001;
    public static final int FOUND_ROWS = 0x00000002;
    public static final int LONG_FLAG = 0x00000004;
    public static final int CONNECT_WITH_DB = 0x00000008;
    public static final int NO_SCHEMA = 0x00000010;
    public static final int COMPRESS = 0x00000020;
    public static final int ODBC = 0x00000040;
    public static final int LOCAL_FILES = 0x00000080;
    public static final int IGNORE_SPACE = 0x00000100;
    public static final int PROTOCOL_41 = 0x00000200;
    public static final int INTERACTIVE = 0x00000400;
    public static final int SSL = 0x00000800;
    public static final int IGNORE_SIGPIPE = 0x00001000;
    public static final int TRANSACTIONS = 0x00002000;
    public static final int RESERVED = 0x00004000;
    public static final int SECURE_CONNECTION = 0x00008000;
    public static final int MULTI_STATEMENTS = 0x00010000;
    public static final int MULTI_RESULTS = 0x00020000;
    public static final int PS_MULTI_RESULTS = 0x00040000;
    public static final int PLUGIN_AUTH = 0x00080000;
    public static final int CONNECT_ATTRS = 0x00100000;
    public static final int PLUGIN_AUTH_LENENC_CLIENT_DATA = 0x00200000;
    public static final int CAN_HANDLE_EXPIRED_PASSWORDS = 0x00400000;
    public static final int SESSION_TRACK = 0x00800000;
    public static final int DEPRECATE_EOF = 0x01000000;


}
