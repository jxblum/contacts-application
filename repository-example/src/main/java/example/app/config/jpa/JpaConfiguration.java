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

package example.app.config.jpa;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import example.app.model.Contact;

/**
 * Spring {@link Configuration} class used to configure and bootstrap a {@link DataSource} along with configuring
 * a JPA {@link javax.persistence.EntityManager} to persist entities to a relational data source.
 *
 * @author John Blum
 * @see javax.persistence.EntityManagerFactory
 * @see javax.sql.DataSource
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.jpa.repository.config.EnableJpaRepositories
 * @see org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
 * @see org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType#HSQL
 * @see org.springframework.orm.jpa.JpaTransactionManager
 * @see org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
 * @see org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
 * @see org.springframework.transaction.PlatformTransactionManager
 * @see org.springframework.transaction.annotation.EnableTransactionManagement
 * @since 1.0.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "example.app.repo.jpa")
@EnableTransactionManagement
@SuppressWarnings("unused")
public class JpaConfiguration {

	@Bean
	public DataSource hsqlDataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).build();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();

		jpaVendorAdapter.setGenerateDdl(true);
		jpaVendorAdapter.setShowSql(true);

		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();

		entityManagerFactory.setDataSource(dataSource);
		entityManagerFactory.setJpaVendorAdapter(jpaVendorAdapter);
		entityManagerFactory.setPackagesToScan(Contact.class.getPackage().getName());

		return entityManagerFactory;
	}

	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}
}
