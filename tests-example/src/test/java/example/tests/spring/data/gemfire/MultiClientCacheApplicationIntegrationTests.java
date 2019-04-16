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

package example.tests.spring.data.gemfire;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests test the configuration of multiple application components annotated with
 * {@link ClientCacheApplication}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see <a href="https://stackoverflow.com/questions/55698394/can-i-have-two-clientcacheapplication-in-the-same-spring-boot-application">Can I have two @ClientCacheApplication in the same Spring Boot Application?</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class MultiClientCacheApplicationIntegrationTests {

  private static final String LOG_LEVEL = "error";

  @Autowired
  private ClientCache clientCache;

  @Resource(name = "People")
  private Region<Object, Object> people;

  @Resource(name = "Job")
  private Region<Object, Object> job;

  @Test
  public void gemfireConfigurationIsCorrect() {

    assertThat(this.clientCache).isNotNull();

    assertThat(this.clientCache.rootRegions().stream().map(Region::getName))
      .containsExactlyInAnyOrder("People", "Job");

    assertThat(this.people).isNotNull();
    assertThat(this.people.getName()).isEqualTo("People");
    assertThat(this.people.getRegionService()).isEqualTo(this.clientCache);
    assertThat(this.people).isNotEqualTo(this.job);

    assertThat(this.job).isNotNull();
    assertThat(this.job.getName()).isEqualTo("Job");
    assertThat(this.job.getRegionService()).isEqualTo(this.clientCache);
    assertThat(this.job).isNotEqualTo(this.people);
  }

  @ClientCacheApplication(logLevel = LOG_LEVEL)
  static class TestGemFireConfigurationOne {

    @Bean("People")
    public ClientRegionFactoryBean<Object, Object> peopleRegion(GemFireCache gemfireCache) {

      ClientRegionFactoryBean<Object, Object> peopleRegion = new ClientRegionFactoryBean<>();

      peopleRegion.setCache(gemfireCache);
      peopleRegion.setClose(false);
      peopleRegion.setShortcut(ClientRegionShortcut.LOCAL);

      return peopleRegion;
    }
  }

  @ClientCacheApplication(logLevel = LOG_LEVEL)
  static class TestGemFireConfigurationTwo {

    @Bean("Job")
    public ClientRegionFactoryBean<Object, Object> jobRegion(GemFireCache gemfireCache) {

      ClientRegionFactoryBean<Object, Object> jobRegion = new ClientRegionFactoryBean<>();

      jobRegion.setCache(gemfireCache);
      jobRegion.setClose(false);
      jobRegion.setShortcut(ClientRegionShortcut.LOCAL);

      return jobRegion;
    }

  }
}
