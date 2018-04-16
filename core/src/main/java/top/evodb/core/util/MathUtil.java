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

package top.evodb.core.util;

/**
 * @author evodb
 */
public class MathUtil {

    public static int log2(int val) {
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(val);
    }

    public static int to2N(int val) {
        int i = MathUtil.log2(val);
        int mask = 1 << i;
        if ((val & ~mask) != 0) {
            return 1 << ++i;
        } else {
            return val;
        }
    }

}
