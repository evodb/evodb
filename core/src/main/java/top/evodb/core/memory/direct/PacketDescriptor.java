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

package top.evodb.core.memory.direct;

/**
 * The tool for manipulating the descriptor.
 *
 * @author evodb
 */
public final class PacketDescriptor {

    public static final int NONE = 0;

    private PacketDescriptor() {
    }

    public static long setPacketType(long packetDescriptor, PacketType packetType) {
        return packetDescriptor & ~0x03L | packetType.getValue() & 0x03;
    }

    public static long setCommandType(long packetDescriptor, byte commandType) {
        return packetDescriptor & ~(0xFFL << 2) | (commandType & 0xFF) << 2;
    }

    public static long setPacketLen(long packetDescriptor, long packetLen) {
        return packetDescriptor & ~(0xFFFFFFL << 10) | (packetLen & 0xFFFFFF) << 10;
    }

    public static long setPacketStartPos(long packetDescriptor, long packetStartPos) {
        //noinspection NumericOverflow
        return packetDescriptor & ~(0x2FFFFFFFL << 34) | (packetStartPos & 0x2FFFFFFF) << 34;
    }

    public static PacketType getPacketType(long packetDescriptor) {
        int type = (int) (packetDescriptor & 0x03);
        PacketType packetType;
        switch (type) {
            case 0:
                packetType = PacketType.HALF;
                break;
            case 1:
                packetType = PacketType.FULL;
                break;
            default:
                throw new IllegalArgumentException("Wrong packet descriptor " + packetDescriptor);
        }
        return packetType;
    }

    public static byte getCommandType(long packetDescriptor) {
        return (byte) (packetDescriptor >>> 2 & 0xFF);
    }

    public static int getPacketStartPos(long packetDescriptor) {
        return (int) (packetDescriptor >>> 34 & 0x2FFFFFFF);
    }

    public static int getPacketLen(long packetDescriptor) {
        return (int) (packetDescriptor >>> 10 & 0xFFFFFF);
    }

    public enum PacketType {
        HALF(0), FULL(1);

        PacketType(int value) {
            this.value = value;
        }

        private final int value;

        public int getValue() {
            return value;
        }
    }
}
