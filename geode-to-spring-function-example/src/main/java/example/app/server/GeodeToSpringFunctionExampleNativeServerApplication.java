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

package example.app.server;

import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newRuntimeException;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.server.CacheServer;
import org.cp.elements.lang.StringUtils;
import org.springframework.data.gemfire.GemfireUtils;
import org.springframework.data.gemfire.function.support.SpringDefinedFunctionAwareRegistrar;

import example.app.chat.model.Chat;
import example.app.server.function.SpellCheckerWithAutoCorrectFunction;

/**
 * The GeodeToSpringFunctionExampleNativeServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeToSpringFunctionExampleNativeServerApplication {

  private static final int GEMFIRE_CACHE_SERVER_PORT = CacheServer.DEFAULT_PORT;

  private static final SpringDefinedFunctionAwareRegistrar springDefinedFunctionAwareRegistrar =
    new SpringDefinedFunctionAwareRegistrar();

  private static final String CHATS_REGION_NAME = "Chats";
  private static final String GEMFIRE_CACHE_SERVER_HOSTNAME_FOR_CLIENTS = "localhost";
  private static final String GEMFIRE_CACHE_SERVER_HOSTNAME_FOR_CLIENTS_PROPERTY = "gemfire.cache.server.hostname-for-clients";
  private static final String GEMFIRE_CACHE_SERVER_PORT_PROPERTY = "gemfire.cache.server.port";
  private static final String GEMFIRE_LOG_LEVEL = "config";

  private static final String[] functionArguments = {
    SpellCheckerWithAutoCorrectFunction.class.getPackage().getName(),
  };

  public static void main(String[] args) {

    Cache gemfireCache = registerShutdownHook(gemfireCacheServer(gemfireCache(gemfireProperties())));

    Region<String, Chat> chatsRegion = chatsRegion(gemfireCache);

    registerSpringDefinedFunctionAwareRegistrar(gemfireCache);
    //executeSpringDefinedFunctionAwareRegistrar(gemfireCache);
  }

  private static Properties gemfireProperties() {

    Properties gemfireProperties = new Properties();

    String memberName = GeodeToSpringFunctionExampleNativeServerApplication.class.getSimpleName();

    gemfireProperties.setProperty("name", memberName);
    gemfireProperties.setProperty("log-level", GEMFIRE_LOG_LEVEL);
    //gemfireProperties.setProperty("locators", "localhost[10334]");
    gemfireProperties.setProperty("jmx-manager", "true");
    gemfireProperties.setProperty("jmx-manager-start", "true");
    //gemfireProperties.setProperty("member-timeout", "600000");
    gemfireProperties.setProperty("start-locator", "localhost[10334]");

    return gemfireProperties;
  }

  private static Cache gemfireCache(Properties gemfireProperties) {
    return new CacheFactory(gemfireProperties).create();
  }

  private static Cache gemfireCacheServer(Cache gemfireCache) {

    CacheServer cacheServer = gemfireCache.addCacheServer();

    String host = System.getProperty(GEMFIRE_CACHE_SERVER_HOSTNAME_FOR_CLIENTS_PROPERTY,
      GEMFIRE_CACHE_SERVER_HOSTNAME_FOR_CLIENTS);

    int port = Optional.ofNullable(System.getProperty(GEMFIRE_CACHE_SERVER_PORT_PROPERTY))
      .map(StringUtils::getDigits)
      .filter(StringUtils::hasText)
      .map(Integer::valueOf)
      .orElse(GEMFIRE_CACHE_SERVER_PORT);

    cacheServer.setBindAddress(host);
    cacheServer.setHostnameForClients(host);
    cacheServer.setPort(port);
    start(cacheServer, host, port);

    return gemfireCache;
  }

  private static void start(CacheServer cacheServer, String host, int port) {

    try {
      cacheServer.start();
    }
    catch (IOException cause) {
      throw newRuntimeException(cause, "Failed to start CacheServer on host [%s] and port [%d]",
        host, port);
    }
  }

  private static Region<String, Chat> chatsRegion(Cache gemfireCache) {

    RegionFactory<String, Chat> chatsRegion =
      gemfireCache.createRegionFactory(RegionShortcut.PARTITION);

    chatsRegion.setKeyConstraint(String.class);
    chatsRegion.setValueConstraint(Chat.class);

    return chatsRegion.create(CHATS_REGION_NAME);
  }

  @SuppressWarnings("unchecked")
  private static void executeSpringDefinedFunctionAwareRegistrar(Cache gemfireCache) {

    FunctionService.onMember(gemfireCache.getDistributedSystem().getDistributedMember())
      .setArguments(functionArguments)
      .execute(springDefinedFunctionAwareRegistrar);
  }

  private static void registerSpringDefinedFunctionAwareRegistrar(Cache gemfireCache) {
    FunctionService.registerFunction(springDefinedFunctionAwareRegistrar);
  }

  private static Cache registerShutdownHook(Cache gemfireCache) {

    Runtime.getRuntime().addShutdownHook(new Thread(() ->
      GemfireUtils.close(gemfireCache), "Apache Geode/Pivotal GemFire Cache Shutdown Thread"));

    return gemfireCache;
  }
}
