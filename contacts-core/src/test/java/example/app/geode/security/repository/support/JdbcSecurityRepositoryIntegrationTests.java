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

package example.app.geode.security.repository.support;

import javax.sql.DataSource;

import org.junit.runner.RunWith;

import org.apache.shiro.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.geode.security.repository.SecurityRepository;

/**
 * Integration tests for {@link JdbcSecurityRepository}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.geode.security.repository.SecurityRepository
 * @see example.app.geode.security.repository.support.AbstractSecurityRepositoryIntegrationTests
 * @see example.app.geode.security.repository.support.JdbcSecurityRepository
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class JdbcSecurityRepositoryIntegrationTests extends AbstractSecurityRepositoryIntegrationTests {

  @Autowired
  private JdbcSecurityRepository securityRepository;

  /**
   * @inheritDoc
   */
  @Override
  @SuppressWarnings("unchecked")
  protected <T extends SecurityRepository> T getSecurityRepository() {
    Assert.state(this.securityRepository != null, "JdbcSecurityRepository was not properly configured");
    return (T) this.securityRepository;
  }

  @Configuration
  static class JdbcSecurityRepositoryConfiguration {

    @Bean
    DataSource hsqlDataSource() {
      return new EmbeddedDatabaseBuilder()
        .setName("geode_security")
        .setScriptEncoding("UTF-8")
        .setType(EmbeddedDatabaseType.HSQL)
        .addScript("geode-security-schema-ddl.sql")
        .addScript("define-roles-table-ddl.sql")
        .addScript("define-roles-permissions-table-ddl.sql")
        .addScript("define-users-table-ddl.sql")
        .addScript("define-users-roles-table-ddl.sql")
        .addScript("insert-roles-dml.sql")
        .addScript("insert-roles-permissions-dml.sql")
        .addScript("insert-users-dml.sql")
        .addScript("insert-users-roles-dml.sql")
        .build();
    }

    @Bean
    JdbcTemplate hsqlTemplate() {
      return new JdbcTemplate(hsqlDataSource());
    }

    @Bean
    JdbcSecurityRepository securityRepository() {
      return new JdbcSecurityRepository(hsqlTemplate());
    }
  }
}
