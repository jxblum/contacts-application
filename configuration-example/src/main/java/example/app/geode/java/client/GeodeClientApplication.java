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

package example.app.geode.java.client;

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
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.server.CacheServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GeodeClientApplication} class uses Apache Geode's public, Java API
 * to configure and bootstrap an Apache Geode {@link ClientCache} application.
 *
 * @author John Blum
 * @see java.lang.Runnable
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.client.ClientCacheFactory
 * @see <a href="http://geode.apache.org/releases/latest/javadoc/index.html">Apache Geode Public Java API</a>
 * @since 1.0.0
 */
public class GeodeClientApplication implements Runnable {

  protected static final int GEMFIRE_CACHE_SERVER_PORT = CacheServer.DEFAULT_PORT;

  protected static final String ECHO_REGION_NAME = "Echo";
  protected static final String GEMFIRE_CACHE_SERVER_HOST = "localhost";
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

      gemfireCache = gemfireCache(gemfireProperties());

      Region<String, String> echo = echoRegion(gemfireCache);

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
      getLogger().error("Failed to run GeodeClientApplication", cause);
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
    return GeodeClientApplication.class.getSimpleName();
  }

  String logLevel() {
    return System.getProperty("configuration-example.gemfire.log.level", GEMFIRE_LOG_LEVEL);
  }

  ClientCache gemfireCache(Properties gemfireProperties) {

    return new ClientCacheFactory(gemfireProperties)
      .addPoolServer(GEMFIRE_CACHE_SERVER_HOST, GEMFIRE_CACHE_SERVER_PORT)
      .create();
  }

  Region<String, String> echoRegion(ClientCache gemfireCache) {
    return gemfireCache.<String, String>createClientRegionFactory(ClientRegionShortcut.PROXY)
      .create(ECHO_REGION_NAME);
  }

  String sendEchoRequest(Region<String, String> echo, String echoRequest) {
    String echoResponse = echo.get(echoRequest);
    getLogger().info("Client said {}; Server said {}", echoRequest, echoResponse);
    return echoResponse;
  }
}
