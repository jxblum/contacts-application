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

import org.junit.Test;

import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.ResourcePermission.Operation;
import org.apache.geode.security.ResourcePermission.Resource;

import example.app.geode.security.model.Role;
import example.app.geode.security.model.User;
import example.app.geode.security.repository.SecurityRepository;

/**
 * The {@link AbstractSecurityRepositoryIntegrationTests} class is an abstract base class encapsulating common
 * functionality for implementing integration test cases to test {@link SecurityRepository} implementations.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.security.ResourcePermission
 * @see example.app.geode.security.model.Role
 * @see example.app.geode.security.model.User
 * @see example.app.geode.security.repository.SecurityRepository
 * @since 1.0.0
 */
public abstract class AbstractSecurityRepositoryIntegrationTests {

  protected static final ResourcePermission CLUSTER_MANAGE = new ResourcePermission(Resource.CLUSTER, Operation.MANAGE);
  protected static final ResourcePermission CLUSTER_READ = new ResourcePermission(Resource.CLUSTER, Operation.READ);
  protected static final ResourcePermission CLUSTER_WRITE = new ResourcePermission(Resource.CLUSTER, Operation.WRITE);
  protected static final ResourcePermission DATA_MANAGE = new ResourcePermission(Resource.DATA, Operation.MANAGE);
  protected static final ResourcePermission DATA_READ = new ResourcePermission(Resource.DATA, Operation.READ);
  protected static final ResourcePermission DATA_WRITE = new ResourcePermission(Resource.DATA, Operation.WRITE);

  protected static final Role ADMIN = newRole("ADMIN");
  protected static final Role DBA = newRole("DBA");
  protected static final Role DATA_SCIENTIST = newRole("DATA_SCIENTIST");
  protected static final Role DATA_ANALYST = newRole("DATA_ANALYST");
  protected static final Role GUEST = newRole("GUEST");

  /* (non-Javadoc) */
  protected User assertPermissions(User user, ResourcePermission... permissions) {
    for (ResourcePermission permission : permissions) {
      assertThat(user.hasPermission(permission)).isTrue();
    }

    return user;
  }

  /* (non-Javadoc) */
  protected User assertRoles(User user, Role... roles) {
    for (Role role : roles) {
      assertThat(user.hasRole(role)).isTrue();
    }

    return user;
  }

  /* (non-Javadoc) */
  protected User assertUser(User user, String credentials) {
    assertThat((Object) user).isNotNull();
    assertThat(user.getCredentials()).isEqualTo(credentials);

    return user;
  }

  /* (non-Javadoc) */
  protected abstract <T extends SecurityRepository> T getSecurityRepository();

  @Test
  public void usersRolesPermissionsAreConfiguredProperly() {
    assertPermissions(assertRoles(assertUser(getSecurityRepository().findBy("root"), "s3cr3t!"), ADMIN, DBA),
      CLUSTER_MANAGE, CLUSTER_READ, CLUSTER_WRITE, DATA_MANAGE, DATA_READ, DATA_WRITE);

    assertPermissions(assertRoles(assertUser(getSecurityRepository().findBy("scientist"), "w0rk!ng4u"), DATA_SCIENTIST),
      DATA_READ, DATA_WRITE);

    assertPermissions(assertRoles(assertUser(getSecurityRepository().findBy("analyst"), "p@55w0rd"), DATA_ANALYST),
      DATA_READ);

    assertPermissions(assertRoles(assertUser(getSecurityRepository().findBy("guest"), "guest"), GUEST));
  }
}
