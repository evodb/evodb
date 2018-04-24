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

package top.evodb.core.memory.protocol;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import top.evodb.core.memory.heap.ByteChunk;

/**
 * Network communications dedicated buffer, encapsulated the data type of mysql protocol for reading
 * and writing. </br> the structure is as follows:
 * <pre>
 *      +-------------------+------------------+------------------+
 *      | discardable bytes |  readable bytes  |  writable bytes  |
 *      +-------------------+------------------+------------------+
 *      |                   |                  |                  |
 *      0      <=        readIndex   <=    writeIndex    <=    capacity
 *
 * BEFORE clear()
 *
 *      +-------------------+------------------+------------------+
 *      | discardable bytes |  readable bytes  |  writable bytes  |
 *      +-------------------+------------------+------------------+
 *      |                   |                  |                  |
 *      0      <=         redeIndex   <=   writIndex    <=   capacity
 *
 *
 * AFTER clear()
 *
 *      +---------------------------------------------------------+
 *      |             writable bytes (got more space)             |
 *      +---------------------------------------------------------+
 *      |                                                         |
 *      0 = readIndex = writeIndex             <=            capacity
 * BEFORE compact()
 *
 *      +-------------------+------------------+------------------+
 *      | discardable bytes |  readable bytes  |  writable bytes  |
 *      +-------------------+------------------+------------------+
 *      |                   |                  |                  |
 *      0      <=        readIndex   <=   writIndex    <=    capacity
 *
 *
 * AFTER compact()
 *
 *      +------------------+--------------------------------------+
 *      |  readable bytes  |    writable bytes (got more space)   |
 *      +------------------+--------------------------------------+
 *      |                  |                                      |
 * readIndex (0) <= writeIndex (compacted)        <=         capacity
 * </pre>
 * Read-prefix methods will cause {@code readIndex} increase. Write-prefix methods will cause {@code
 * writeIndex} increase.
 *
 * @author evodb
 */
@SuppressWarnings("unused")
public interface ProtocolBuffer extends IterableBuffer {

    /**
     * Write the data between {@code readIndex} and {@code writeIndex} to the {@code socketChannel}.
     * </br> After operation {@code readIndex} will increase the number of bytes written.
     *
     * @param socketChannel Target channel
     * @return The number of bytes that have been written
     * @throws IOException May cause IOException
     */
    int transferToChannel(SocketChannel socketChannel) throws IOException;

    /**
     * Write {@code length} bytes to {@code socketChannel}. </br> After operation {@code readIndex}
     * will increase the number of bytes written.
     *
     * @param socketChannel Target channel
     * @param length        Num of bytes
     * @return The number of bytes that have been written
     * @throws IOException May cause IOException
     */
    int transferToChannel(SocketChannel socketChannel, int length) throws IOException;

    /**
     * Read data from the {@code socketChannel}. </br> After operation {@code writeIndex} will
     * increase the number of bytes readed.
     *
     * @param socketChannel Source channel
     * @return The number of bytes that have been readed
     * @throws IOException May cause IOException
     */
    int transferFromChannel(SocketChannel socketChannel) throws IOException;

    /**
     * compact
     *
     * @return The instance of self
     */
    ProtocolBuffer compact();

    /**
     * clear
     */
    void clear();

    /**
     * Whether there is unread data.
     *
     * @return {@code true} yes，{@code false} no.
     */
    boolean hasReadableBytes();

    /**
     * Get readable data length.
     *
     * @return The Readable length
     */
    int readableBytes();

    /**
     * Get writable data length.
     *
     * @return The Writeable length
     */
    int writableBytes();

    /**
     * Get total capacity.
     *
     * @return The total capacity
     */
    int capacity();

    /**
     * Get {@code writeIndex}.
     *
     * @return writeIndex
     */
    int writeIndex();

    /**
     * Get {@code readIndex}.
     *
     * @return readIndex
     */
    int readIndex();

    /**
     * Set {@code writeIndex}.
     *
     * @param writeIndex The new value of WriteIndex
     */
    void writeIndex(int writeIndex);

    /**
     * Set {@code readIndex}.
     *
     * @param readIndex The new value of readIndex
     */
    void readIndex(int readIndex);

    /**
     * Get Protocol::FixedLengthInteger from {@code index},see mysql protocol for details.
     *
     * @param index  read position
     * @param length length
     * @return value
     */
    long getFixInt(int index, int length);

    /**
     * Read Protocol::FixedLengthInteger from {@code readIndex},see mysql protocol for details.
     * </br> After operation {@code readIndex} will increase the number of {@code length}.
     *
     * @param length lenght of integer
     * @return value
     */
    long readFixInt(int length);

    /**
     * Get Protocol::LengthEncodedInteger from {@code readIndex},see mysql protocol for details.
     *
     * @param index read position
     * @return value
     */
    long getLenencInt(int index);

    /**
     * Read Protocol::LengthEncodedInteger from {@code readIndex},see mysql protocol for details.
     * </br> After operation {@code readIndex} will increase the number of EncodedInteger length.
     *
     * @return value
     */
    long readLenencInt();

    /**
     * Get Protocol::FixedLengthString from {@code index},see mysql protocol for details.
     *
     * @param index  read position
     * @param length string length
     * @return ByteChunk
     */
    ByteChunk getFixString(int index, int length);

    /**
     * Read Protocol::LengthEncodedInteger form {@code readIndex},see mysql protocol for details.
     * </br> After operation {@code readIndex} will increase the number of {@code length}.
     *
     * @param length string length
     * @return ByteChunk
     */
    ByteChunk readFixString(int length);

    /**
     * Get Protocol::LengthEncodedString from {@code index}, see mysql protocol for details.
     *
     * @param index read position
     * @return ByteChunk
     */
    ByteChunk getLenencString(int index);

    /**
     * Read Protocol::LengthEncodedString from {@code readIndex}, see mysql protocol for details.
     * </br> After operation {@code readIndex} will increase the number of LengthEncodeString
     * length.
     *
     * @return ByteChunk
     */
    ByteChunk readLenencString();

    /**
     * Get Protocol::NulTerminatedString from {@code index}, see mysql protocol for details.
     *
     * @param index read  position
     * @return ByteChunk
     */
    ByteChunk getNULString(int index);

    /**
     * Read Protocol::NulTerminatedString from {@code readIndex}, see mysql protocol for details.
     * </br> After operation {@code readIndex} will increase the number of NulTerminatedString
     * length.
     *
     * @return ByteChunk
     */
    ByteChunk readNULString();

    /**
     * Put Protocol::FixedLengthInteger to {@code index}, see mysql protocol for details.
     *
     * @param index  write position
     * @param length fixInt length
     * @param val    value
     * @return self instance
     */
    ProtocolBuffer putFixInt(int index, int length, long val);

    /**
     * Write Protocol::FixedLengthInteger to {@code readIndex}, see mysql protocol for details.
     * </br> After operation {@code writeIndex} will increase the number of FixedLengthInteger
     * length.
     *
     * @param length finxInt length
     * @param val    values
     * @return self instance
     */
    ProtocolBuffer writeFixInt(int length, long val);

    /**
     * Put Protocol::LengthEncodedInteger to {@code index}, see mysql protocol for details.
     *
     * @param index write position
     * @param val   value
     * @return self instance
     */
    ProtocolBuffer putLenencInt(int index, long val);

    /**
     * Write Protocol::LengthEncodedInteger to {@code index}, see mysql protocol for details. </br>
     * After operation {@code writeIndex} will increase the number of LengthEncodeInteger length.
     *
     * @param val value
     * @return self instance
     */
    ProtocolBuffer writeLenencInt(long val);

    /**
     * Put Protocol::FixedLengthString to {@code index},see mysql protocol for details.
     *
     * @param index write position
     * @param val   ByteChunk
     * @return self instance
     */
    ProtocolBuffer putFixString(int index, ByteChunk val);

    /**
     * Write Protocol::FixedLengthString to {@code readIndex},see mysql protocol for details. </br>
     * After operation {@code writeIndex} will increase the number of FixedLengthString length.
     *
     * @param val ByteChunk
     * @return self instance
     */
    ProtocolBuffer writeFixString(ByteChunk val);

    /**
     * Put Protocol::LengthEncodedString to {@code index},see mysql protocol for details.
     *
     * @param index write position
     * @param val   ByteChunk
     * @return self instance
     */
    ProtocolBuffer putLenencString(int index, ByteChunk val);

    /**
     * Write Protocol::LengthEncodedString to {@code readIndex},see mysql protocol for details.
     * </br> After operation {@code writeIndex} will increase the number of LengthENcodeString
     * length.
     *
     * @param val ByteChunk
     * @return self instance
     */
    ProtocolBuffer writeLenencString(ByteChunk val);

    /**
     * Put Protocol::NulTerminatedString to {@code index},see mysql protocol for details.
     *
     * @param index write position
     * @param val   ByteChunk
     * @return self instance
     */
    ProtocolBuffer putNULString(int index, ByteChunk val);

    /**
     * Write Protocol::NulTerminatedString to {@code readIndex},see mysql protocol for details.
     * </br> After operation {@code writeIndex} will increase the number of NulTerminatedString
     * length.
     *
     * @param val ByteChunk
     * @return self instance
     */
    ProtocolBuffer writeNULString(ByteChunk val);

    /**
     * Get bytes from {@code index}.
     *
     * @param dest  ByteChunk
     * @param index read position
     * @return readed bytes
     */
    int getBytes(ByteChunk dest, int index);

    /**
     * Read bytes from {@code readIndex}. </br> After operation {@code readIndex} will increase the
     * number of bytes length.
     *
     * @param dest ByteChunk
     * @return readed bytes
     */
    int readBytes(ByteChunk dest);

    /**
     * Get byte from {@code index}.
     *
     * @param index read position
     * @return value
     */
    byte getByte(int index);

    /**
     * Read byte from {@code readIndex}. </br> After operation {@code readIndex} will increase 1.
     *
     * @return value
     */
    byte readByte();

    /**
     * Get LengencBytes from {@code index}, see msyql protocol for details.
     *
     * @param index read position
     * @return ByteChunk
     */
    ByteChunk getLenencBytes(int index);

    /**
     * Read LenencBytes from {@code readIndex}, see mysql protocol for details. </br> After
     * operation {@code readIndex} will increase the number of LenencBytes length.
     *
     * @return ByteChunk
     */
    ByteChunk readLenencBytes();

    /**
     * Put bytes to {@code index},see msyql protocol for details.
     *
     * @param index write position
     * @param bytes bytes chunk
     * @return self instance
     */
    ProtocolBuffer putBytes(int index, ByteChunk bytes);

    /**
     * Put bytes to {@code index},see mysql protocol for details.
     *
     * @param index  write position
     * @param length write length
     * @param bytes  bytes chunk
     * @return self instance
     */
    ProtocolBuffer putBytes(int index, int length, ByteChunk bytes);

    /**
     * Put LenencBytes to {@code index},see mysql protocol for details.
     *
     * @param index write position
     * @param bytes bytes chunk
     * @return self instance
     */
    ProtocolBuffer putLenencBytes(int index, ByteChunk bytes);

    /**
     * Put byte to {@code index}.
     *
     * @param index write position
     * @param val   value
     * @return self instance
     */
    ProtocolBuffer putByte(int index, byte val);

    /**
     * Write LenencBytes to {@code writeIndex},see msyql protocol for details. </br> After operation
     * {@code writeIndex} will increase the number of LenencBytes length.
     *
     * @param bytes bytes chunk
     * @return self instance
     */
    ProtocolBuffer writeLenencBytes(ByteChunk bytes);

    /**
     * Write bytes to  {@code writeIndex}. </br> After operation {@code writeIndex} will increase
     * 1.
     *
     * @param val byte
     * @return self instance
     */
    ProtocolBuffer writeByte(byte val);

    /**
     * Write bytes to {@code writeIndex}. </br> After operation {@code writeIndex} will increase the
     * number of bytes length.
     *
     * @param bytes ByteChunk
     * @return self instance
     */
    ProtocolBuffer writeBytes(ByteChunk bytes);

    /**
     * Write bytes to {@code writeIndex}. </br> After operation {@code writeIndex} will increase the
     * number of bytes length.
     *
     * @param length bytes array length
     * @param bytes  ByteChunk
     * @return self instance
     */
    ProtocolBuffer writeBytes(int length, ByteChunk bytes);

    /**
     * Get "Length Encode Data Type" length,see mysql protocol for details.
     *
     * @param lenenc Length Encode
     * @return The Length of Length Encode
     */
    default int getLenencLength(long lenenc) {
        if (lenenc < 251) {
            return 1;
        } else if (lenenc >= 251 && lenenc < 1 << 16) {
            return 3;
        } else if (lenenc >= 1 << 16 && lenenc < 1 << 24) {
            return 4;
        } else {
            return 9;
        }
    }
}
