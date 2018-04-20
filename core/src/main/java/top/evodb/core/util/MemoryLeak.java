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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import top.evodb.core.memory.heap.AbstractChunk;

/**
 * @author ynfeng
 */
public class MemoryLeak<T extends AbstractChunk> extends WeakReference<T> {
    private String hint;

    public MemoryLeak(T referent, ReferenceQueue q) {
        super(referent, q);
    }

    public void generateTraceInfo(int skipTrace) {
        StackTraceElement[] traceElements = new Throwable().getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (int i = skipTrace; i < traceElements.length; i++) {
            sb.append(traceElements[i]);
            sb.append('\n');
        }
        hint = sb.toString();
    }

    public String getHint() {
        return hint;
    }
}