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

import javax.annotation.PostConstruct;
import javax.transaction.TransactionManager;

import com.gemstone.gemfire.cache.GemFireCache;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import example.app.config.gemfire.GemFireDependsOnBeanFactoryPostProcessor;
import example.app.config.support.NamingContextBuilderFactoryBean;
import example.app.model.Contact;
import example.app.service.ContactsService;

/**
 * Spring {@link Configuration} class configuring Global, JTA-based Transactions using Apache Geode (or Pivotal GemFire)
 * with an external {@link javax.sql.DataSource}, such as a relational database (e.g. MySQL) and using JPA
 * for persistence.
 *
 * @author John Blum
 * @see javax.transaction.TransactionManager
 * @see org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * @see org.springframework.boot.autoconfigure.domain.EntityScan
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.jpa.repository.config.EnableJpaRepositories
 * @see org.springframework.transaction.annotation.EnableTransactionManagement
 * @see com.gemstone.gemfire.cache.GemFireCache#setCopyOnRead(boolean)
 * @see example.app.config.ApplicationConfiguration
 * @see example.app.config.gemfire.GemFireDependsOnBeanFactoryPostProcessor
 * @see example.app.config.support.NamingContextBuilderFactoryBean
 * @since 1.0.0
 */
@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackageClasses = example.app.repo.jpa.ContactRepository.class)
@EntityScan(basePackageClasses = Contact.class)
@EnableTransactionManagement
@Import(ApplicationConfiguration.class)
@SuppressWarnings("unused")
public class GlobalTransactionApplicationConfiguration {

	protected static final String USER_TRANSACTION_NAMING_CONTEXT_NAME = "java:comp/UserTransaction";

	@Bean
	public ContactsService contactsService(example.app.repo.gemfire.ContactRepository gemfireContactRepository,
			example.app.repo.jpa.ContactRepository jpaContactRepository) {

		return new ContactsService(gemfireContactRepository, jpaContactRepository);
	}

	@Bean
	public GemFireDependsOnBeanFactoryPostProcessor gemFireDependsOnBeanFactoryPostProcessor() {
		return new GemFireDependsOnBeanFactoryPostProcessor().add("NamingContextBuilder");
	}

	@Bean(name = "NamingContextBuilder")
	public NamingContextBuilderFactoryBean namingContextBuilder(TransactionManager transactionManager) {
		return new NamingContextBuilderFactoryBean().bind(USER_TRANSACTION_NAMING_CONTEXT_NAME, transactionManager);
	}

	@PostConstruct
	public void postProcess(GemFireCache gemFireCache) {
		gemFireCache.setCopyOnRead(true);
	}
}
