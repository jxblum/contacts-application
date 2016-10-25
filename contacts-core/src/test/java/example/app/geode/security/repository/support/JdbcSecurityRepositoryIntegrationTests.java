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

import static example.app.geode.security.model.Role.newRole;
import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.ResourcePermission.Operation;
import org.apache.geode.security.ResourcePermission.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.geode.security.model.Role;
import example.app.geode.security.model.User;

/**
 * Integration tests for {@link JdbcSecurityRepository}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.geode.security.model.Role
 * @see example.app.geode.security.model.User
 * @see example.app.geode.security.repository.support.JdbcSecurityRepository
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class JdbcSecurityRepositoryIntegrationTests {

  private static final ResourcePermission CLUSTER_MANAGE = new ResourcePermission(Resource.CLUSTER, Operation.MANAGE);
  private static final ResourcePermission CLUSTER_READ = new ResourcePermission(Resource.CLUSTER, Operation.READ);
  private static final ResourcePermission CLUSTER_WRITE = new ResourcePermission(Resource.CLUSTER, Operation.WRITE);
  private static final ResourcePermission DATA_MANAGE = new ResourcePermission(Resource.DATA, Operation.MANAGE);
  private static final ResourcePermission DATA_READ = new ResourcePermission(Resource.DATA, Operation.READ);
  private static final ResourcePermission DATA_WRITE = new ResourcePermission(Resource.DATA, Operation.WRITE);

  private static final Role ADMIN = newRole("ADMIN");
  private static final Role DATA_SCIENTIST = newRole("DATA_SCIENTIST");
  private static final Role DATA_ANALYST = newRole("DATA_ANALYST");
  private static final Role GUEST = newRole("GUEST");

  @Autowired
  private JdbcSecurityRepository securityRepository;

  protected User assertPermissions(User user, ResourcePermission... permissions) {
    for (ResourcePermission permission : permissions) {
      assertThat(user.hasPermission(permission)).isTrue();
    }

    return user;
  }

  protected User assertRoles(User user, Role... roles) {
    for (Role role : user) {
      assertThat(user.hasRole(role)).isTrue();
    }

    return user;
  }

  protected User assertUser(User user, String credentials) {
    assertThat((Object) user).isNotNull();
    assertThat(user.getCredentials()).isEqualTo(credentials);

    return user;
  }

  @Test
  public void usersRolesPermissionsAreConfiguredProperly() {
    assertPermissions(assertRoles(assertUser(securityRepository.findBy("root"), "s3cr3t!"), ADMIN, DATA_SCIENTIST),
      CLUSTER_MANAGE, CLUSTER_READ, CLUSTER_WRITE, DATA_MANAGE, DATA_READ, DATA_WRITE);

    assertPermissions(assertRoles(assertUser(securityRepository.findBy("scientist"), "w0rk!ng4u"), DATA_SCIENTIST),
      DATA_MANAGE, DATA_READ, DATA_WRITE);

    assertPermissions(assertRoles(assertUser(securityRepository.findBy("analyst"), "p@55w0rd"), DATA_ANALYST),
      DATA_READ, DATA_WRITE);

    assertPermissions(assertRoles(assertUser(securityRepository.findBy("guest"), "guest"), GUEST), DATA_READ);
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
