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

import java.util.HashMap;
import java.util.Map;

/**
 * https://dev.mysql.com/doc/refman/5.7/en/error-messages-server.html
 *
 * @author evodb
 */
public class ErrorCode {
    private static final Map<Short, String> sqlStateMap = new HashMap<>();
    public static final short ER_ACCESS_DENIED_ERROR = 1045;
    public static final short ER_HANDSHAKE_ERROR = 1043;

    static {
        sqlStateMap.put(ER_ACCESS_DENIED_ERROR, "#28000");
        sqlStateMap.put(ER_HANDSHAKE_ERROR, "#08S01");
    }

    public static String getSqlState(short errCode) {
        return sqlStateMap.get(errCode);
    }
}
