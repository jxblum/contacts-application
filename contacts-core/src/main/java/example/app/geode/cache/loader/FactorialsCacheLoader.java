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

package example.app.geode.cache.loader;

import org.apache.geode.cache.CacheLoaderException;
import org.apache.geode.cache.LoaderHelper;
import org.springframework.util.Assert;

import example.app.geode.cache.loader.support.CacheLoaderSupport;

/**
 * The FactorialsCacheLoader class is a GemFire {@link com.gemstone.gemfire.cache.CacheLoader} that computes
 * the factorial of a key as it's value. I.e. f(n) = n! = n * (n - 1) * (n - 2) .. * 1.
 *
 * For example:
 *
 * f(5) = 5! = 5 * 4 * 3 * 2 * 1 = 120
 *
 * @author John Blum
 * @see example.app.geode.cache.loader.support.CacheLoaderSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class FactorialsCacheLoader extends CacheLoaderSupport<Long, Long> {

  private static final FactorialsCacheLoader INSTANCE = new FactorialsCacheLoader();

  public static FactorialsCacheLoader getInstance() {
    return INSTANCE;
  }

  @Override
  public Long load(LoaderHelper<Long, Long> helper) throws CacheLoaderException {
    Long key = helper.getKey();

    Assert.isTrue(key >= 0L, String.format("Number [%d] must be greater than equal to 0", key));

    if (key <= 2L) {
      return (key < 2L ? 1L : 2L);
    }

    long result = key;

    while (--key > 1L) {
      result *= key;
    }

    return result;
  }
}
