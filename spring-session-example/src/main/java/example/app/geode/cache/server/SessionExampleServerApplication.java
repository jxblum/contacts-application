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

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;

/**
 * The SessionExampleServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(name = "SpringSessionExampleServerApplication")
@EnableGemFireHttpSession(
  regionName = "Sessions",
  maxInactiveIntervalInSeconds = 300,
  sessionSerializerBeanName = GemFireHttpSessionConfiguration.SESSION_DATA_SERIALIZER_BEAN_NAME
)
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

  @Bean
  PeerCacheConfigurer pdxReadSerializedAndIgnoreUnreadFieldsConfigurer() {
    return (beanName, cacheFactoryBean) -> {
      cacheFactoryBean.setPdxIgnoreUnreadFields(true);
      cacheFactoryBean.setPdxReadSerialized(true);
    };
  }
}
