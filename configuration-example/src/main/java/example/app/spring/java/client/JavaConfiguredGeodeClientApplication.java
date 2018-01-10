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

package example.app.spring.java.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.gemfire.client.ClientCacheFactoryBean;
import org.springframework.data.gemfire.support.ConnectionEndpoint;

import example.app.config.client.EchoClientApplicationConfiguration;

/**
 * The {@link JavaConfiguredGeodeClientApplication} class is a {@link SpringBootApplication} that configures
 * and bootstraps an Apache Geode client JVM process using Spring Java-based configuration meta-data.
 *
 * @author John Blum
 * @see org.springframework.boot.CommandLineRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.gemfire.client.ClientCacheFactoryBean
 * @see org.apache.geode.cache.Region
 * @see example.app.config.client.EchoClientApplicationConfiguration
 * @since 1.0.0
 */
@SpringBootApplication
@Import(EchoClientApplicationConfiguration.class)
@SuppressWarnings("unused")
public class JavaConfiguredGeodeClientApplication implements CommandLineRunner {

  protected static final String GEMFIRE_LOG_LEVEL = "config";

  public static void main(String[] args) {
    SpringApplication.run(JavaConfiguredGeodeClientApplication.class);
  }

  @Bean
  static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  Properties gemfireProperties(
      @Value("${configuration-example.gemfire.log.level:"+GEMFIRE_LOG_LEVEL+"}") String logLevel) {

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", applicationName());
    gemfireProperties.setProperty("log-level", logLevel);

    return gemfireProperties;
  }

  String applicationName() {
    return JavaConfiguredGeodeClientApplication.class.getSimpleName();
  }

  @Bean
  ClientCacheFactoryBean gemfireCache(@Qualifier("gemfireProperties") Properties gemfireProperties,
      @Value("${configuration-example.gemfire.cache.server.host:localhost}") String cacheServerHost,
      @Value("${configuration-example.gemfire.cache.server.port:40404}") int cacheServerPort) {

    ClientCacheFactoryBean gemfireCache = new ClientCacheFactoryBean();

    gemfireCache.setClose(true);
    gemfireCache.setProperties(gemfireProperties);
    gemfireCache.setServers(Collections.singletonList(new ConnectionEndpoint(cacheServerHost, cacheServerPort)));

    return gemfireCache;
  }

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Resource(name = "Echo")
  private Region<String, String> echo;

  @Override
  public void run(String... args) throws Exception {
    assertThat(sendEchoRequest("Hello")).isEqualTo("Hello");
    assertThat(sendEchoRequest("TEST")).isEqualTo("TEST");
    assertThat(sendEchoRequest("Good-Bye")).isEqualTo("Good-Bye");
  }

  private String sendEchoRequest(String echoRequest) {
    String echoResponse = this.echo.get(echoRequest);
    this.logger.info("Client says {}; Server says {}", echoRequest, echoResponse);
    return echoResponse;
  }
}
