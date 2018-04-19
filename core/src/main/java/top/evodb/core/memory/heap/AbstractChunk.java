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

package top.evodb.core.memory.heap;

/**
 * @author evodb
 */
public abstract class AbstractChunk {
    protected int start;
    protected int end;
    private int offset;

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return end - start + 1;
    }

    public void setOffset(int offset) {
        if (offset < start) {
            this.offset = start;
            return;
        }
        if (offset > end) {
            this.offset = end;
            return;
        }
        this.offset = offset;
    }

    public void recycle() {
        offset = 0;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
