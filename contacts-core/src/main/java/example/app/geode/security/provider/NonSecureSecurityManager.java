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

import static example.app.geode.security.model.User.newUser;

import java.util.Properties;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;

import example.app.geode.security.SecurityManagerSupport;
import example.app.geode.security.model.User;

/**
 * The {@link NonSecureSecurityManager} class is an Apache Geode {@link org.apache.geode.security.SecurityManager}
 * implementation with wide open security allowing anyone to perform any action, effectively not securing
 * Apache Geode at all.
 *
 * @author John Blum
 * @see example.app.geode.security.SecurityManagerSupport
 * @see example.app.geode.security.model.User
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class NonSecureSecurityManager extends SecurityManagerSupport {

  private static final User SUPER_USER = newUser("superuser");

  /**
   * @inheritDoc
   */
  @Override
  public Object authenticate(Properties securityProperties) throws AuthenticationFailedException {
    return SUPER_USER;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean authorize(Object principal, ResourcePermission permission) {
    logger.debug("Principal {} requires Permission {} on Resource {}", principal, permission, permission.getResource());
    return super.authorize(principal, permission);
  }
}
