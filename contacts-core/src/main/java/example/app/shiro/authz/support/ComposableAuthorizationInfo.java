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

package example.app.shiro.authz.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;

/**
 * The {@link ComposableAuthorizationInfo} class is an Apache Shiro {@link AuthorizationInfo} implementation
 * that composes multiple Apache Shiro {@link AuthorizationInfo} objects into a composition acting as a
 * single {@link AuthorizationInfo}.
 *
 * @author John Blum
 * @see org.apache.shiro.authz.AuthorizationInfo
 * @see example.app.shiro.authz.support.AuthorizationInfoSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ComposableAuthorizationInfo extends AuthorizationInfoSupport {

  /**
   * Composes an array of {@link AuthorizationInfo} objects into a composition acting as
   * a single {@link AuthorizationInfo}.
   *
   * @param array array of {@link AuthorizationInfo} objects to compose.
   * @return an instance of {@link ComposableAuthorizationInfo} composed of the array
   * of {@link AuthorizationInfo} objects.
   * @throws NullPointerException if the array is {@literal null}.
   * @see org.apache.shiro.authz.AuthorizationInfo
   * @see #compose(Iterable)
   */
  public static AuthorizationInfo compose(AuthorizationInfo... array) {
    return compose(Arrays.asList(array));
  }

  /**
   * Composes an {@link Iterable} of {@link AuthorizationInfo} objects into a composition acting as
   * a single {@link AuthorizationInfo}.
   *
   * @param iterable {@link Iterable} of {@link AuthorizationInfo} objects to compose.
   * @return an instance of {@link ComposableAuthorizationInfo} composed of the {@link Iterable}
   * of {@link AuthorizationInfo} objects.
   * @throws NullPointerException if the {@link Iterable} is {@literal null}.
   * @see org.apache.shiro.authz.AuthorizationInfo
   * @see #compose(Iterable)
   */
  public static AuthorizationInfo compose(Iterable<AuthorizationInfo> iterable) {
    AuthorizationInfo current = null;

    for (AuthorizationInfo authorizationInfo : iterable) {
      current = compose(current, authorizationInfo);
    }

    return current;
  }

  /**
   * Composes two Apache Shiro {@link AuthorizationInfo} objects into a composition, acting as a single instance
   * of the Apache Shiro {@link AuthorizationInfo} interface.
   *
   * @param one first {@link AuthorizationInfo} in the composition.
   * @param two second {@link AuthorizationInfo} in the composition.
   * @return {@code two} if {@code one} is {@literal null}, {@code one} if {@code two} is {@literal null}
   * or an instance of {@link ComposableAuthorizationInfo} if neither {@code one} or {@code two} is {@literal null}.
   * @see example.app.shiro.authz.support.ComposableAuthorizationInfo
   * @see org.apache.shiro.authz.AuthorizationInfo
   */
  public static AuthorizationInfo compose(AuthorizationInfo one, AuthorizationInfo two) {
    return (one == null ? two : (two == null ? one : new ComposableAuthorizationInfo(one, two)));
  }

  private final AuthorizationInfo one;
  private final AuthorizationInfo two;

  /**
   * Constructs an instance of the {@link ComposableAuthorizationInfo} class compose of 2 given Apache Shiro
   * {@link AuthorizationInfo} objects.
   *
   * @param one first {@link AuthorizationInfo} in the composition.
   * @param two second {@link AuthorizationInfo} in the composition.
   * @see org.apache.shiro.authz.AuthorizationInfo
   */
  private ComposableAuthorizationInfo(AuthorizationInfo one, AuthorizationInfo two) {
    this.one = one;
    this.two = two;
  }

  /**
   * @inheritDoc
   */
  @Override
  public Collection<String> getRoles() {
    Collection<String> roles = new HashSet<>(this.one.getRoles());
    roles.addAll(this.two.getRoles());
    return roles;
  }

  /**
   * @inheritDoc
   */
  @Override
  public Collection<Permission> getObjectPermissions() {
    Collection<Permission> permissions = new HashSet<>(this.one.getObjectPermissions());
    permissions.addAll(this.two.getObjectPermissions());
    return permissions;
  }
}
