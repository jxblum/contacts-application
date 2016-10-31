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

package example.app.shiro.realm;

import static example.app.shiro.authc.UserAuthenticationInfo.newAuthenticationInfo;
import static example.app.shiro.authz.UserAuthorizationInfo.newAuthorizationInfo;
import static example.app.shiro.realm.support.CredentialsMatcher.newCredentialsMatcher;

import java.security.Principal;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.cp.elements.lang.Assert;

import example.app.geode.security.model.User;
import example.app.geode.security.repository.SecurityRepository;
import example.app.shiro.authz.support.ComposableAuthorizationInfo;

/**
 * The {@link SecurityRepositoryAuthorizingRealm} class is an Adapter and a Apache Shiro
 * {@link org.apache.shiro.realm.Realm} used to access security configuration meta-data
 * using a {@link SecurityRepository}.
 *
 * @author John Blum
 * @see org.apache.shiro.realm.AuthorizingRealm
 * @see example.app.geode.security.model.User
 * @see example.app.geode.security.repository.SecurityRepository
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SecurityRepositoryAuthorizingRealm<T extends User> extends AuthorizingRealm {

  private final SecurityRepository<T> securityRepository;

  /**
   * Constructs an instance of the {@link SecurityRepositoryAuthorizingRealm} initialized with a {@link SecurityRepository}
   * containing security configuration meta-data to enforce authentication/authorization in a Apache Shiro secured
   * application.
   *
   * @param securityRepository {@link SecurityRepository} used by this {@link org.apache.shiro.realm.Realm} to
   * access security configuration meta-data (authentication/authorization).
   * @throws IllegalArgumentException if {@link SecurityRepository} is null.
   * @see example.app.geode.security.repository.SecurityRepository
   */
  public SecurityRepositoryAuthorizingRealm(SecurityRepository<T> securityRepository) {
    Assert.notNull(securityRepository, "SecurityRepository must not be null");
    this.securityRepository = securityRepository;
  }

  /**
   * Returns a reference to the {@link SecurityRepository} used to access application security configuration meta-data
   * used in authentication and authorization security operations.
   *
   * @return a reference to the {@link SecurityRepository} used to access application security configuration meta-data.
   * @see example.app.geode.security.repository.SecurityRepository
   */
  protected SecurityRepository<T> getSecurityRepository() {
    return this.securityRepository;
  }

  /**
   * @inheritDoc
   */
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    return newAuthenticationInfo(authenticate(token), this);
  }

  /* (non-Javadoc) */
  protected User authenticate(AuthenticationToken token) {
    User user = resolveUser(token);

    if (isNotIdentified(user, token)) {
      throw new AuthenticationException(String.format("User [%s] could not be authenticated", user));
    }

    return user;
  }

  /* (non-Javadoc) */
  protected User resolveUser(AuthenticationToken token) {
    return resolveUser(token.getPrincipal());
  }

  /* (non-Javadoc) */
  protected boolean isRealmAuthenticationRequired() {
    return (getCredentialsMatcher() == null);
  }

  /* (non-Javadoc) */
  protected boolean isNotIdentified(User user, AuthenticationToken token) {
    return (isRealmAuthenticationRequired() && !isIdentified(user, token));
  }

  /* (non-Javadoc) */
  protected boolean isIdentified(User user, AuthenticationToken token) {
    return (user != null && newCredentialsMatcher().match(user.getCredentials(), token.getCredentials()));
    //return (user != null && user.getCredentials().equals(token.getCredentials()));
  }

  /**
   * @inheritDoc
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Object primaryPrincipal = principals.getPrimaryPrincipal();

    AuthorizationInfo current = compose(null, primaryPrincipal);

    for (Object principal : principals) {
      if (principal != null && principal != primaryPrincipal) {
        current = compose(current, principal);
      }
    }

    return current;
  }

  /* (non-Javadoc) */
  protected AuthorizationInfo compose(AuthorizationInfo current, Object principal) {
    return compose(current, resolveUser(principal));
  }

  /* (non-Javadoc) */
  protected AuthorizationInfo compose(AuthorizationInfo current, User user) {
    if (user != null) {
      current = ComposableAuthorizationInfo.compose(current, newAuthorizationInfo(user));
    }

    return current;
  }

  /* (non-Javadoc) */
  protected User resolveUser(Object principal) {
    return (principal instanceof User ? (User) principal : getSecurityRepository().findBy(resolveUsername(principal)));
  }

  /* (non-Javadoc) */
  protected String resolveUsername(Object principal) {
    return (principal instanceof User ? ((User) principal).getName()
      : (principal instanceof Principal ? ((Principal) principal).getName()
      : String.valueOf(principal)));
  }
}
