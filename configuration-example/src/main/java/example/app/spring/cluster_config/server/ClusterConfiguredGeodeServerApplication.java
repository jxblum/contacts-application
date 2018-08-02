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

package example.app.spring.cluster_config.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.LookupRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The ClusterConfiguredGeodeServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@SuppressWarnings("unused")
public class ClusterConfiguredGeodeServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClusterConfiguredGeodeServerApplication.class, args);
  }

  @Bean
  ApplicationRunner runner(GemfireTemplate customersTemplate) {

    return args -> {

      Customer jonDoe = Customer.named("Jon Doe").identifiedBy(1L);

      customersTemplate.put(jonDoe.getId(), jonDoe);

      Customer jonDoeCopy = customersTemplate.get(jonDoe.getId());

      assertThat(jonDoeCopy).isEqualTo(jonDoe);
    };
  }

  @PeerCacheApplication(name = "ClusterConfiguredGeodeServerApplication", locators = "localhost[10334]")
  @EnablePdx
  static class GeodeConfiguration {

    @Bean
    PeerCacheConfigurer useClusterConfigurationConfigurer() {
      return (beanName, cacheFactoryBean) -> cacheFactoryBean.setUseClusterConfiguration(true);
    }

    @Bean("Customers")
    LookupRegionFactoryBean<Long, Customer> customersRegion(GemFireCache gemfireCache) {

      LookupRegionFactoryBean<Long, Customer> customersRegion = new LookupRegionFactoryBean<>();

      customersRegion.setCache(gemfireCache);

      return customersRegion;
    }

    @Bean("CustomersTemplate")
    GemfireTemplate customersTemplate(@Qualifier("Customers") Region<?, ?> customers) {
      return new GemfireTemplate(customers);
    }
  }

  @Data
  @RequiredArgsConstructor(staticName = "named")
  static class Customer {

    @Id
    private Long id;

    @NonNull
    private String name;

    Customer identifiedBy(Long id) {
      this.id = id;
      return this;
    }
  }
}
