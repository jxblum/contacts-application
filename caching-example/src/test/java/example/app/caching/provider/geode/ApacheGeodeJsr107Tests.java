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

package example.app.caching.provider.geode;

import javax.annotation.Resource;

import org.junit.runner.RunWith;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.Region;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.support.GemfireCache;
import org.springframework.data.gemfire.support.GemfireCacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.caching.provider.geode.ApacheGeodeJsr107Tests.TestConfiguration;
import example.app.caching.provider.support.AbstractJsr107CachingProviderTests;
import example.app.caching.provider.support.Calculator;

/**
 * Test suite of test cases testing the contract and functionality of Apache Geode serving
 * as a JSR-107 caching provider in Spring's Cache Abstraction using Spring Boot.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see example.app.caching.provider.support.Calculator
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("cache-manager")
@EnableAutoConfiguration(excludeName = "HazelcastAutoConfiguration")
@SpringBootTest(classes = TestConfiguration.class, webEnvironment = WebEnvironment.NONE)
public class ApacheGeodeJsr107Tests extends AbstractJsr107CachingProviderTests {

  @Configuration
  @PeerCacheApplication(name = "ApacheGeodeJsr107Tests")
  @SuppressWarnings("unused")
  static class GeodeConfiguration {

    @Bean(name = "Factorials")
    LocalRegionFactoryBean<Long, Long> factorialsRegion(Cache gemfireCache) {
      LocalRegionFactoryBean<Long, Long> factorialsRegion = new LocalRegionFactoryBean<>();

      factorialsRegion.setCache(gemfireCache);
      factorialsRegion.setClose(false);
      factorialsRegion.setPersistent(false);

      return factorialsRegion;
    }
  }

  @Configuration
  @EnableCaching
  @Import({ GeodeConfiguration.class, Calculator.class })
  @SuppressWarnings("unused")
  static class TestConfiguration {

    @Resource(name = "Factorials")
    Region<Long, Long> factorials;

    @Bean
    @Profile("cache-manager")
    GemfireCacheManager cacheManager(Cache gemfireCache) {
      GemfireCacheManager cacheManager = new GemfireCacheManager();
      cacheManager.setCache(gemfireCache);
      return cacheManager;
    }

    @Bean
    @Profile("cache")
    GemfireCache factorialsCache() {
      return new GemfireCache(factorials);
    }
  }
}
