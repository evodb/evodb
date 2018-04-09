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

package top.evodb.server.mysql.protocol.packet;

import top.evodb.server.buffer.ProtocolBuffer;
import top.evodb.server.buffer.ProtocolBufferAllocator;
import top.evodb.server.exception.MysqlPacketFactoryException;
import top.evodb.server.exception.ReflectionException;
import top.evodb.server.util.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Not thread safe.
 *
 * @author evodb
 */
public final class MysqlPacketFactory {

    private ProtocolBufferAllocator protocolBufferAllocator;
    private static final Map<Byte, Class> packetRegistryMap = new HashMap<>();

    static {
        packetRegistryMap.put(MysqlPacket.OK_PACKET, OKPacket.class);
        packetRegistryMap.put(MysqlPacket.ERR_PACKET, ErrorPacket.class);
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
            Object[] params = {protocolBufferAllocator.allocate(), Integer.valueOf(0),
                Integer.valueOf(0)};
            Class<?>[] paramTypes = {ProtocolBuffer.class, Integer.class, Integer.class};
            Constructor<?> constructor = ReflectionUtil.getConstructor(aClass, paramTypes);
            mysqlPacket = ReflectionUtil.newInstance(constructor, params);
        } catch (ReflectionException e) {
            throw new MysqlPacketFactoryException("Can't instantiate " + aClass, e);
        }
        return (T) mysqlPacket;
    }

    public <T extends MysqlPacket> T getMysqlPacket(Class clazz) throws MysqlPacketFactoryException {
        ProtocolBuffer protocolBuffer = protocolBufferAllocator.allocate();
        Class<?>[] paramTypes = {ProtocolBuffer.class, Integer.class, Integer.class};
        Object[] params = {protocolBuffer, Integer.valueOf(0), Integer.valueOf(0)};
        MysqlPacket mysqlPacket;
        try {
            Constructor<?> constructor = ReflectionUtil.getConstructor(clazz, paramTypes);
            mysqlPacket = ReflectionUtil.newInstance(constructor, params);
        } catch (ReflectionException e) {
            throw new MysqlPacketFactoryException("Can't instantiate " + clazz, e);
        }
        return (T) mysqlPacket;

    }

    public <T extends MysqlPacket> T getMysqlPacket(Class clazz, ProtocolBuffer protocolBuffer)
        throws MysqlPacketFactoryException {
        MysqlPacket mysqlPacket;
        try {
            Object[] params = {protocolBuffer, Integer.valueOf(0), Integer.valueOf(0)};
            Class<?>[] paramTypes = {ProtocolBuffer.class, Integer.class, Integer.class};
            Constructor<?> constructor = ReflectionUtil.getConstructor(clazz, paramTypes);
            mysqlPacket = ReflectionUtil.newInstance(constructor, params);
        } catch (ReflectionException e) {
            throw new MysqlPacketFactoryException("Can't instantiate " + clazz, e);
        }
        return (T) mysqlPacket;
    }

    public <T extends MysqlPacket> T getMysqlPacket(ProtocolBuffer protocolBuffer, int startIndex)
        throws MysqlPacketFactoryException {
        byte cmd = protocolBuffer.getByte(startIndex + MysqlPacket.PACKET_PAYLOAD_OFFSET);
        Class aClass = packetRegistryMap.get(cmd);
        if (aClass == null) {
            throw new MysqlPacketFactoryException("There is no such type '" + cmd + "' packet");
        }
        MysqlPacket mysqlPacket;
        try {
            Object[] params = {protocolBuffer, startIndex, Integer.valueOf(0)};
            Class<?>[] paramTypes = {ProtocolBuffer.class, Integer.class, Integer.class};
            Constructor<?> constructor = ReflectionUtil.getConstructor(aClass, paramTypes);
            mysqlPacket = ReflectionUtil.newInstance(constructor, params);
        } catch (ReflectionException e) {
            throw new MysqlPacketFactoryException("Can't instantiate " + aClass, e);
        }
        return (T) mysqlPacket;
    }

}
