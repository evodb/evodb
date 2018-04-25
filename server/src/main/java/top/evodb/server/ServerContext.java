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

package top.evodb.server;

import top.evodb.core.memory.heap.ByteChunkAllocator;
import top.evodb.core.util.MemoryLeakDetector;
import top.evodb.core.util.StringCache;
import top.evodb.server.mysql.Charset;
import top.evodb.server.util.IDGenerator;

/**
 * @author evodb
 */
public class ServerContext {
    private static volatile ServerContext instance;

    private final IDGenerator idGenerator;
    private final Version version;
    private Charset charset;
    private final StringCache stringCache;
    private final ByteChunkAllocator byteChunkAllocator;

    private ServerContext() {
        idGenerator = IDGenerator.newInstance();
        version = new Version();
        stringCache = StringCache.newInstance(20000);
        /* 10MB*/
        byteChunkAllocator = new ByteChunkAllocator((1 << 20) * 10);
        byteChunkAllocator.getMemoryLeakDetector().setDetectLevel(MemoryLeakDetector.DetectLevel.HIGH);
        byteChunkAllocator.getMemoryLeakDetector().setPrintLog(true);
    }

    public static ServerContext getContext() {
        if (instance == null) {
            synchronized (ServerContext.class) {
                if (instance == null) {
                    instance = new ServerContext();
                }
            }
        }
        return instance;
    }

    public int newConnectId() {
        return (int) idGenerator.getId();
    }

    public Version getVersion() {
        return version;
    }

    public Charset getCharset() {
        //TODO load charset
        charset = new Charset();
        charset.charsetIndex = 8;
        return charset;
    }

    public StringCache getStringCache() {
        return stringCache;
    }

    public ByteChunkAllocator getByteChunkAllocator() {
        return byteChunkAllocator;
    }
}
