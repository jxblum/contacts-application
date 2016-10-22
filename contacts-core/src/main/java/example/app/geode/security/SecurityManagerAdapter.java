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

import java.security.Principal;
import java.util.Properties;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SecurityManagerAdapter} class is an Apache Geode {@link SecurityManager} interface adapter providing
 * default implementations of the {@link SecurityManager} interface operations.
 *
 * @author John Blum
 * @see java.security.Principal
 * @see org.apache.geode.security.ResourcePermission
 * @see org.apache.geode.security.SecurityManager
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class SecurityManagerAdapter implements org.apache.geode.security.SecurityManager {

  protected static final String SECURITY_PASSWORD_PROPERTY = "security-password";
  protected static final String SECURITY_USERNAME_PROPERTY = "security-username";

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /* (non-Javadoc)*/
  protected String getName(Object principal) {
    return (principal instanceof Principal ? ((Principal) principal).getName() : String.valueOf(principal));
  }

  /* (non-Javadoc)*/
  protected String getPassword(Properties securityProperties) {
    return getPropertyValue(securityProperties, SECURITY_PASSWORD_PROPERTY);
  }

  /* (non-Javadoc)*/
  protected String getUsername(Properties securityProperties) {
    return getPropertyValue(securityProperties, SECURITY_USERNAME_PROPERTY);
  }

  /* (non-Javadoc)*/
  protected String getPropertyValue(Properties properties, String propertyName) {
    return properties.getProperty(propertyName);
  }

  /* (non-Javadoc)*/
  protected void logDebug(String message, Object... args) {
    if (logger.isDebugEnabled()) {
      logger.debug(message, args);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public void init(Properties securityProperties) {
    if (logger.isDebugEnabled()) {
      logger.debug("Security Properties [{}]", securityProperties);
    }
  }

  /**
   * @inheritDoc
   */
  @Override
  public Object authenticate(Properties securityProperties) throws AuthenticationFailedException {
    return null;
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean authorize(Object principal, ResourcePermission permission) {
    return (principal != null);
  }

  /**
   * @inheritDoc
   */
  @Override
  public void close() {
    if (logger.isDebugEnabled()) {
      logger.debug("Closing SecurityManager [{}]", getClass().getName());
    }
  }
}
