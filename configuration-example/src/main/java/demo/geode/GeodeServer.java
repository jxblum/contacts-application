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

package demo.geode;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Properties;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.server.CacheServer;

import example.app.geode.cache.loader.EchoCacheLoader;

/**
 * The {@link GeodeServer} class is an example Apache Geode application bootstrapping a Geode Server,
 * peer cache instance, distributed system (cluster) member using Geode's API.
 *
 * This example sets up a simple {@literal PARTITION} {@link Region} having a {@link org.apache.geode.cache.CacheLoader}
 * that echos the key as the value for the cache {@link Region} entry.
 *
 * It is also possible to connect to and interact with this Geode Server using Gfsh since this application
 * has both an embedded Locator and Manager service.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.server.CacheServer
 * @since 1.0.0
 */
public class GeodeServer {

  public static void main(String[] args) throws Exception {

    Properties geodeProperties = new Properties();

    geodeProperties.setProperty("name", GeodeServer.class.getSimpleName());
    geodeProperties.setProperty("log-level", "config");
    geodeProperties.setProperty("jmx-manager", "true");
    geodeProperties.setProperty("jmx-manager-start", "true");
    geodeProperties.setProperty("start-locator", "localhost[10334]");

    Cache geodeCache = new CacheFactory(geodeProperties).create();

    CacheServer cacheServer = geodeCache.addCacheServer();

    cacheServer.setPort(CacheServer.DEFAULT_PORT);
    cacheServer.start();

    RegionFactory<String, String> echoRegionFactory =
      geodeCache.createRegionFactory(RegionShortcut.PARTITION);

    echoRegionFactory.setCacheLoader(EchoCacheLoader.getInstance());

    Region<String, String> echoRegion = echoRegionFactory.create("Echo");

    assertThat(echoRegion).isNotNull();
    assertThat(echoRegion).isEmpty();
  }
}
