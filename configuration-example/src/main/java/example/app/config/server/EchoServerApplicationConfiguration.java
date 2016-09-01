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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app.config.server;

import com.gemstone.gemfire.cache.Cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;

import example.app.geode.cache.loader.EchoCacheLoader;

/**
 * The EchoServerApplicationConfiguration class is a Spring {@link Configuration @Configuration} class that configures
 * a Geode, Server-side, PARTITION Region for storing echo messages.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.PartitionedRegionFactoryBean
 * @see example.app.geode.cache.loader.EchoCacheLoader
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class EchoServerApplicationConfiguration {

	@Bean(name = "Echo")
	PartitionedRegionFactoryBean<String, String> echoRegion(Cache gemfireCache) {
		PartitionedRegionFactoryBean<String, String> echoRegion = new PartitionedRegionFactoryBean<>();

		echoRegion.setCache(gemfireCache);
		echoRegion.setCacheLoader(EchoCacheLoader.getInstance());
		echoRegion.setClose(false);
		echoRegion.setPersistent(false);

		return echoRegion;
	}
}
