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

package example.app.config.gemfire;

import org.apache.geode.cache.GemFireCache;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;

/**
 * The GemFireConfiguration class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@PeerCacheApplication(name = "SimpleCachingExampleApplication")
@SuppressWarnings("unused")
public class GemFireConfiguration {

  @Bean("Factorials")
  public LocalRegionFactoryBean<Long, Long> factorialsRegion(GemFireCache gemfireCache) {

    LocalRegionFactoryBean<Long, Long> factorials = new LocalRegionFactoryBean<>();

    factorials.setCache(gemfireCache);
    factorials.setClose(false);
    factorials.setPersistent(false);

    return factorials;
  }
}
