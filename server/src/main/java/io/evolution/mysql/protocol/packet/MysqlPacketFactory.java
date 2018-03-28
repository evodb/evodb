/*
 * Copyright 2018 The Evolution Project
 *
 * The Evolution Project licenses this file to you under the Apache License,
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

package io.evolution.mysql.protocol.packet;

import io.evolution.buffer.ProtocolBuffer;
import io.evolution.buffer.ProtocolBufferAllocator;
import io.evolution.exception.MysqlPacketFactoryException;
import io.evolution.exception.ReflectionException;
import io.evolution.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Not thread safe.
 *
 * @author ynfeng
 */
public final class MysqlPacketFactory {
    private ProtocolBufferAllocator protocolBufferAllocator;
    private static final Map<Byte, Class> packetRegistryMap = new HashMap<>();

    static {
        packetRegistryMap.put(MysqlPacket.OK_PACKET, OKPacket.class);
    }

    public MysqlPacketFactory(ProtocolBufferAllocator protocolBufferAllocator) {
        this.protocolBufferAllocator = protocolBufferAllocator;
    }

    public <T extends MysqlPacket> T getMysqlPacket(byte cmd) throws MysqlPacketFactoryException {
        Class aClass = packetRegistryMap.get(cmd);
        if (aClass == null) {
            throw new MysqlPacketFactoryException("There is no such type '" + cmd + "' packet");
        }
        MysqlPacket mysqlPacket;
        try {
            Object[] params = { protocolBufferAllocator.allocate(), Integer.valueOf(0), Integer.valueOf(0) };
            Class<?>[] paramTypes = { ProtocolBuffer.class, Integer.class, Integer.class };
            Constructor<?> constructor = ReflectionUtil.getConstructor(aClass, paramTypes);
            mysqlPacket = ReflectionUtil.newInstance(constructor, params);
        } catch (ReflectionException e) {
            throw new MysqlPacketFactoryException("Can't instantiate " + aClass, e);
        }
        return (T) mysqlPacket;
    }

    public <T extends MysqlPacket> T getMysqlPacket(ProtocolBuffer protocolBuffer, int startIndex)
            throws MysqlPacketFactoryException {
        byte cmd = protocolBuffer.getByte(startIndex + MysqlPacket.PAYLOAD_OFFSET);
        Class aClass = packetRegistryMap.get(cmd);
        if (aClass == null) {
            throw new MysqlPacketFactoryException("There is no such type '" + cmd + "' packet");
        }
        MysqlPacket mysqlPacket;
        try {
            Object[] params = { protocolBuffer, startIndex, Integer.valueOf(0) };
            Class<?>[] paramTypes = { ProtocolBuffer.class, Integer.class, Integer.class };
            Constructor<?> constructor = ReflectionUtil.getConstructor(aClass, paramTypes);
            mysqlPacket = ReflectionUtil.newInstance(constructor, params);
        } catch (ReflectionException e) {
            throw new MysqlPacketFactoryException("Can't instantiate " + aClass, e);
        }
        return (T) mysqlPacket;
    }

}
