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

package top.evodb.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author evodb
 */
public class IDGenerator {

    private static final AtomicLong atomicLong = new AtomicLong();

    public long getId() {
        return atomicLong.incrementAndGet();
    }

    public static IDGenerator newInstance() {
        return new IDGenerator();
    }
}
