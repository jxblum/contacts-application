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

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.EvictionAction;
import com.gemstone.gemfire.cache.EvictionAttributes;
import com.gemstone.gemfire.cache.ExpirationAction;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.EvictionAttributesFactoryBean;
import org.springframework.data.gemfire.EvictionPolicyType;
import org.springframework.data.gemfire.ExpirationAttributesFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;

/**
 * The ExampleApplicationConfiguration class is a Spring {@link Configuration @Configuration} class that configures
 * and initializes an example Apache Geode cache {@link Region}.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see com.gemstone.gemfire.cache.Cache
 * @see com.gemstone.gemfire.cache.EvictionAttributes
 * @see com.gemstone.gemfire.cache.ExpirationAttributes
 * @see com.gemstone.gemfire.cache.Region
 * @see com.gemstone.gemfire.cache.RegionAttributes
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class ExampleApplicationConfiguration {

	@Bean(name = "Example")
	PartitionedRegionFactoryBean<String, Object> exampleRegion(Cache gemfireCache,
			RegionAttributes<String, Object> exampleRegionAttributes) {

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
		exampleRegionAttributes.setInitialCapacity(101);
		exampleRegionAttributes.setKeyConstraint(String.class);
		exampleRegionAttributes.setLoadFactor(0.75f);
		exampleRegionAttributes.setOffHeap(false);
		exampleRegionAttributes.setStatisticsEnabled(true);
		exampleRegionAttributes.setValueConstraint(Object.class);

		return exampleRegionAttributes;
	}

	@Bean
	EvictionAttributesFactoryBean exampleEvictionAttributes() {
		EvictionAttributesFactoryBean exampleEvictionAttributes = new EvictionAttributesFactoryBean();

		exampleEvictionAttributes.setAction(EvictionAction.LOCAL_DESTROY);
		exampleEvictionAttributes.setThreshold(100);
		exampleEvictionAttributes.setType(EvictionPolicyType.ENTRY_COUNT);

		return exampleEvictionAttributes;
	}

	@Bean
	ExpirationAttributesFactoryBean exampleExpirationAttributes() {
		ExpirationAttributesFactoryBean exampleExpirationAttributes = new ExpirationAttributesFactoryBean();

		exampleExpirationAttributes.setAction(ExpirationAction.LOCAL_DESTROY);
		exampleExpirationAttributes.setTimeout(Long.valueOf(TimeUnit.MINUTES.toMillis(2)).intValue());

		return exampleExpirationAttributes;
	}

	@Resource(name = "Example")
	@SuppressWarnings("all")
	private Region<String, Object> example;

	@PostConstruct
	public void postConstruct() {
		System.err.printf("int[] class type is (%s)%n", int[].class);
		example.put("key1", new int[] { 0, 1, 2 });
	}
}
