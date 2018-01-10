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

package example.app.geode.xml.client;

import static example.app.geode.cache.util.CacheUtils.close;
import static org.assertj.core.api.Assertions.assertThat;
import static org.cp.elements.util.ArrayUtils.nullSafeArray;
import static org.cp.elements.util.CollectionUtils.nullSafeList;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GeodeClientApplication} class uses Apache Geode's XML configuration meta-data
 * to configure and bootstrap an Apache Geode {@link ClientCache} application.
 *
 * @author John Blum
 * @see java.lang.Runnable
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.ClientCacheFactory
 * @since 1.0.0
 */
public class GeodeClientApplication implements Runnable {

  protected static final String ECHO_REGION_NAME = "Echo";
  protected static final String GEMFIRE_CACHE_XML = "geode-client-cache.xml";
  protected static final String GEMFIRE_LOG_LEVEL = "config";

  public static void main(String[] args) {
    GeodeClientApplication.run(args);
  }

  public static GeodeClientApplication newGeodeClientApplication(String[] args) {
    return new GeodeClientApplication(args);
  }

  public static GeodeClientApplication run(String[] args) {
    GeodeClientApplication geodeClientApplication = newGeodeClientApplication(args);
    geodeClientApplication.run();
    return geodeClientApplication;
  }

  private final List<String> arguments;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public GeodeClientApplication(String[] args) {
    this(Arrays.asList(nullSafeArray(args, String.class)));
  }

  public GeodeClientApplication(List<String> args) {
    this.arguments = nullSafeList(args);
  }

  protected List<String> getArguments() {
    return this.arguments;
  }

  protected Logger getLogger() {
    return this.logger;
  }

  @Override
  public void run() {
    run(getArguments());
  }

  @SuppressWarnings("all")
  void run(List<String> arguments) {

    ClientCache gemfireCache = null;

    try {

      gemfireCache = gemfireCache(gemfireProperties(), cacheXmlFile());

      Region<String, String> echo = gemfireCache.getRegion(ECHO_REGION_NAME);

      assertThat(echo).isNotNull();
      assertThat(echo.getName()).isEqualTo(ECHO_REGION_NAME);
      assertThat(echo.isEmpty()).isTrue();
      assertThat(echo.size()).isEqualTo(0);
      assertThat(sendEchoRequest(echo, "Hello")).isEqualTo("Hello");
      assertThat(sendEchoRequest(echo, "Test")).isEqualTo("Test");
      assertThat(sendEchoRequest(echo, "Good-Bye")).isEqualTo("Good-Bye");
      assertThat(echo.isEmpty()).isTrue();
      assertThat(echo.size()).isEqualTo(0);
    }
    catch (Exception cause) {
      getLogger().error("", cause);
    }
    finally {
      close(gemfireCache);
    }
  }

  Properties gemfireProperties() {

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", applicationName());
    gemfireProperties.setProperty("log-level", logLevel());

    return gemfireProperties;
  }

  String applicationName() {
    return example.app.geode.java.client.GeodeClientApplication.class.getSimpleName();
  }

  String logLevel() {
    return System.getProperty("configuration-example.gemfire.log.level", GEMFIRE_LOG_LEVEL);
  }

  String cacheXmlFile() {
    return System.getProperty("configuration-example.gemfire.cache.xml", GEMFIRE_CACHE_XML);
  }

  ClientCache gemfireCache(Properties gemfireProperties, String cacheXmlFile) {
    return new ClientCacheFactory(gemfireProperties).set("cache-xml-file", cacheXmlFile).create();
  }

  String sendEchoRequest(Region<String, String> echo, String echoRequest) {
    String echoResponse = echo.get(echoRequest);
    this.logger.info("Client said {}; Server said {}", echoRequest, echoResponse);
    return echoResponse;
  }
}
