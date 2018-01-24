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

package example.app.geode.xml.server;

import static example.app.geode.cache.util.CacheUtils.close;
import static org.cp.elements.util.ArrayUtils.nullSafeArray;
import static org.cp.elements.util.CollectionUtils.nullSafeList;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.cp.elements.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The GeodeServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeServerApplication implements Runnable {

  protected static final int GEMFIRE_JMX_MANAGER_PORT = 1099;

  protected static final String GEMFIRE_CACHE_XML = "geode-server-cache.xml";
  protected static final String GEMFIRE_JMX_MANAGER = "true";
  protected static final String GEMFIRE_JMX_MANAGER_START = "true";
  protected static final String GEMFIRE_LOCATOR_HOST_PORT = "localhost[10334]";
  protected static final String GEMFIRE_LOG_LEVEL = "config";

  public static void main(String[] args) {
    GeodeServerApplication.run(args);
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
   * Constructs an instance of the {@link example.app.geode.java.server.GeodeServerApplication} class to configure and fork/launch
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
   * Constructs an instance of the {@link example.app.geode.java.server.GeodeServerApplication} class to configure and fork/launch
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
      gemfireCache = gemfireCache(gemfireProperties(), cacheXmlFile());
      SystemUtils.promptPressEnterToExit();
    }
    catch (Exception cause) {
      getLogger().error("", cause);
    }
    finally {
      close(gemfireCache);
    }
  }

  void waitOnInput() {
    getLogger().info("Press <enter> to exit.");
    new Scanner(System.in).nextLine();
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
    return example.app.geode.java.server.GeodeServerApplication.class.getSimpleName();
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

  String cacheXmlFile() {
    return System.getProperty("configuration-example.gemfire.cache.xml", GEMFIRE_CACHE_XML);
  }

  Cache gemfireCache(Properties gemfireProperties, String cacheXmlFile) {
    return new CacheFactory(gemfireProperties).set("cache-xml-file", cacheXmlFile).create();
  }
}
