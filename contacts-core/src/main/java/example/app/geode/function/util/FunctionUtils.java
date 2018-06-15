/*
 * Copyright 2018 the original author or authors.
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

package example.app.geode.function.util;

import java.util.Iterator;

/**
 * The FunctionUtils class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class FunctionUtils {

  @SuppressWarnings("unchecked")
  public static <T> T extractSingleFunctionResult(Object result) {

    if (result instanceof Iterable) {

      Iterable<T> results = (Iterable<T>) result;
      Iterator<T> resultsIterator = results.iterator();

      return resultsIterator.hasNext() ? resultsIterator.next() : null;
    }

    return (T) result;
  }
}
