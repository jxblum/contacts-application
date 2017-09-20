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

import java.util.Map;

/**
 * Utility class for working with {@link Map Maps}.
 *
 * @author John Blum
 * @see java.util.Map
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class MapUtils {

  public static <K, V> Map.Entry newMapEntry(K key, V value) {

    return new Map.Entry<K, V>() {

      @Override public K getKey() {
        return key;
      }

      @Override public V getValue() {
        return value;
      }

      @Override public V setValue(V value) {
        throw new UnsupportedOperationException("Operation Not Supported");
      }

      @Override public String toString() {
        return String.format("%1$s = %2$s", getKey(), getValue());
      }
    };
  }
}
