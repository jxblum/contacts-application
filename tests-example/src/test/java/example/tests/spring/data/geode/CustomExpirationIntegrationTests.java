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

package example.tests.spring.data.geode;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.CustomExpiry;
import org.apache.geode.cache.ExpirationAction;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.Region.Entry;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * The CustomExpirationIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SuppressWarnings("unused")
public class CustomExpirationIntegrationTests {

  @Resource(name = "Example")
  private Region<Object, Object> example;

  @Test
  public void customExpirationTest() throws InterruptedException {

    this.example.put(1L, "one");
    this.example.put(2L, "two");

    Thread.sleep(1000L);

    assertThat(this.example.get(1L)).isEqualTo("one");
    assertThat(this.example.get(2L)).isEqualTo("two");

    Thread.sleep(2500L);

    assertThat(this.example.containsKey(1L)).isTrue();
    assertThat(this.example.containsKey(2L)).isTrue();

    this.example.put(3L, "three");

    Thread.sleep(2500L);

    assertThat(this.example.containsKey(1L)).isFalse();
    assertThat(this.example.containsKey(2L)).isFalse();
    assertThat(this.example.get(3L)).isEqualTo("three");

    Thread.sleep(5001L);

    assertThat(this.example.containsKey(1L)).isFalse();
    assertThat(this.example.containsKey(2L)).isFalse();
    assertThat(this.example.containsKey(3L)).isFalse();
    assertThat(TestCustomExpiry.counter.get()).isGreaterThanOrEqualTo(9);
  }

  @PeerCacheApplication
  static class TestConfiguration {

    @Bean("Example")
    public ReplicatedRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

      ReplicatedRegionFactoryBean<Object, Object> exampleRegion = new ReplicatedRegionFactoryBean<>();

      exampleRegion.setCache(gemfireCache);
      exampleRegion.setClose(false);
      exampleRegion.setCustomEntryIdleTimeout(new TestCustomExpiry());
      exampleRegion.setStatisticsEnabled(true);
      exampleRegion.setPersistent(false);

      return exampleRegion;
    }
  }

  private static class TestCustomExpiry implements CustomExpiry<Object, Object> {

    private static final ExpirationAttributes FIXED_EXPIRATION_ATTRIBUTES =
      new ExpirationAttributes(5, ExpirationAction.DESTROY);

    private static final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public ExpirationAttributes getExpiry(Entry<Object, Object> entry) {

      System.out.printf("COUNT [%d]%n", counter.incrementAndGet());

      return FIXED_EXPIRATION_ATTRIBUTES;
    }
  }
}
