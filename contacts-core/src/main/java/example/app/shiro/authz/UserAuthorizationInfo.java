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

package example.app.shiro.authz;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.cp.elements.lang.Assert;

import example.app.geode.security.model.Role;
import example.app.geode.security.model.User;
import example.app.shiro.authz.support.AuthorizationInfoSupport;

/**
 * The {@link UserAuthorizationInfo} class is an implementation of the Apache Shiro {@link AuthorizationInfo} interface
 * backed by a {@link User} object.
 *
 * @author John Blum
 * @see org.apache.shiro.authz.AuthorizationInfo
 * @see example.app.geode.security.model.Role
 * @see example.app.geode.security.model.User
 * @see example.app.shiro.authz.support.AuthorizationInfoSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class UserAuthorizationInfo extends AuthorizationInfoSupport {

  private final User user;

  /**
   * Factory method used to construct an instance of the {@link UserAuthorizationInfo} class initialized with
   * the given {@link User}.
   *
   * @param user {@link User} backing the Apache Shiro {@link AuthorizationInfo}.
   * @return a new instance of the {@link UserAuthorizationInfo} class initialized with the given {@link User}.
   * @see example.app.shiro.authz.UserAuthorizationInfo
   * @see example.app.geode.security.model.User
   */
  public static UserAuthorizationInfo newAuthorizationInfo(User user) {
    return new UserAuthorizationInfo(user);
  }

  /**
   * Construct an instance of the {@link UserAuthorizationInfo} class initialized with the given {@link User},
   * which backs the details of the Apache Shiro {@link org.apache.shiro.authz.AuthorizationInfo}.
   *
   * @param user {@link User} object encapsulating the details of the Apache Shiro
   * {@link org.apache.shiro.authz.AuthorizationInfo}.
   * @throws IllegalArgumentException if {@link User} is null.
   * @see example.app.geode.security.model.User
   */
  public UserAuthorizationInfo(User user) {
    Assert.notNull(user, "User must not be null");
    this.user = user;
  }

  /**
   * Returns a reference to the {@link User} object backing the details of this Apache Shiro
   * {@link org.apache.shiro.authz.AuthorizationInfo}.
   *
   * @return a reference to the backing {@link User}.
   * @see example.app.geode.security.model.User
   */
  protected User getUser() {
    return this.user;
  }

  /**
   * @inheritDoc
   */
  @Override
  public Collection<String> getRoles() {
    Set<String> roles = new HashSet<>();

    for (Role role : getUser()) {
      roles.add(role.getName());
    }

    return roles;
  }

  /**
   * @inheritDoc
   */
  @Override
  public Collection<Permission> getObjectPermissions() {
    Set<Permission> permissions = new HashSet<>();

    for (Role role : getUser()) {
      for (Permission permission : role) {
        permissions.add(permission);
      }
    }

    return permissions;
  }
}
