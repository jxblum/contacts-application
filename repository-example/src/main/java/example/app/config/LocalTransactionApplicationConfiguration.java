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

package example.app.config;

import com.gemstone.gemfire.cache.Cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.GemfireTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring {@link Configuration} class used to configure local Apache Geode (or Pivotal GemFire) Cache transactions.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.GemfireTransactionManager
 * @see org.springframework.transaction.annotation.EnableTransactionManagement
 * @see org.springframework.transaction.PlatformTransactionManager
 * @see example.app.config.ApplicationConfiguration
 * @since 1.0.0
 */
@Configuration
@EnableTransactionManagement
@Import(ApplicationConfiguration.class)
@SuppressWarnings("unused")
public class LocalTransactionApplicationConfiguration {

	@Bean
	public PlatformTransactionManager transactionManager(Cache gemfireCache) {
		return new GemfireTransactionManager(gemfireCache);
	}
}
