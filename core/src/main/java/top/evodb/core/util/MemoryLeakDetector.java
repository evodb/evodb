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
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.evodb.core.memory.heap.AbstractChunk;

/**
 * @author evodb
 */
public class MemoryLeakDetector<T extends AbstractChunk> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryLeakDetector.class);
    private final HashMap<MemoryLeak, LeakEntry> leaks = new HashMap<>();
    private final ReferenceQueue<MemoryLeak> refQueue = new ReferenceQueue<>();
    private DetectLevel detectLevel = DetectLevel.DISABLE;
    private int detectRate = 100;
    private boolean memoryLeakOccurred;
    private boolean printLog;

    public MemoryLeak open(T t) {
        MemoryLeak memoryLeak = new MemoryLeak(t, refQueue);
        leaks.putIfAbsent(memoryLeak, LeakEntry.INSTANCE);
        reportLeaks();
        return memoryLeak;
    }

    private void reportLeaks() {
        for (MemoryLeak memoryLeak = (MemoryLeak) refQueue.poll(); memoryLeak != null; memoryLeak = (MemoryLeak) refQueue.poll()) {
            if (!leaks.remove(memoryLeak, LeakEntry.INSTANCE)) {
                continue;
            }
            leaks.clear();
            memoryLeakOccurred = true;
            if (printLog) {
                LOGGER.error("Memory leak detected.\n" + memoryLeak.getHint());
            }
        }
    }

    private static final class LeakEntry {
        static final LeakEntry INSTANCE = new LeakEntry();
        private static final int HASH = System.identityHashCode(INSTANCE);

        private LeakEntry() {
        }

        @Override
        public int hashCode() {
            return HASH;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
    }

    public DetectLevel getDetectLevel() {
        return detectLevel;
    }

    public void setDetectLevel(DetectLevel detectLevel) {
        this.detectLevel = detectLevel;
    }

    public int getDetectRate() {
        return detectRate;
    }

    public void setPrintLog(boolean printLog) {
        this.printLog = printLog;
    }

    public boolean isMemoryLeakOccurred() {
        return memoryLeakOccurred;
    }

    public void setDetectRate(int detectRate) {
        this.detectRate = detectRate;
    }

    public static enum DetectLevel {
        HIGH, MIDDLE, DISABLE
    }


}
