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

package example.tests.geode;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.ExpirationAction;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.server.CacheServer;

/**
 * Java application to configure and bootstrap an Apache Geode Server using Apache Geode's API.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.CacheFactory
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.server.CacheServer
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SessionGeodeServer {

  private static final int CACHE_SERVER_PORT = CacheServer.DEFAULT_PORT;

  private static final String LOG_LEVEL = "config";
  private static final String SESSIONS_REGION_NAME = "Sessions";

  public static void main(String[] args) throws IOException {
    registerShutdownHook(sessionsRegion(gemfireCacheServer(gemfireCache(gemfireProperties()))));
  }

  private static Properties gemfireProperties() {

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", "SessionGeodeServer");
    gemfireProperties.setProperty("log-level", LOG_LEVEL);
    gemfireProperties.setProperty("locators", "localhost[10334]");
    gemfireProperties.setProperty("jmx-manager", "true");
    gemfireProperties.setProperty("jmx-manager-start", "true");
    gemfireProperties.setProperty("start-locator", "localhost[10334]");

    return gemfireProperties;
  }

  private static Cache gemfireCache(Properties gemfireProperties) {
    return new CacheFactory(gemfireProperties).create();
  }

  private static Cache gemfireCacheServer(Cache gemfireCache) throws IOException {

    CacheServer cacheServer = gemfireCache.addCacheServer();

    cacheServer.setMaximumTimeBetweenPings(15000);
    cacheServer.setPort(CACHE_SERVER_PORT);
    cacheServer.start();

    return gemfireCache;
  }

  private static Cache sessionsRegion(Cache gemfireCache) {

    gemfireCache.createRegionFactory(RegionShortcut.PARTITION)
      .setEntryIdleTimeout(new ExpirationAttributes(1800, ExpirationAction.INVALIDATE))
      .create(SESSIONS_REGION_NAME);

    return gemfireCache;
  }

  private static Cache registerShutdownHook(Cache gemfireCache) {

    Runtime.getRuntime().addShutdownHook(new Thread(() ->
      Optional.ofNullable(gemfireCache).ifPresent(GemFireCache::close), "Geode Cache Closer Thread"));

    return gemfireCache;
  }
}
