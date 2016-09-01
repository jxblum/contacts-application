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

package example.app.config.gemfire;

import java.util.Properties;

import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.PartitionAttributes;
import com.gemstone.gemfire.cache.RegionAttributes;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.IndexFactoryBean;
import org.springframework.data.gemfire.IndexType;
import org.springframework.data.gemfire.PartitionAttributesFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;

import example.app.RepositoryExampleApplication;
import example.app.model.Contact;
import example.app.model.Customer;

/**
 * Spring @{@link Configuration} class used to configure and bootstrap Apache Geode (or Pivotal GemFire)
 * as an embedded cache used as the application's system of record (SOR).
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see com.gemstone.gemfire.cache.GemFireCache
 * @see example.app.RepositoryExampleApplication
 * @see example.app.config.ApplicationConfiguration
 * @since 1.0.0
 */
@Configuration
@SuppressWarnings("unused")
public class GemFireConfiguration {

	protected static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";

	protected String applicationName() {
		return RepositoryExampleApplication.class.getSimpleName();
	}

	protected String logLevel() {
		return System.getProperty("gemfire.log-level", DEFAULT_GEMFIRE_LOG_LEVEL);
	}

	public Properties gemfireProperties() {
		Properties gemfireProperties = new Properties();

		gemfireProperties.setProperty("name", applicationName());
		gemfireProperties.setProperty("mcast-port", "0");
		gemfireProperties.setProperty("log-level", logLevel());

		return gemfireProperties;
	}

	@Bean
	public CacheFactoryBean gemfireCache() {
		CacheFactoryBean gemfireCache = new CacheFactoryBean();

		gemfireCache.setClose(true);
		gemfireCache.setProperties(gemfireProperties());

		return gemfireCache;
	}

	@Bean(name = "Contacts")
	//@DependsOn("Customers")
	public PartitionedRegionFactoryBean<Long, Contact> contactsRegion(GemFireCache gemfireCache,
			@Qualifier("contactsRegionAttributes") RegionAttributes<Long, Contact> contactsRegionAttributes) {

		PartitionedRegionFactoryBean<Long, Contact> contactsRegion = new PartitionedRegionFactoryBean<>();

		contactsRegion.setAttributes(contactsRegionAttributes);
		contactsRegion.setCache(gemfireCache);
		contactsRegion.setClose(false);
		contactsRegion.setPersistent(false);

		return contactsRegion;
	}

	@Bean
	@SuppressWarnings("unchecked")
	public RegionAttributesFactoryBean contactsRegionAttributes(
			@Qualifier("contactsRegionPartitionAttributes") PartitionAttributes contactsRegionPartitionAttributes) {

		RegionAttributesFactoryBean contactsRegionAttributes = new RegionAttributesFactoryBean();

		contactsRegionAttributes.setKeyConstraint(Long.class);
		contactsRegionAttributes.setValueConstraint(Contact.class);
		contactsRegionAttributes.setPartitionAttributes(contactsRegionPartitionAttributes);

		return contactsRegionAttributes;
	}

	@Bean
	public PartitionAttributesFactoryBean contactsRegionPartitionAttributes() {
		PartitionAttributesFactoryBean contactsRegionPartitionAttributes = new PartitionAttributesFactoryBean();

		contactsRegionPartitionAttributes.setColocatedWith("/Customers");
		contactsRegionPartitionAttributes.setRedundantCopies(1);

		return contactsRegionPartitionAttributes;
	}

	@Bean(name = "Customers")
	public PartitionedRegionFactoryBean<Long, Customer> customersRegion(GemFireCache gemfireCache,
			@Qualifier("customersRegionAttributes") RegionAttributes<Long, Customer> customersRegionAttributes) {

		PartitionedRegionFactoryBean<Long, Customer> customersRegion = new PartitionedRegionFactoryBean<>();

		customersRegion.setAttributes(customersRegionAttributes);
		customersRegion.setCache(gemfireCache);
		customersRegion.setClose(false);
		customersRegion.setPersistent(false);

		return customersRegion;
	}

	@Bean
	@SuppressWarnings("unchecked")
	public RegionAttributesFactoryBean customersRegionAttributes(
			PartitionAttributes<Long, Customer> customersRegionPartitionAttributes) {

		RegionAttributesFactoryBean customersRegionAttributes = new RegionAttributesFactoryBean();

		customersRegionAttributes.setKeyConstraint(Long.class);
		customersRegionAttributes.setValueConstraint(Customer.class);
		customersRegionAttributes.setPartitionAttributes(customersRegionPartitionAttributes);

		return customersRegionAttributes;
	}

	@Bean
	public PartitionAttributesFactoryBean customersRegionPartitionAttributes() {
		PartitionAttributesFactoryBean customersRegionPartitionAttributes = new PartitionAttributesFactoryBean();

		customersRegionPartitionAttributes.setRedundantCopies(1);

		return customersRegionPartitionAttributes;
	}

	@Bean
	@DependsOn("Contacts")
	public IndexFactoryBean emailIndex(GemFireCache gemfireCache) {
		IndexFactoryBean lastNameIndex = new IndexFactoryBean();

		lastNameIndex.setCache(gemfireCache);
		lastNameIndex.setExpression("email");
		lastNameIndex.setFrom("/Contacts");
		lastNameIndex.setName("EmailIdx");
		lastNameIndex.setType(IndexType.HASH);

		return lastNameIndex;
	}

	@Bean
	@DependsOn("Contacts")
	public IndexFactoryBean lastNameIndex(GemFireCache gemfireCache) {
		IndexFactoryBean lastNameIndex = new IndexFactoryBean();

		lastNameIndex.setCache(gemfireCache);
		lastNameIndex.setExpression("person.lastName");
		lastNameIndex.setFrom("/Contacts");
		lastNameIndex.setName("PersonLastNameIdx");
		lastNameIndex.setType(IndexType.HASH);

		return lastNameIndex;
	}
}
