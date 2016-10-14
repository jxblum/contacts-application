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

package example.config.spring.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.Region;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The {@link HazelcastApiExampleTests} class demonstrates how to implement the Hazelcast.org's
 * example applications using the Hazelcast API (e.g. {@literal DistributedMap}
 * with Spring Data Geode's API (Annotation configuration meta-data).
 *
 * @author John Blum
 * @see <a href="http://docs.spring.io/spring-data-gemfire/docs/current/api/">Spring Data Geode API</a>
 * @see <a href="https://hazelcast.org/">Hazelcast API Examples</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class HazelcastApiExampleTests {

  @Resource(name = "myDistributedMap")
  private Region<String, String> myDistributedMap;

  @Test
  public void distributedMapOperations() {
    assertThat(myDistributedMap.put("key", "value")).isNull();
    assertThat(myDistributedMap.get("key")).isEqualTo("value");
    assertThat(myDistributedMap.putIfAbsent("somekey", "somevalue")).isNull();
    assertThat(myDistributedMap.replace("key", "value", "newvalue")).isTrue();
  }

  @Configuration
  @PeerCacheApplication
  static class ExampleApplicationConfiguration {

    @Bean
    LocalRegionFactoryBean<String, String> myDistributedMap(GemFireCache gemfireCache) {
      LocalRegionFactoryBean<String, String> myDistributedRegion = new LocalRegionFactoryBean<>();
      myDistributedRegion.setCache(gemfireCache);
      myDistributedRegion.setClose(false);
      return myDistributedRegion;
    }
  }
}
