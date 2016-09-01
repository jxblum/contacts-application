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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;

/**
 * The AbstractJsr107CachingProviderTests class is an abstract base class encapsulating tests to test
 * different caching providers for JSR-107 compliance using Spring Cache Abstraction
 *
 * @author John Blum
 * @see org.springframework.cache.Cache
 * @see org.springframework.cache.CacheManager`
 * @see example.app.caching.provider.support.Calculator
 * @since 1.0.0
 */
public abstract class AbstractJsr107CachingProviderTests {

  @Autowired(required = false)
  protected CacheManager cacheManager;

  @Autowired
  @SuppressWarnings("unused")
  private Calculator calculator;

  protected CacheManager getCacheManager() {
    Assert.state(cacheManager != null, "CacheManager was not properly configured and initialized");
    return this.cacheManager;
  }

  protected Cache getCache(String name) {
    return getCacheManager().getCache(name);
  }

  @SuppressWarnings("all")
  protected <T> T getNativeCache(String name) {
    Cache cache = getCache(name);
    Assert.state(cache != null, String.format("Cache [%s] not found", name));
    return (T) cache.getNativeCache();
  }

  protected void assertCacheIsEmpty() {
    assertCacheSize(0);
  }

  protected void assertCacheSize(int expectedSize) {
    assertThat(this.<Map>getNativeCache("Factorials").size()).isEqualTo(expectedSize);
  }

  @Before
  public void setup() {
    assertThat(calculator.isCacheMiss()).isFalse();
  }

  @Test
  public void cacheResultIsSuccessful() {
    assertThat(calculator.factorial(3L)).isEqualTo(6L);
    assertThat(calculator.isCacheMiss()).isTrue();
    assertThat(calculator.factorial(3L)).isEqualTo(6L);
    assertThat(calculator.isCacheMiss()).isFalse();
    assertThat(calculator.factorial(5L)).isEqualTo(120L);
    assertThat(calculator.isCacheMiss()).isTrue();
  }

  @Test
  public void cachePutIsSuccessful() {
    assertThat(calculator.store(4L, 24L)).isEqualTo(24L);
    assertThat(calculator.factorial(4L)).isEqualTo(24L);
    assertThat(calculator.isCacheMiss()).isFalse();
  }

  @Test
  public void cacheRemoveIsSuccessful() {
    assertThat(calculator.factorial(6L)).isEqualTo(720L);
    assertThat(calculator.isCacheMiss()).isTrue();
    assertThat(calculator.remove(6L)).isTrue();
    assertThat(calculator.factorial(6L)).isEqualTo(720L);
    assertThat(calculator.isCacheMiss()).isTrue();
  }

  @Test
  public void cacheRemoveAllIsSuccessful() {
    assertThat(calculator.clear()).isTrue();
    assertCacheIsEmpty();

    assertThat(calculator.factorial(0L)).isEqualTo(1L);
    assertThat(calculator.isCacheMiss()).isTrue();
    assertThat(calculator.factorial(1L)).isEqualTo(1L);
    assertThat(calculator.isCacheMiss()).isTrue();
    assertThat(calculator.factorial(2L)).isEqualTo(2L);
    assertThat(calculator.isCacheMiss()).isTrue();
    assertCacheSize(3);

    assertThat(calculator.clear()).isTrue();
    assertCacheIsEmpty();
  }
}
