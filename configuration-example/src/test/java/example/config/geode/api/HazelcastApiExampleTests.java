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

package example.config.geode.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionShortcut;

/**
 * The {@link HazelcastApiExampleTests} class demonstrates how to implement the Hazelcast.org's
 * example applications using the Hazelcast API (e.g. {@literal DistributedMap}
 * with Apache Geode's API.
 *
 * @author John Blum
 * @see <a href="http://geode.incubator.apache.org/releases/latest/javadoc/index.html">Apache Geode API</a>
 * @see <a href="https://hazelcast.org/">Hazelcast API Examples</a>
 * @since 1.0.0
 */
public class HazelcastApiExampleTests {

  private Cache gemfireCache;

  private Region<String, String> myDistributedMap;

  @Before
  public void setup() {
    gemfireCache = new CacheFactory().create();
    myDistributedMap = gemfireCache.<String, String>createRegionFactory(RegionShortcut.LOCAL)
      .create("my-distributed-map");
  }

  @After
  public void tearDown() {
    if (gemfireCache != null) {
      gemfireCache.close();
    }
  }

  @Test
  public void distributeMapOperations() {
    assertThat(myDistributedMap.put("key", "value")).isNull();
    assertThat(myDistributedMap.get("key")).isEqualTo("value");
    assertThat(myDistributedMap.putIfAbsent("somekey", "somevalue")).isNull();
    assertThat(myDistributedMap.replace("key", "value", "newvalue")).isTrue();
  }
}
