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

package example.test.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;

/**
 * The {@link ClusterConfigurationExampleServer} class is a {@link SpringBootApplication} that configures
 * and bootstraps a Pivotal GemFire or Apache Geode server.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableLocator
 * @see org.springframework.data.gemfire.config.annotation.EnableManager
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(name = "ClusterConfigurationExampleServer")
@SuppressWarnings("unused")
public class ClusterConfigurationExampleServer {

  public static void main(String[] args) {
    SpringApplication.run(ClusterConfigurationExampleServer.class, args);
  }

  @EnableLocator
  @EnableManager
  @Profile("locator-manager")
  static class ServicesConfiguration {
  }
}
