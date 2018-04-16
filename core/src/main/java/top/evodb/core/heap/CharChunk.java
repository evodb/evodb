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
public class CharChunk extends AbstractChunk implements CharSequence {
    private char[] buf;

    public CharChunk(int size) {
        allocate(size, -1);
    }

    public void allocate(int size, int limit) {
        if (buf == null) {
            buf = new char[size];
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
        char[] newBuf = new char[newSize];
        System.arraycopy(buf, 0, newBuf, 0, buf.length);
        buf = newBuf;
        buf = null;
    }

    public void recyle() {
        setStart(0);
        setEnd(0);
        setOffset(0);
    }

    public void append(char[] chars, int offset, int size) {
        ensureSpace(size);
        System.arraycopy(chars, offset, buf, getOffset(), size);
    }

    public void append(char[] chars) {
        append(chars, 0, chars.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CharChunk)) {
            return false;
        }
        CharChunk charChunk = (CharChunk) o;
        return Arrays.equals(buf, charChunk.buf);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(buf);
    }

    @Override
    public int length() {
        return getOffset() - getStart();
    }

    @Override
    public char charAt(int index) {
        if (index > getEnd() || index < getStart()) {
            throw new IndexOutOfBoundsException();
        }
        return buf[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (start < 0 || end < 0) {
            throw new IndexOutOfBoundsException();
        }
        try {
            CharChunk newOne = (CharChunk) clone();
            newOne.setOffset(getStart() + start);
            newOne.setEnd(getStart() + end);
            return newOne;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
