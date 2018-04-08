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

package top.evodb.server.util;

import top.evodb.server.exception.ReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author evodb
 */
public class ReflectionUtil {

    public static <T> T newInstance(Constructor<?> constructor, Object... args)
        throws ReflectionException {
        try {
            return (T) constructor.newInstance(args);
        } catch (IllegalAccessException
            | InstantiationException | InvocationTargetException e) {
            throw new ReflectionException(e);
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes)
        throws ReflectionException {
        try {
            return clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(e);
        }
    }
}
