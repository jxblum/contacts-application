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

package example.app.spring.xml.client;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.apache.geode.cache.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * The {@link XmlConfiguredGeodeClientApplication} class is a {@link SpringBootApplication} that configures
 * and bootstraps an Apache Geode client JVM process using Spring Data for Apache Geodes's XML namespace.
 *
 * @author John Blum
 * @see org.springframework.boot.CommandLineRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.context.annotation.ImportResource
 * @see org.apache.geode.cache.Region
 * @since 1.0.0
 */
@SpringBootApplication
@ImportResource("spring-client-cache.xml")
@SuppressWarnings("all")
public class XmlConfiguredGeodeClientApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(XmlConfiguredGeodeClientApplication.class, args);
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
