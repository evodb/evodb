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

/**
 * @author evodb
 */
public abstract class AbstractChunk implements Cloneable {
    public static final int MAX_SIZE = Integer.MAX_VALUE - 8;
    private int limit = -1;
    private int start;
    private int end;
    private int offset;


    public int getLimit() {
        return limit;
    }

    public void recycle() {
        start = 0;
        end = 0;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        if (offset > end) {
            this.offset = end;
        }
        if (offset < start) {
            this.offset = start;
        }
        this.offset = offset;
    }

    /**
     * Maximum amount of data in this buffer. If -1 or not set, the buffer will
     * grow to {{@link #MAX_SIZE}.
     *
     * @param limit limit
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    protected int getLimit0() {
        if (limit == -1) {
            return MAX_SIZE;
        }
        return limit;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        if (start > end) {
            this.start = end;
        }
        if (start < 0) {
            this.start = 0;
        }
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        if (end < start) {
            this.end = start;
        }
        this.end = end;
    }
}
