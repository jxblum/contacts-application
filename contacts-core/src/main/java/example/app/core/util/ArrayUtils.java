/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app.core.util;

import static example.app.core.util.CollectionUtils.toList;

import java.lang.reflect.Array;
import java.util.List;

/**
 * The {@link ArrayUtils} class is a abstract utility class for working with Java arrays.
 *
 * @author John Blum
 * @see java.lang.reflect.Array
 * @see java.util.Arrays
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class ArrayUtils {

  /* (non-Javadoc) */
  @SafeVarargs
  public static <T> T[] toArray(T... array) {
    return array;
  }

  /* (non-Javadoc) */
  @SuppressWarnings("unchecked")
  public static <T> T[] toArray(Iterable<T> iterable) {

    List<T> list = toList(iterable);

    Object[] array = (Object[]) Array.newInstance(Object.class, list.size());

    System.arraycopy(list.toArray(), 0, array, 0, array.length);

    return (T[]) array;
  }
}
