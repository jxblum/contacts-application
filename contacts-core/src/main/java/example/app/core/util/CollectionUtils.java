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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * The {@link CollectionUtils} class is an abstract utility class for working with Java Collection Framework classes
 * in the {@link java.util} package.
 *
 * @author John Blum
 * @see java.lang.Iterable
 * @see java.util.Collection
 * @see java.util.Collections
 * @see java.util.Enumeration
 * @see java.util.Iterator
 * @see java.util.List
 * @see java.util.Map
 * @see java.util.Set
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class CollectionUtils {

  /* (non-Javadoc) */
  public static <T> Enumeration<T> nullSafeEnumeration(Enumeration<T> enumeration) {
    return (enumeration != null ? enumeration : Collections.emptyEnumeration());
  }

  /* (non-Javadoc) */
  public static <T> Iterable<T> nullSafeIterable(Iterable<T> iterable) {
    return (iterable != null ? iterable : Collections::emptyIterator);
  }

  /* (non-Javadoc) */
  public static <T> Iterator<T> nullSafeIterator(Iterator<T> iterator) {
    return (iterator != null ? iterator : Collections.emptyIterator());
  }

  /* (non-Javadoc) */
  public static <T> List<T> nullSafeList(List<T> list) {
    return (list != null ? list : Collections.emptyList());
  }

  /* (non-Javadoc) */
  public static <K, V> Map<K, V> nullSafeMap(Map<K, V> map) {
    return (map != null ? map : Collections.emptyMap());
  }

  /* (non-Javadoc) */
  public static <T> Set<T> nullSafeSet(Set<T> set) {
    return (set != null ? set : Collections.emptySet());
  }

  /* (non-Javadoc) */
  public static <T> List<T> toList(Iterable<T> iterable) {
    if (iterable instanceof Collection) {
      return new ArrayList<>((Collection<T>) iterable);
    }
    else {
      List<T> list = new ArrayList<>();

      for (T element : iterable) {
        list.add(element);
      }

      return list;
    }
  }

  /* (non-Javadoc) */
  public static <T> Set<T> toSet(Iterable<T> iterable) {
    if (iterable instanceof Collection) {
      return new HashSet<>((Collection<T>) iterable);
    }
    else {
      Set<T> set = new HashSet<>();

      for (T element : iterable) {
        set.add(element);
      }

      return set;
    }
  }

  /* (non-Javadoc) */
  public static String toString(Map<?, ?> map) {
    StringBuilder builder = new StringBuilder("{\n");

    int count = 0;

    for (Entry<?, ?> entry : new TreeMap<>(map).entrySet()) {
      builder.append(++count > 1 ? ",\n" : "");
      builder.append("\t");
      builder.append(entry.getKey());
      builder.append(" = ");
      builder.append(entry.getValue());
    }

    builder.append("\n}");

    return builder.toString();
  }
}
