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

package example.app.geode.java.server;

import static example.app.geode.cache.util.CacheUtils.close;
import static org.cp.elements.util.ArrayUtils.nullSafeArray;
import static org.cp.elements.util.CollectionUtils.nullSafeList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.server.CacheServer;
import org.cp.elements.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.app.geode.cache.loader.EchoCacheLoader;

/**
 * The {@link GeodeServerApplication} class uses Apache Geode's public, Java API
 * to configure and bootstrap an Apache Geode {@link CacheServer} application.
 *
 * @author John Blum
 * @see java.lang.Runnable
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.CacheFactory
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.server.CacheServer
 * @see <a href="http://geode.apache.org/releases/latest/javadoc/index.html">Apache Geode Public Java API</a>
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeServerApplication implements Runnable {

  protected static final int GEMFIRE_CACHE_SERVER_MAX_CONNECTIONS = 50;
  protected static final int GEMFIRE_CACHE_SERVER_MAX_TIME_BETWEEN_PINGS = intValue(TimeUnit.MINUTES.toMillis(5));
  protected static final int GEMFIRE_CACHE_SERVER_PORT = CacheServer.DEFAULT_PORT;
  protected static final int GEMFIRE_JMX_MANAGER_PORT = 1099;

  protected static final String ECHO_REGION_NAME = "Echo";
  protected static final String GEMFIRE_CACHE_SERVER_BIND_ADDRESS = "localhost";
  protected static final String GEMFIRE_CACHE_SERVER_HOSTNAME_FOR_CLIENTS = GEMFIRE_CACHE_SERVER_BIND_ADDRESS;
  protected static final String GEMFIRE_JMX_MANAGER = "true";
  protected static final String GEMFIRE_JMX_MANAGER_START = "true";
  protected static final String GEMFIRE_LOCATOR_HOST_PORT = "localhost[10334]";
  protected static final String GEMFIRE_LOG_LEVEL = "config";

  public static void main(String[] args) {
    GeodeServerApplication.run(args);
  }

  protected static int intValue(Number value) {
    return value.intValue();
  }

  public static GeodeServerApplication newGeodeServerApplication(String[] args) {
    return new GeodeServerApplication(args);
  }

  public static GeodeServerApplication run(String[] args) {
    GeodeServerApplication geodeServerApplication = newGeodeServerApplication(args);
    geodeServerApplication.run();
    return geodeServerApplication;
  }

  private final List<String> arguments;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  /**
   * Constructs an instance of the {@link GeodeServerApplication} class to configure and fork/launch
   * an Apache Geode data server node.
   *
   * @param args {@link String} array of program arguments.
   * @throws NullPointerException if the program arguments are null.
   * @see #GeodeServerApplication(List)
   */
  public GeodeServerApplication(String[] args) {
    this(Arrays.asList(nullSafeArray(args, String.class)));
  }

  /**
   * Constructs an instance of the {@link GeodeServerApplication} class to configure and fork/launch
   * an Apache Geode data server node.
   *
   * @param args {@link List} of program arguments.
   * @throws IllegalArgumentException if the program arguments are null.
   */
  public GeodeServerApplication(List<String> args) {
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

  protected void run(List<String> arguments) {

    Cache gemfireCache = null;

    try {
      gemfireCache = gemfireCache(gemfireProperties());
      gemfireCacheServer(gemfireCache);
      echoRegion(gemfireCache);
      SystemUtils.promptPressEnterToExit();
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
    gemfireProperties.setProperty("jmx-manager", String.valueOf(jmxManager()));
    gemfireProperties.setProperty("jmx-manager-port", String.valueOf(jmxManagerPort()));
    gemfireProperties.setProperty("jmx-manager-start", String.valueOf(jmxManagerStart()));
    gemfireProperties.setProperty("log-level", logLevel());
    gemfireProperties.setProperty("start-locator", startLocator());

    return gemfireProperties;
  }

  String applicationName() {
    return GeodeServerApplication.class.getSimpleName();
  }

  boolean jmxManager() {
    return Boolean.parseBoolean(System.getProperty("configuration-example.gemfire.manager.enabled",
      GEMFIRE_JMX_MANAGER));
  }

  int jmxManagerPort() {
    return Integer.getInteger("configuration-example.gemfire.manager.port", GEMFIRE_JMX_MANAGER_PORT);
  }

  boolean jmxManagerStart() {
    return Boolean.parseBoolean(System.getProperty("configuration-example.gemfire.manager.start",
      GEMFIRE_JMX_MANAGER_START));
  }

  String logLevel() {
    return System.getProperty("configuration-example.gemfire.log.level",
      GEMFIRE_LOG_LEVEL);
  }

  String startLocator() {
    return System.getProperty("configuration-example.gemfire.locator.host-port",
      GEMFIRE_LOCATOR_HOST_PORT);
  }

  Cache gemfireCache(Properties gemfireProperties) {
    return new CacheFactory(gemfireProperties).create();
  }

  CacheServer gemfireCacheServer(Cache gemfireCache) throws IOException {

    CacheServer gemfireCacheServer = gemfireCache.addCacheServer();

    gemfireCacheServer.setBindAddress(GEMFIRE_CACHE_SERVER_BIND_ADDRESS);
    gemfireCacheServer.setHostnameForClients(GEMFIRE_CACHE_SERVER_HOSTNAME_FOR_CLIENTS);
    gemfireCacheServer.setMaxConnections(GEMFIRE_CACHE_SERVER_MAX_CONNECTIONS);
    gemfireCacheServer.setMaximumTimeBetweenPings(GEMFIRE_CACHE_SERVER_MAX_TIME_BETWEEN_PINGS);
    gemfireCacheServer.setPort(GEMFIRE_CACHE_SERVER_PORT);
    gemfireCacheServer.start();

    return gemfireCacheServer;
  }

  Region<Object, Object> echoRegion(Cache gemfireCache) {

    RegionFactory<Object, Object> echoRegion = gemfireCache.createRegionFactory(RegionShortcut.PARTITION);

    echoRegion.setCacheLoader(EchoCacheLoader.getInstance());

    return echoRegion.create(ECHO_REGION_NAME);
  }
}
