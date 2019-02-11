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

package example.app.geode.cache.server;

import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.geode.DataSerializer;
import org.apache.geode.cache.ExpirationAction;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.internal.InternalDataSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.session.Session;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.util.ObjectUtils;

/**
 * The SessionExampleServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(name = "SpringSessionExampleServerApplication")
@EnableLocator
@EnableManager(start = true)
@EnablePdx(ignoreUnreadFields = true, readSerialized = true)
@SuppressWarnings("unused")
public class SessionExampleServerApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(SessionExampleServerApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(args);
  }

  @Configuration
  @Profile("!manual")
  @EnableGemFireHttpSession(
    regionName = "Sessions",
    maxInactiveIntervalInSeconds = 300,
    sessionSerializerBeanName = GemFireHttpSessionConfiguration.SESSION_DATA_SERIALIZER_BEAN_NAME
  )
  static class AnnotationBasedSpringSessionConfiguration { }

  @Configuration
  @Profile("manual")
  static class JavaConfigBasedSpringSessionConfiguration {

    // NOTE: no GemFire Serialization framework configuration
    // NOTE: no Session Region Index configuration
    // NOTE: no Session Region Repository configuration

    @Bean("Sessions")
    PartitionedRegionFactoryBean<String, Session> sessionsRegion(GemFireCache gemfireCache,
        @Qualifier("sessionsRegionAttributes") RegionAttributes<String, Session> sessionRegionAttributes) {

      PartitionedRegionFactoryBean<String, Session> sessionsRegion =
        new PartitionedRegionFactoryBean<>();

      sessionsRegion.setAttributes(sessionRegionAttributes);
      sessionsRegion.setCache(gemfireCache);
      sessionsRegion.setClose(false);
      sessionsRegion.setPersistent(false);

      return sessionsRegion;
    }

    @Bean
    @SuppressWarnings("unchecked")
    RegionAttributesFactoryBean sessionsRegionAttributes() {

      ExpirationAttributes sessionExpiration =
        new ExpirationAttributes(Long.valueOf(TimeUnit.MINUTES.toSeconds(30)).intValue(),
          ExpirationAction.INVALIDATE);

      RegionAttributesFactoryBean sessionRegionAttributes = new RegionAttributesFactoryBean();

      sessionRegionAttributes.setKeyConstraint(String.class);
      sessionRegionAttributes.setEntryIdleTimeout(sessionExpiration);
      sessionRegionAttributes.setValueConstraint(Session.class);

      return sessionRegionAttributes;
    }

    @Bean
    GemfireTemplate sessionsRegionTemplate(GemFireCache gemfireCache) {
      return new GemfireTemplate(gemfireCache.getRegion("Sessions"));
    }
  }

  @Configuration
  @Profile("debug")
  static class DebugLoggingConfiguration {

    @Bean
    ApplicationRunner runner(GemFireCache gemfireCache) {
      return args -> printRegisteredDataSerializers();
    }

    private void printRegisteredDataSerializers() {

      System.err.println("Server Registered DataSerializers:");

      Arrays.stream(nullSafeArray(InternalDataSerializer.getSerializers(), DataSerializer.class))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet())
        .forEach(dataSerializer ->
          System.err.printf("Registered DataSerializer [%s]%n", ObjectUtils.nullSafeClassName(dataSerializer)));
    }
  }
}
