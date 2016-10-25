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

package demo.app.client;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;

/**
 * The {@link DemoGeodeClientApplication} class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class DemoGeodeClientApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(DemoGeodeClientApplication.class, args);
  }

  @Resource(name = "Echo")
  private Region<String, String> echo;

  @Override
  public void run(String... args) throws Exception {
    for (String key : args) {
      String value = echo.get(key);
      assertThat(key).isEqualTo(value);
      System.err.printf("Client says [%1$s]; Server says [%2$s]%n", key, value);
    }
  }

  @ClientCacheApplication(name = "DemoEchoClient", locators = { @ClientCacheApplication.Locator(port = 10334)})
  static class GeodeClientConfiguration {

    @Bean(name = "Echo")
    ClientRegionFactoryBean<String, String> echoRegion(GemFireCache gemfireCache) {
      ClientRegionFactoryBean<String, String> echoRegion = new ClientRegionFactoryBean<>();

      echoRegion.setCache(gemfireCache);
      echoRegion.setClose(false);
      echoRegion.setShortcut(ClientRegionShortcut.PROXY);

      return echoRegion;
    }
  }
}
