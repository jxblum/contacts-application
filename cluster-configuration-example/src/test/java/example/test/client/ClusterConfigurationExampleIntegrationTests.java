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

package example.test.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.client.ClientCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.data.gemfire.support.ConnectionEndpoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.test.client.model.Customer;
import example.test.client.repo.CustomerRepository;

/**
 * Integration tests for Spring Data GemFire/Geode's {@link EnableClusterConfiguration} auto-configuration push
 * from a Spring (Data GemFire/Geode) {@link ClientCache} application to a Pivotal GemFire
 * or Apache Geode cluster.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer
 * @see org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see <a href="https://docs.spring.io/spring-data/geode/docs/current/reference/html/#bootstrap-annotation-config-cluster">Configuring Cluster Configuration Push</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class ClusterConfigurationExampleIntegrationTests {

  @Autowired
  private CustomerRepository customerRepository;

  @Test
  public void saveAndFindCustomer() {

    Customer jonDoe = Customer.newCustomer(1L, "Jon Doe");

    this.customerRepository.save(jonDoe);

    Customer jonDoeLoaded = this.customerRepository.findByName("Jon Doe");

    assertThat(jonDoeLoaded).isNotNull();
    assertThat(jonDoeLoaded).isNotSameAs(jonDoe);
    assertThat(jonDoeLoaded).isEqualTo(jonDoe);
  }

  @ClientCacheApplication(name = "ClusterConfigurationExampleIntegrationTests")
  @EnableClusterConfiguration(useHttp = true)
  @EnableEntityDefinedRegions(basePackageClasses = Customer.class)
  @EnableGemfireRepositories(basePackageClasses = CustomerRepository.class)
  @EnablePdx
  static class TestConfiguration {

    @Bean
    ClientCacheConfigurer clientCachePoolConfigurer(
        @Value("${spring.data.gemfire.locator.host:localhost}") String host,
        @Value("${spring.data.gemfire.locator.port:10334}") int port ) {

      return (beanName, clientCacheFactoryBean) ->
        clientCacheFactoryBean.setLocators(Collections.singletonList(new ConnectionEndpoint(host, port)));
    }
  }
}
