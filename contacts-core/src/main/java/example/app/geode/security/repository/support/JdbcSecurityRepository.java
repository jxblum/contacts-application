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

import static example.app.core.util.ArrayUtils.toArray;
import static example.app.geode.security.model.Role.newRole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.geode.security.ResourcePermission;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import example.app.geode.security.model.Role;
import example.app.geode.security.model.User;
import example.app.geode.security.repository.CachingSecurityRepository;

/**
 * The {@link JdbcSecurityRepository} class is a {@link example.app.geode.security.repository.SecurityRepository}
 * implementation that accesses security configuration meta-data stored in an RDBMS, accessed via JDBC.
 *
 * @author John Blum
 * @see example.app.geode.security.model.Role
 * @see example.app.geode.security.model.User
 * @see example.app.geode.security.repository.CachingSecurityRepository
 * @see org.apache.geode.security.ResourcePermission
 * @see org.springframework.beans.factory.InitializingBean
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @since 1.0.0
 */
@Repository
@SuppressWarnings("unused")
public class JdbcSecurityRepository extends CachingSecurityRepository<User>
    implements InitializingBean {

  protected static final String ROLES_QUERY = "SELECT name FROM geode_security.roles";

  protected static final String ROLE_PERMISSIONS_QUERY = ""
    + " SELECT rolePerms.resource, rolePerms.operation, rolePerms.region_name, rolePerms.key_name"
    + " FROM geode_security.roles_permissions rolePerms"
    + " INNER JOIN geode_security.roles roles ON roles.id = rolePerms.role_id "
    + " WHERE roles.name = ?";

  protected static final String USERS_QUERY = "SELECT name, credentials FROM geode_security.users";

  protected static final String USER_ROLES_QUERY = ""
    + " SELECT roles.name"
    + " FROM geode_security.roles roles"
    + " INNER JOIN geode_security.users_roles userRoles ON roles.id = userRoles.role_id"
    + " INNER JOIN geode_security.users users ON userRoles.user_id = users.id"
    + " WHERE users.name = ?";

  private final JdbcTemplate jdbcTemplate;

  /**
   * Constructs an instance of the {@link JdbcSecurityRepository} initialized with the given {@link JdbcTemplate}.
   *
   * @param jdbcTemplate {@link JdbcTemplate} used to query the JDBC {@link javax.sql.DataSource}
   * containing security configuration meta-data used to secure Apache Geode.
   * @throws IllegalArgumentException if {@link JdbcTemplate} is {@literal null}.
   * @see org.springframework.jdbc.core.JdbcTemplate
   */
  public JdbcSecurityRepository(JdbcTemplate jdbcTemplate) {
    Assert.notNull(jdbcTemplate, "JdbcTemplate must not be null");
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Returns a reference to the {@link JdbcTemplate} configured with the JDBC {@link javax.sql.DataSource}
   * containing the security configuration meta-data to secure Apache Geode.
   *
   * @return a reference to the {@link JdbcTemplate}.
   * @see org.springframework.jdbc.core.JdbcTemplate
   */
  protected JdbcTemplate getJdbcTemplate() {
    return this.jdbcTemplate;
  }

  /**
   * @inheritDoc
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    List<Role> roles = getJdbcTemplate().query(ROLES_QUERY, (resultSet, row) -> newRole(resultSet.getString(1)));
    Map<String, Role> roleMapping = new HashMap<>(roles.size());

    for (Role role : roles) {
      getJdbcTemplate().query(ROLE_PERMISSIONS_QUERY, toArray(role.getName()),
        (resultSet, row) -> role.with(newResourcePermission(resultSet.getString(1), resultSet.getString(2),
          resultSet.getString(3), resultSet.getString(4))));

      roleMapping.put(role.getName(), role);
    }

    List<User> users = getJdbcTemplate().query(USERS_QUERY,
      (resultSet, row) -> create(resultSet.getString(1)).with(resultSet.getString(2)));

    for (User user : users) {
      getJdbcTemplate().query(USER_ROLES_QUERY, toArray(user.getName()),
        (resultSet, row) -> user.in(roleMapping.get(resultSet.getString(1))));

      save(user);
    }

    logger.debug("Users {}", users);
  }

  /* (non-Javadoc) */
  protected ResourcePermission newResourcePermission(String resource, String operation, String region, String key) {
    return new ResourcePermission(resource, operation, region, key);
  }
}
