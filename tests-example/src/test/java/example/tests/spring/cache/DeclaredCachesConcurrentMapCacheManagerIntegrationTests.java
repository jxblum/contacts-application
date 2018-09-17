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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for Spring's Cache Abstraction using the {@link ConcurrentMapCacheManager} implementation
 * and caching provider with a non-existing {@link Cache} and asserting the behavior/effects.
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
public class DeclaredCachesConcurrentMapCacheManagerIntegrationTests {

  @Autowired
  private TestCacheableService cacheableService;

  private void invokeAndAssertCacheOperation(Supplier<Object> supplier, Object expectedValue) {

    assertThat(this.cacheableService.wasCacheMiss()).isFalse();
    assertThat(supplier.get()).isEqualTo(expectedValue);
    assertThat(this.cacheableService.wasCacheMiss()).isTrue();
    assertThat(supplier.get()).isEqualTo(expectedValue);
    assertThat(this.cacheableService.wasCacheMiss()).isFalse();
  }

  @Test
  public void existingCacheWorks() {
    invokeAndAssertCacheOperation(() -> this.cacheableService.serviceMethodOne(), "TEST");
  }

  @Test(expected = IllegalArgumentException.class)
  public void nonExistingCacheFails() {

    try {
      invokeAndAssertCacheOperation(() -> this.cacheableService.serviceMethodTwo(), "MOCK");
    }
    catch (IllegalArgumentException expected) {

      assertThat(expected).hasMessageContaining("NonExistingCache");
      assertThat(expected).hasNoCause();

      throw expected;
    }
  }

  @Configuration
  @EnableCaching
  static class TestConfiguration {

    @Bean
    ConcurrentMapCacheManager cacheManager() {
      return new ConcurrentMapCacheManager("ExistingCache");
    }

    @Bean
    TestCacheableService cacheableService() {
      return new TestCacheableService();
    }
  }

  @Service
  static class TestCacheableService {

    private AtomicBoolean cacheMiss = new AtomicBoolean(false);

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
}
