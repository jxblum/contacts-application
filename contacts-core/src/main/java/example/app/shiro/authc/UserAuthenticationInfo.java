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

package example.app.shiro.authc;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.cp.elements.lang.Assert;

import example.app.geode.security.model.User;

/**
 * The UserAuthenticationInfo class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class UserAuthenticationInfo implements AuthenticationInfo {

  private final Realm realm;

  private final User user;

  /**
   * Factory method used to construct a new instance of the {@link UserAuthenticationInfo} class initialized with
   * the given {@link User}.
   *
   * @param user {@link User} backing this {@link AuthenticationInfo} implementation
   * @param realm {@link Realm} from which the {@link User} data was loaded.
   * @return an instance of {@link UserAuthenticationInfo} initialized with the given {@link User} and {@link Realm}.
   * @throws IllegalArgumentException if {@link User} is {@literal null}.
   * @see example.app.geode.security.model.User
   * @see org.apache.shiro.realm.Realm
   */
  public static UserAuthenticationInfo newAuthenticationInfo(User user, Realm realm) {
    return new UserAuthenticationInfo(user, realm);
  }

  /**
   * Constructs an instance of {@link UserAuthenticationInfo} initialized with the given {@link User}
   * and {@link Realm} from which the {@link User} data was loaded.
   *
   * @param user {@link User} backing this {@link AuthenticationInfo} implementation
   * @param realm {@link Realm} from which the {@link User} data was loaded.
   * @throws IllegalArgumentException if {@link User} is {@literal null}.
   * @see example.app.geode.security.model.User
   * @see org.apache.shiro.realm.Realm
   */
  public UserAuthenticationInfo(User user, Realm realm) {
    Assert.notNull(user, "User must not be null");
    Assert.notNull(realm, "Realm must not be null");

    this.user = user;
    this.realm = realm;
  }

  /**
   * Returns a reference to the {@link Realm} from which the {@link User} data was loaded.
   *
   * @return a reference to the {@link User} {@link Realm}.
   * @see org.apache.shiro.realm.Realm
   */
  protected Realm getRealm() {
    return this.realm;
  }

  /**
   * Returns a reference to the {@link User} backing this {@link org.apache.shiro.authc.AuthenticationInfo}.
   *
   * @return a reference to the underlying {@link User}.
   * @see example.app.geode.security.model.User
   */
  protected User getUser() {
    return this.user;
  }

  /**
   * @inheritDoc
   */
  @Override
  public Object getCredentials() {
    return getUser().getCredentials();
  }

  /**
   * @inheritDoc
   */
  @Override
  public PrincipalCollection getPrincipals() {
    return new SimplePrincipalCollection(getUser(), getRealm().getName());
  }
}
