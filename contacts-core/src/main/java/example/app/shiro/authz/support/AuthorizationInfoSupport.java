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

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.shiro.authz.AuthorizationInfo;

/**
 * The {@link AuthorizationInfoSupport} class is an abstract base class supporting the implementation of
 * the Apache Shiro {@link AuthorizationInfo} interface.
 *
 * @author John Blum
 * @see org.apache.shiro.authz.AuthorizationInfo
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AuthorizationInfoSupport implements AuthorizationInfo {

  /**
   * @inheritDoc
   */
  @Override
  public Collection<String> getStringPermissions() {
    return getObjectPermissions().stream().map(Object::toString).collect(Collectors.toList());
  }
}
