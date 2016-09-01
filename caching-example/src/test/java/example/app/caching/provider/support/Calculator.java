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

package example.app.caching.provider.support;

import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import example.app.service.support.AbstractCacheableService;

/**
 * The Calculator class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Service
@SuppressWarnings("unused")
public class Calculator extends AbstractCacheableService {

  @CacheResult(cacheName = "Factorials")
  public long factorial(long number) {
    setCacheMiss();

    Assert.isTrue(number >= 0L, String.format("Number [%d] must be greater than equal to 0", number));

    if (number <= 2L) {
      return (number < 2L ? 1L : 2L);
    }

    long result = number;

    while (--number > 1L) {
      result *= number;
    }

    return result;
  }

  @CachePut(cacheName = "Factorials")
  public long store(@CacheKey long number, @CacheValue long result) {
    return result;
  }

  @CacheRemove(cacheName = "Factorials")
  public boolean remove(long number) {
    return true;
  }

  @CacheRemoveAll(cacheName = "Factorials")
  public boolean clear() {
    return true;
  }
}
