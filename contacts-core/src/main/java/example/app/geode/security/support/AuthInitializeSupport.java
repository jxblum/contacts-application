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

package example.app.geode.security.support;

import java.util.Properties;

import org.apache.geode.LogWriter;
import org.apache.geode.distributed.DistributedMember;
import org.apache.geode.security.AuthInitialize;
import org.apache.geode.security.AuthenticationFailedException;

import example.app.geode.security.Constants;

/**
 * {@link AuthInitializeSupport} is an abstract class supporting the implementation
 * of the Apache Geode {@link AuthInitialize} interface.
 *
 * @author John Blum
 * @see org.apache.geode.security.AuthInitialize
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class AuthInitializeSupport implements AuthInitialize {

  /**
   * @inheritDoc
   */
  @Override
  public void init(LogWriter systemLogger, LogWriter securityLogger) throws AuthenticationFailedException {
  }

  /**
   * @inheritDoc
   */
  @Override
  @SuppressWarnings("deprecation")
  public Properties getCredentials(Properties securityProperties, DistributedMember server, boolean isPeer)
      throws AuthenticationFailedException {

    return getCredentials(securityProperties);
  }

  /**
   * @inheritDoc
   */
  @Override
  public Properties getCredentials(Properties securityProperties) {
    Properties credentials = new Properties();

    credentials.setProperty(Constants.SECURITY_USERNAME_PROPERTY,
      securityProperties.getProperty(Constants.SECURITY_USERNAME_PROPERTY));

    credentials.setProperty(Constants.SECURITY_PASSWORD_PROPERTY,
      securityProperties.getProperty(Constants.SECURITY_PASSWORD_PROPERTY));

    return credentials;
  }

  /**
   * @inheritDoc
   */
  @Override
  public void close() {
  }
}
