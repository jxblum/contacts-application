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

package example.app.shiro.authc.pam;

import org.apache.shiro.authc.pam.AllSuccessfulStrategy;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.cp.elements.lang.Assert;

/**
 * The {@link ShiroAuthenticationStrategy} class is enumerated type that enumerates all the
 * Pluggable Authentication Modules (PAM) or strategies constituting when a Subject has been successfully identified.
 *
 * @author John Blum
 * @see org.apache.shiro.authc.pam.AllSuccessfulStrategy
 * @see org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy
 * @see org.apache.shiro.authc.pam.AuthenticationStrategy
 * @see org.apache.shiro.authc.pam.FirstSuccessfulStrategy
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public enum ShiroAuthenticationStrategy {
  ALL_SUCCESSFUL(AllSuccessfulStrategy.class, "allSuccessful"),
  AT_LEAST_ONE_SUCCESSFUL(AtLeastOneSuccessfulStrategy.class, "atLeastOneSuccessful"),
  FIRST_SUCCESSFUL(FirstSuccessfulStrategy.class, "firstSuccessful");

  private final Class<? extends AuthenticationStrategy> type;
  private final String name;

  /**
   * Constructs an instance of the {@link ShiroAuthenticationStrategy} enum initialized with the specified
   * and corresponding Apache Shiro {@link AuthenticationStrategy} type and name for this enumerated value.
   *
   * @param type Apache Shiro {@link AuthenticationStrategy} class type.
   * @param name name of this enumerated value.
   * @throws IllegalArgumentException if the {@link AuthenticationStrategy} type or name are not specified.
   * @see org.apache.shiro.authc.pam.AuthenticationStrategy
   */
  ShiroAuthenticationStrategy(Class<? extends AuthenticationStrategy> type, String name) {
    Assert.notNull(type, "AuthenticationStrategy class type must not be null");
    Assert.hasText(name, "Name must be specified");

    this.type = type;
    this.name = name;
  }

  /* (non-Javadoc) */
  public static ShiroAuthenticationStrategy findBy(Class<? extends AuthenticationStrategy> type) {
    for (ShiroAuthenticationStrategy authenticationStrategy : values()) {
      if (authenticationStrategy.getType().equals(type)) {
        return authenticationStrategy;
      }
    }

    return null;
  }

  /* (non-Javadoc) */
  public static ShiroAuthenticationStrategy findBy(String name) {
    for (ShiroAuthenticationStrategy authenticationStrategy : values()) {
      if (authenticationStrategy.getName().equalsIgnoreCase(name)) {
        return authenticationStrategy;
      }
    }

    return null;
  }

  /* (non-Javadoc) */
  public String getName() {
    return name;
  }

  /* (non-Javadoc) */
  public Class<? extends AuthenticationStrategy> getType() {
    return type;
  }


  /**
   * @inheritDoc
   */
  @Override
  public String toString() {
    return getName();
  }
}
