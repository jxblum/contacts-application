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

package example.app.geode.security;

import java.util.Properties;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.util.PropertiesBuilder;

import example.app.geode.security.model.User;
import example.app.geode.security.repository.support.XmlSecurityRepository;
import example.app.geode.security.support.AuthInitializeSupport;

/**
 * The {@link GeodeClientAuthInitialize} class is an implementation of Apache Geode's
 * {@link org.apache.geode.security.AuthInitialize} interface supporting Geode (cache) client authentication
 * to a secure Geode cluster.
 *
 * This class is used to identify the {@link User} who will invoke the operations performed in the test cases
 * of the {@link GeodeSecurityIntegrationTests} class.
 *
 * @author John Blum
 * @see example.app.geode.security.model.User
 * @see example.app.geode.security.repository.support.XmlSecurityRepository
 * @see example.app.geode.security.support.AuthInitializeSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeodeClientAuthInitialize extends AuthInitializeSupport {

  private static XmlSecurityRepository securityRepository = null;

  static {
    try {
      securityRepository = new XmlSecurityRepository();
      securityRepository.afterPropertiesSet();
    }
    catch (Exception e) {
      throw new RuntimeException(String.format("Failed ot initialize the SecurityRepository [%s]",
        ObjectUtils.getClassName(securityRepository)));
    }
  }

  /**
   * Factory method to construct an instance of the {@link GeodeClientAuthInitialize} class initialized
   * with either the scientist or analyst.
   *
   * @return an instance of the {@link GeodeClientAuthInitialize} initialized with either the scientist or analyst.
   * @see #isScientist()
   */
  public static GeodeClientAuthInitialize create() {
    return new GeodeClientAuthInitialize(isScientist() ? securityRepository.findBy("scientist")
      : securityRepository.findBy("analyst"));
  }

  /* (non-Javadoc) */
  protected static boolean isAnalyst() {
    return !isScientist();
  }

  /* (non-Javadoc) */
  protected static boolean isScientist() {
    return (GeodeSecurityIntegrationTests.TEST_CASE_COUNT.incrementAndGet() < 2);
  }

  private final User user;

  /**
   * Constructs an instance of the {@link GeodeClientAuthInitialize} class initialized with the given {@link User}
   * who will perform secure actions (operations) on resources in Apache Geode.
   *
   * @param user identified {@link User} used to login from a Geode (cache) client.
   * @throws IllegalArgumentException if {@link User} is {@literal null}.
   * @see example.app.geode.security.model.User
   */
  public GeodeClientAuthInitialize(User user) {
    Assert.notNull(user, "User cannot be null");
    this.user = user;
  }

  /**
   * @inheritDoc
   */
  @Override
  public Properties getCredentials(Properties securityProperties) {
    User user = getUser();

    return new PropertiesBuilder()
      .set(Constants.SECURITY_USERNAME_PROPERTY, user.getName())
      .set(Constants.SECURITY_PASSWORD_PROPERTY, user.getCredentials())
      .build();
  }

  /**
   * Returns the {@link User} who will be identified during login from a Geode client to a Geode server.
   *
   * @return the identified {@link User} during login.
   * @see example.app.geode.security.model.User
   */
  protected User getUser() {
    return this.user;
  }

  /**
   * @inheritDoc
   */
  @Override
  public String toString() {
    User user = getUser();
    return String.format("%1$s:%2$s", user.getName(), user.getCredentials());
  }
}
