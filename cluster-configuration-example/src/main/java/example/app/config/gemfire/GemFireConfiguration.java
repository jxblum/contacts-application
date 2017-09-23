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
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication.Locator;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;

/**
 * The GemFireConfiguration class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@ClientCacheApplication(name = "ClusterConfigurationExampleGemFireClientApplication", locators = { @Locator })
@EnableClusterConfiguration
@SuppressWarnings("unused")
public class GemFireConfiguration {

  @Bean("ClientDefinedRegion")
  ClientRegionFactoryBean<String, String> clientDefinedRegion(GemFireCache gemfireCache) {

    ClientRegionFactoryBean<String, String> clientDefinedRegion = new ClientRegionFactoryBean<>();

    clientDefinedRegion.setCache(gemfireCache);
    clientDefinedRegion.setClose(false);
    clientDefinedRegion.setShortcut(ClientRegionShortcut.PROXY);

    return clientDefinedRegion;
  }
}
