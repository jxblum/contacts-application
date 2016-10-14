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

package example.config.hazelcast;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ConcurrentMap;

import org.junit.Before;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * The {@link HazelcastApiExampleTests} class is an example demonstrating Hazelcast's DistributedMap operations.
 * This test is based on the example from Hazelcast's website.
 *
 * @author John J. Blum
 * @see com.hazelcast.core.Hazelcast
 * @see com.hazelcast.core.HazelcastInstance
 * @see <a href="https://hazelcast.org/">Hazelcast.org</a>
 * @since 1.0.0
 */
public class HazelcastApiExampleTests {

  private HazelcastInstance hazelcastInstance;

  @Before
  public void setup() {
    hazelcastInstance = Hazelcast.newHazelcastInstance();
  }

  public void tearDown() {
    if (hazelcastInstance != null) {
      hazelcastInstance.shutdown();
    }
  }

  @Test
  public void distributedMapOperations() {
    ConcurrentMap<String, String> map = hazelcastInstance.getMap("distributedMapExample");

    assertThat(map.put("key", "value")).isNull();
    assertThat(map.get("key")).isEqualTo("value");
    assertThat(map.putIfAbsent("someKey", "someValue")).isNull();
    assertThat(map.replace("key", "value", "newValue")).isTrue();
  }
}
