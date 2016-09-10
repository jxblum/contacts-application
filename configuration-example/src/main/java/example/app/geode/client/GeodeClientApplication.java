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

package example.app.geode.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.server.CacheServer;

import org.springframework.util.Assert;

/**
 * The GeodeClientApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class GeodeClientApplication implements Runnable {

  protected static final int GEMFIRE_SERVER_PORT = CacheServer.DEFAULT_PORT;

  protected static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";
  protected static final String ECHO_REGION_NAME = "Echo";
  protected static final String GEMFIRE_SERVER_HOST = "localhost";

  public static void main(String[] args) {
    GeodeClientApplication.run(args);
  }

  public static GeodeClientApplication newGeodeClientApplication(String[] args) {
    return new GeodeClientApplication(args);
  }

  public static GeodeClientApplication run(String[] args) {
    GeodeClientApplication cacheClientApplication = newGeodeClientApplication(args);
    cacheClientApplication.run();
    return cacheClientApplication;
  }

  private final List<String> arguments;

  private Region<String, String> echo;

  public GeodeClientApplication(String[] args) {
    this(Arrays.asList(args));
  }

  public GeodeClientApplication(List<String> args) {
    Assert.notNull(args, "Program arguments must not be null");
    this.arguments = args;
  }

  @Override
  public void run() {
    run(this.arguments);
  }

  @SuppressWarnings("all")
  void run(List<String> arguments) {
    ClientCache gemfireCache = null;

    try {
      gemfireCache = gemfireCache(gemfireProperties());
      echo = echoRegion(gemfireCache);

      assertThat(echo.getName()).isEqualTo(ECHO_REGION_NAME);
      assertThat(echo.isEmpty()).isTrue();
      assertThat(echo.size()).isEqualTo(0);
      assertThat(sendEchoRequest("Hello")).isEqualTo("Hello");
      assertThat(sendEchoRequest("Test")).isEqualTo("Test");
      assertThat(sendEchoRequest("Good-Bye")).isEqualTo("Good-Bye");
      assertThat(echo.isEmpty()).isTrue();
      assertThat(echo.size()).isEqualTo(0);
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
    }
    finally {
      close(gemfireCache);
    }
  }

  boolean close(GemFireCache cache) {
    try {
      if (cache != null) {
        cache.close();
        return true;
      }
    }
    catch (Exception ignore) {
    }

    return false;
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
    return System.getProperty("gemfire.log.level", DEFAULT_GEMFIRE_LOG_LEVEL);
  }

  ClientCache gemfireCache(Properties gemfireProperties) {
    return new ClientCacheFactory(gemfireProperties)
      .addPoolServer(GEMFIRE_SERVER_HOST, GEMFIRE_SERVER_PORT)
      .create();
  }

  Region<String, String> echoRegion(ClientCache gemfireCache) {
    return gemfireCache.<String, String>createClientRegionFactory(ClientRegionShortcut.PROXY).create(ECHO_REGION_NAME);
  }

  String sendEchoRequest(String echoRequest) {
    String echoResponse = echo.get(echoRequest);
    System.err.printf("Client said [%1$s]; Server said [%2$s]%n", echoRequest, echoResponse);
    return echoResponse;
  }
}
