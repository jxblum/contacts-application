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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.EvictionAction;
import org.apache.geode.cache.EvictionAttributes;
import org.apache.geode.cache.ExpirationAction;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.eviction.EvictionAttributesFactoryBean;
import org.springframework.data.gemfire.eviction.EvictionPolicyType;
import org.springframework.data.gemfire.expiration.ExpirationAttributesFactoryBean;

/**
 * The {@link ExampleApplicationConfiguration} class is a Spring {@link Configuration @Configuration} class
 * that configures and initializes an example Apache Geode cache {@link Region}.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.EvictionAttributes
 * @see org.apache.geode.cache.ExpirationAttributes
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.RegionAttributes
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class ExampleApplicationConfiguration {

	@Bean
	static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean(name = "Example")
	PartitionedRegionFactoryBean<String, Object> exampleRegion(Cache gemfireCache,
			@Qualifier("exampleRegionAttributes") RegionAttributes<String, Object> exampleRegionAttributes) {

		PartitionedRegionFactoryBean<String, Object> exampleRegion = new PartitionedRegionFactoryBean<>();

		exampleRegion.setCache(gemfireCache);
		exampleRegion.setClose(false);
		exampleRegion.setPersistent(false);

		return exampleRegion;
	}

	@Bean
	@SuppressWarnings("unchecked")
	RegionAttributesFactoryBean exampleRegionAttributes(EvictionAttributes exampleEvictionAttributes,
			ExpirationAttributes exampleExpirationAttributes) {

		RegionAttributesFactoryBean exampleRegionAttributes = new RegionAttributesFactoryBean();

		exampleRegionAttributes.setCloningEnabled(false);
		exampleRegionAttributes.setEvictionAttributes(exampleEvictionAttributes);
		exampleRegionAttributes.setEntryIdleTimeout(exampleExpirationAttributes);
		exampleRegionAttributes.setEntryTimeToLive(exampleExpirationAttributes);
		exampleRegionAttributes.setInitialCapacity(101);
		exampleRegionAttributes.setKeyConstraint(String.class);
		exampleRegionAttributes.setLoadFactor(0.75f);
		exampleRegionAttributes.setOffHeap(false);
		exampleRegionAttributes.setStatisticsEnabled(true);
		exampleRegionAttributes.setValueConstraint(Object.class);

		return exampleRegionAttributes;
	}

	@Bean
	EvictionAttributesFactoryBean exampleEvictionAttributes(
			@Value("${gemfire.cache.eviction.threshold:100}") int threshold) {

		EvictionAttributesFactoryBean exampleEvictionAttributes = new EvictionAttributesFactoryBean();

		exampleEvictionAttributes.setAction(EvictionAction.LOCAL_DESTROY);
		exampleEvictionAttributes.setThreshold(threshold);
		exampleEvictionAttributes.setType(EvictionPolicyType.ENTRY_COUNT);

		return exampleEvictionAttributes;
	}

	@Bean
	ExpirationAttributesFactoryBean exampleExpirationAttributes(
			@Value("${gemfire.cache.expiration.timeout:120}") int timeout) {

		ExpirationAttributesFactoryBean exampleExpirationAttributes = new ExpirationAttributesFactoryBean();

		exampleExpirationAttributes.setAction(ExpirationAction.LOCAL_DESTROY);
		exampleExpirationAttributes.setTimeout(timeout);

		return exampleExpirationAttributes;
	}

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource(name = "Example")
	@SuppressWarnings("all")
	private Region<String, Object> example;

	@PostConstruct
	public void postConstruct() {
		logger.info("int[] class type is {}", int[].class);
		example.put("key1", new int[] { 0, 1, 2 });
	}
}
