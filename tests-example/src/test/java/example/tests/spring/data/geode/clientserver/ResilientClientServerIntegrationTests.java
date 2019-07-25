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
package example.tests.spring.data.geode.clientserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication.Locator;
import org.springframework.data.gemfire.tests.util.SocketUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration Test illustrating a Spring Data for Apache Geode (or Pivotal GemFire) {@link ClientCache} application
 * that does not strictly require a Geode/GemFire cluster of peer servers to be running for the {@link ClientCache}
 * to be usable by the application, and to equally demonstrate that the cluster can be "conditionally" used
 * when the cluster is available.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.ClientRegionShortcut
 * @see org.springframework.context.annotation.Condition
 * @see org.springframework.context.annotation.ConditionContext
 * @see org.springframework.context.annotation.Conditional
 * @see org.springframework.data.gemfire.client.ClientRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see <a href="https://stackoverflow.com/questions/57085860/spring-data-gemfire-loose-coupling-cache-with-spring-boot-startup">StackOverflow</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class ResilientClientServerIntegrationTests {

  @Resource(name = "Example")
  private Region<Object, Object> example;

  @Before
  public void setup() {

    assertThat(this.example).isNotNull();
    assertThat(this.example.getName()).isEqualTo("Example");
  }

  @Test
  public void exampleRegionDataAccessOperationsAreSuccessful() {

    this.example.put(1, "test");

    assertThat(this.example.get(1)).isEqualTo("test");
  }

  @Conditional(GemFireClusterAvailableCondition.class)
  @ClientCacheApplication(locators = @Locator)
  static class ClientConfiguration {

    @Bean("Example")
    ClientRegionFactoryBean<Object, Object> exampleRegion(GemFireCache cache,
        @Value("${spring.data.gemfire.cache.client.region.shortcut:PROXY}") ClientRegionShortcut shortcut) {

      ClientRegionFactoryBean<Object, Object> exampleRegion = new ClientRegionFactoryBean<>();

      exampleRegion.setCache(cache);
      exampleRegion.setShortcut(shortcut);

      return exampleRegion;
    }
  }

  static class GemFireClusterAvailableCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

      Socket locatorSocket = null;

      try {

        // NOTE: You can make the configuration of the Locator host and port configurable, such as in a Spring Boot,
        // application.properties file, and then access the property from the Environment object
        // accessible from ConditionContext.
        SocketAddress locatorSocketAddress = new InetSocketAddress("localhost", 10334);

        locatorSocket = new Socket();
        locatorSocket.connect(locatorSocketAddress, 5000); // NOTE: Your timeout could be configurable as well.

        return true;
      }
      catch (Exception ignore) {

        System.setProperty("spring.data.gemfire.cache.client.region.shortcut", "LOCAL");

        // NOTE: You could return false if you wanted to prevent even GemFire client configuration from bootstrapping.
        // However, you can still use GemFire in local-only mode and simply configure your client Regions to be "LOCAL".
        // That way, data access operations on client Regions will only be performed locally and won't expect a server
        // Region to exist, as is the case with a client PROXY, or CACHING_PROXY, Region.
        return true;
      }
      finally {
        SocketUtils.close(locatorSocket);
      }
    }
  }
}
