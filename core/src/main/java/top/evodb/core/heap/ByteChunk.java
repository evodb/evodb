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

package top.evodb.core.heap;

import java.util.Arrays;

/**
 * @author evodb
 */
public class ByteChunk extends AbstractChunk {
    private byte[] buf;

    public ByteChunk(int size) {
        allocate(size, -1);
    }

    public void allocate(int size, int limit) {
        if (buf == null) {
            buf = new byte[size];
        }
        setStart(0);
        setEnd(0);
        setOffset(0);
        setLimit(limit);
    }

    public void ensureSpace(int size) {
        int limit = getLimit0();
        int reqSize = getEnd() + size;
        if (reqSize >= limit) {
            reqSize = limit;
        }
        if (reqSize < getEnd()) {
            return;
        }
        int newSize = buf.length * 2;
        if (newSize > limit) {
            newSize = limit;
        }
        byte[] newBuf = new byte[newSize];
        System.arraycopy(buf, 0, newBuf, 0, buf.length);
        buf = newBuf;
        buf = null;
    }

    public void recyle() {
        setStart(0);
        setEnd(0);
        setOffset(0);
    }

    public void append(byte[] bytes, int offset, int size) {
        ensureSpace(size);
        System.arraycopy(bytes, offset, buf, getOffset(), size);
    }

    public void append(byte[] bytes) {
        append(bytes, 0, bytes.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ByteChunk)) {
            return false;
        }
        ByteChunk charChunk = (ByteChunk) o;
        return Arrays.equals(buf, charChunk.buf);
    }
}
