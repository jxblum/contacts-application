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

package example.tests.spring.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

/**
 * Integration tests for Spring's Cache Abstraction using the {@link ConcurrentMapCacheManager} implementation
 * and caching provider safely with a non-existing {@link Cache} and asserting the behavior/effects.
 *
 * @author John Blum
 * @see org.springframework.cache.Cache
 * @see org.springframework.cache.CacheManager
 * @see org.springframework.cache.annotation.EnableCaching
 * @see org.springframework.cache.concurrent.ConcurrentMapCacheManager
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see <a href="https://stackoverflow.com/questions/52341228/spring-cache-to-disable-cache-by-cachename-configuration">Spring Cache to Disable Cache by cacheName configuration</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class NonExistingCacheSafeCacheManagerWrapperIntegrationTests {

  @Autowired
  private TestCacheableService cacheableService;

  @Test
  public void existingCacheWorks() {

    assertThat(this.cacheableService.wasCacheMiss()).isFalse();
    assertThat(this.cacheableService.serviceMethodOne()).isEqualTo("TEST");
    assertThat(this.cacheableService.wasCacheMiss()).isTrue();
    assertThat(this.cacheableService.serviceMethodOne()).isEqualTo("TEST");
    assertThat(this.cacheableService.wasCacheMiss()).isFalse();
  }

  @Test
  public void nonExistingCacheIsSafe() {

    assertThat(this.cacheableService.wasCacheMiss()).isFalse();
    assertThat(this.cacheableService.serviceMethodTwo()).isEqualTo("MOCK");
    assertThat(this.cacheableService.wasCacheMiss()).isTrue();
    assertThat(this.cacheableService.serviceMethodTwo()).isEqualTo("MOCK");
    assertThat(this.cacheableService.wasCacheMiss()).isTrue();
  }

  @Configuration
  @EnableCaching
  static class TestConfiguration {

    @Bean
    CacheManager cacheManager() {
      return new NonExistingCacheSafeCacheManagerWrapper(
        new ConcurrentMapCacheManager("ExistingCache"));
    }

    @Bean
    TestCacheableService cacheableService() {
      return new TestCacheableService();
    }
  }

  @Service
  static class TestCacheableService {

    private final AtomicBoolean cacheMiss = new AtomicBoolean(false);

    public boolean wasCacheMiss() {
      return this.cacheMiss.compareAndSet(true, false);
    }

    @Cacheable("ExistingCache")
    public Object serviceMethodOne() {
      this.cacheMiss.set(true);
      return "TEST";
    }

    @Cacheable("NonExistingCache")
    public Object serviceMethodTwo() {
      this.cacheMiss.set(true);
      return "MOCK";
    }
  }

  static class NonExistingCacheSafeCacheManagerWrapper implements CacheManager {

    private final CacheManager delegate;

    public NonExistingCacheSafeCacheManagerWrapper(CacheManager cacheManager) {

      Assert.notNull(cacheManager, "CacheManager is required");

      this.delegate = cacheManager;
    }

    protected CacheManager getDelegateCacheManager() {
      return this.delegate;
    }

    @Nullable @Override
    public Cache getCache(String name) {

      Cache cache = getDelegateCacheManager().getCache(name);

      return cache != null ? cache : new NoOpNamedCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
      return getDelegateCacheManager().getCacheNames();
    }
  }

  static class NoOpNamedCache implements Cache {

    private final String name;

    public NoOpNamedCache(String name) {

      Assert.hasText(name, "Name is required");

      this.name = name;
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public Object getNativeCache() {
      return null;
    }

    @Nullable @Override
    public ValueWrapper get(Object key) {
      return null;
    }

    @Nullable @Override
    public <T> T get(Object key, @Nullable Class<T> type) {
      return null;
    }

    @Nullable @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
      return null;
    }

    @Override
    public void put(Object key, @Nullable Object value) { }

    @Nullable @Override
    public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
      return null;
    }

    @Override
    public void evict(Object key) { }

    @Override
    public void clear() { }
  }
}
