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

package example.app.geode.security.provider;

import java.util.Properties;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import example.app.geode.security.SecurityManagerSupport;
import example.app.geode.security.model.Role;
import example.app.geode.security.model.User;
import example.app.geode.security.repository.SecurityRepository;
import example.app.geode.security.repository.support.XmlSecurityRepository;

/**
 * The {@link SimpleSecurityManager} class is an example Apache Geode {@link SecurityManager} provider implementation
 * used to secure Apache Geode.
 *
 * @author John Blum
 * @see SecurityManagerSupport
 * @see example.app.geode.security.model.Role
 * @see example.app.geode.security.model.User
 * @see example.app.geode.security.repository.SecurityRepository
 * @see org.apache.geode.security.ResourcePermission
 * @see org.apache.geode.security.SecurityManager
 * @see org.springframework.stereotype.Service
 * @since 1.0.0
 */
@Service
@SuppressWarnings("unused")
public class SimpleSecurityManager extends SecurityManagerSupport {

  private final SecurityRepository<User> securityRepository;

  /**
   * Factory method used to construct and initialize a default instance of the {@link SecurityRepository}.
   * By default, Apache Geode Security is configured with XML security configuration meta-data.
   *
   * @return an instance of the {@link SecurityRepository} used as the default implementation when a
   * {@link SecurityRepository} is not explicitly configured.
   * @see example.app.geode.security.repository.support.XmlSecurityRepository
   * @see example.app.geode.security.repository.SecurityRepository
   */
  private static SecurityRepository<User> defaultSecurityRepository() {
    XmlSecurityRepository securityRepository = null;

    try {
      securityRepository = new XmlSecurityRepository();
      securityRepository.afterPropertiesSet();

      return securityRepository;
    }
    catch (Exception e) {
      throw new RuntimeException(String.format(
        "Failed to construct and initialize an instance of the SecurityRepository [%s]",
        ObjectUtils.getClassName(securityRepository)));
    }
  }

  /**
   * Default constructor constructing an instance of the {@link SimpleSecurityManager} initialized with an instance
   * of the {@link XmlSecurityRepository}, reading security configuration meta-data from a XML document/file.
   *
   * @see example.app.geode.security.repository.support.XmlSecurityRepository
   * @see #SimpleSecurityManager(SecurityRepository)
   * @see #defaultSecurityRepository()
   */
  public SimpleSecurityManager() {
    this(defaultSecurityRepository());
  }

  /**
   * Constructs an instance of the {@link SimpleSecurityManager} initialized with the given {@link SecurityRepository}
   * for accessing the data store containing security configuration meta-data used to secure Apache Geode.
   *
   * @param securityRepository {@link SecurityRepository} used to access the data store containing security
   * configuration meta-data used to secure Apache Geode.
   * @throws IllegalArgumentException if {@link SecurityRepository} is {@literal null}.
   * @see example.app.geode.security.repository.SecurityRepository
   */
  public SimpleSecurityManager(SecurityRepository<User> securityRepository) {
    Assert.notNull(securityRepository, "SecurityRepository must not be null");
    this.securityRepository = securityRepository;
  }

  /**
   * Returns a reference to the {@link SecurityRepository} used to access the data store containing
   * security configuration meta-data used to secure Apache Geode.
   *
   * @return a reference to the {@link SecurityRepository}.
   * @see example.app.geode.security.repository.SecurityRepository
   */
  protected SecurityRepository<User> getSecurityRepository() {
    return this.securityRepository;
  }

  /**
   * @inheritDoc
   */
  @Override
  public Object authenticate(Properties securityProperties) throws AuthenticationFailedException {
    String username = getUsername(securityProperties);
    String password = getPassword(securityProperties);

    logDebug("User with name [{}] is attempting to login with password [{}]", username, password);

    User user = getSecurityRepository().findBy(username);

    if (isNotAuthentic(user, password)) {
      throw new AuthenticationFailedException(String.format("Failed to authenticate user [%s]", username));
    }

    return user;
  }

  /* (non-Javadoc) */
  // NOTE User credentials or provide credentials could be encrypted so this SecurityManager would know how to
  // decrypt or still compare the credentials.
  protected boolean isAuthentic(String username, String credentials) {
    return isAuthentic(getSecurityRepository().findBy(username), credentials);
  }

  /* (non-Javadoc) */
  protected boolean isAuthentic(User user, String credentials) {
    return (user != null && ObjectUtils.equalsIgnoreNull(user.getCredentials(), credentials));
  }

  /* (non-Javadoc) */
  protected boolean isNotAuthentic(String username, String credentials) {
    return !isAuthentic(username, credentials);
  }

  /* (non-Javadoc) */
  protected boolean isNotAuthentic(User user, String credentials) {
    return !isAuthentic(user, credentials);
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean authorize(Object principal, ResourcePermission permission) {
    logDebug("Principal [{}] is requesting access to a Resource {} with the required Permission [{}]",
      principal, permission.getResource(), permission);

    return isAuthorized(principal, permission);
  }

  /* (non-Javadoc) */
  protected boolean isAuthorized(Object principal, ResourcePermission permission) {
    User user = resolveUser(principal);

    return (user != null && isAuthorized(user, permission));
  }

  /* (non-Javadoc) */
  protected User resolveUser(Object principal) {
    return (principal instanceof User ? (User) principal : getSecurityRepository().findBy(getName(principal)));
  }

  /* (non-Javadoc) */
  protected boolean isAuthorized(User user, ResourcePermission requiredPermission) {
    boolean permitted = user.hasPermission(requiredPermission);

    if (!permitted) {
      OUT: for (Role role : user) {
        for (ResourcePermission permission : role) {
          permitted = isPermitted(permission, requiredPermission);

          if (permitted) {
            break OUT;
          }
        }
      }
    }

    return permitted;
  }

  /* (non-Javadoc) */
  protected boolean isPermitted(ResourcePermission userPermission, ResourcePermission resourcePermission) {
    return userPermission.implies(resourcePermission);
  }

  /* (non-Javadoc) */
  String toPermissionDescriptor(ResourcePermission permission) {
    StringBuilder builder = new StringBuilder(permission.getResource().name());

    builder.append(":").append(permission.getOperation().name());

    if (isRequiredPermissionAttribute(permission.getRegionName())) {
      builder.append(":").append(permission.getRegionName());
    }

    if (isRequiredPermissionAttribute(permission.getKey())) {
      builder.append(":").append(permission.getKey());
    }

    return builder.toString();
  }

  /* (non-Javadoc) */
  boolean isRequiredPermissionAttribute(String value) {
    return (StringUtils.hasText(value) && !"*".equals(value.trim()));
  }
}
