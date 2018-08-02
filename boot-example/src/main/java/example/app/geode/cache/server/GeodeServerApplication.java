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

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.server.CacheServer;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.EnablePdx;

/**
 * The GeodeServerApplication class is a {@link SpringBootApplication} that configures and bootstraps an Apache Geode
 * {@link Cache peer Cache} instance, running with a {@link CacheServer} to allow connections from cache client
 * applications.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableLocator
 * @see org.springframework.data.gemfire.config.annotation.EnableManager
 * @see org.springframework.data.gemfire.config.annotation.EnablePdx
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(name = "BookExampleServerApplication", locators = "localhost[10334]")
//@PeerCacheApplication(name = "BookExampleServerApplication", locators = "localhost[10334]")
@EnablePdx(readSerialized = true)
@SuppressWarnings("unused")
public class GeodeServerApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(GeodeServerApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(args);
  }

  @Profile("locator-manager")
  @Configuration
  @EnableLocator
  @EnableManager(start = true)
  static class LocatorManagerConfiguration { }

}
