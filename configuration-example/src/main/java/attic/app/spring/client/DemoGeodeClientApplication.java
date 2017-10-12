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

package attic.app.spring.client;

import javax.annotation.Resource;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.cache.config.EnableGemfireCaching;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

import attic.app.spring.client.service.CustomerService;
import example.app.core.lang.RunnableUtils;
import example.app.geode.cache.loader.EchoCacheLoader;
import example.app.model.Customer;

/**
 * The ClientApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@ClientCacheApplication
@EnableGemfireCaching
@EnableCachingDefinedRegions
@EnableGemfireRepositories
@EnableEntityDefinedRegions(basePackageClasses = Customer.class)
@EnableClusterConfiguration(useHttp = true)
public class DemoGeodeClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(DemoGeodeClientApplication.class, args);
  }

  @Resource(name = "Echo")
  private Region<?, ?>  echo;

  @Bean
  ApplicationRunner run(CustomerService customerService) {

    return args -> {

      System.err.printf("%s%n", echo.get("HELLO"));

      RunnableUtils.timedRun(() -> {

        Customer jonDoe = Customer.newCustomer("Jon", "Doe").with("12345");

        System.err.printf("%s%n", customerService.findBy(jonDoe));
        System.err.printf("%s%n", customerService.findBy(jonDoe));

      }).ifPresent(time -> System.err.printf("%d ms%n", time));
    };
  }

  @Bean("Echo")
  public ClientRegionFactoryBean<Object, Object> clientRegion(GemFireCache gemfireCache) {

    ClientRegionFactoryBean<Object, Object> clientRegion = new ClientRegionFactoryBean<>();

    clientRegion.setCache(gemfireCache);
    clientRegion.setCacheLoader(EchoCacheLoader.getInstance());
    clientRegion.setClose(false);
    clientRegion.setShortcut(ClientRegionShortcut.PROXY);

    return clientRegion;
  }
}
