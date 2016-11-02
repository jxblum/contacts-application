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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Properties;

import org.junit.Test;

import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.ResourcePermission.Operation;
import org.apache.geode.security.ResourcePermission.Resource;

/**
 * Unit tests for {@link SecurityManagerSupport}.
 *
 * @author John Blum
 * @see java.security.Principal
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.apache.geode.security.ResourcePermission
 * @see SecurityManagerSupport
 * @since 1.0.0
 */
public class SecurityManagerSupportTests {

  private SecurityManagerSupport securityManager = new SecurityManagerSupport() {};

  protected ResourcePermission newResourcePermission(Resource resource, Operation operation) {
    return newResourcePermission(resource, operation, null);
  }

  protected ResourcePermission newResourcePermission(Resource resource, Operation operation, String region) {
    return newResourcePermission(resource, operation, region, null);
  }

  protected ResourcePermission newResourcePermission(Resource resource, Operation operation,
    String region, String key) {
    return new ResourcePermission(resource, operation, region, key);
  }

  @Test
  public void getNameFromNull() {
    assertThat(securityManager.getName(null)).isEqualTo("null");
  }

  @Test
  public void getNameFromPrincipal() {
    Principal mockPrincipal = mock(Principal.class);

    when(mockPrincipal.getName()).thenReturn("test");

    assertThat(securityManager.getName(mockPrincipal)).isEqualTo("test");

    verify(mockPrincipal, times(1)).getName();
  }

  @Test
  public void getNameFromString() {
    assertThat(securityManager.getName("test")).isEqualTo("test");
  }

  @Test
  public void getPasswordIsSuccessful() {
    Properties securityProperties = new Properties();

    securityProperties.setProperty(Constants.SECURITY_PASSWORD_PROPERTY, "password");

    assertThat(securityManager.getPassword(securityProperties)).isEqualToIgnoringCase("password");
  }

  @Test
  public void getUsernameIsSuccessful() {
    Properties securityProperties = new Properties();

    securityProperties.setProperty(Constants.SECURITY_USERNAME_PROPERTY, "user");

    assertThat(securityManager.getUsername(securityProperties)).isEqualTo("user");
  }

  @Test
  public void getPropertyValueIsSuccessful() {
    Properties mockProperties = mock(Properties.class);

    when(mockProperties.getProperty(eq("key"))).thenReturn("test");

    assertThat(securityManager.getPropertyValue(mockProperties, "key")).isEqualTo("test");

    verify(mockProperties, times(1)).getProperty(eq("key"));
  }

  @Test
  public void authenticateReturnsNull() {
    assertThat(securityManager.authenticate(new Properties())).isNull();
  }

  @Test
  public void authorizeWithNonNullPrincipal() {
    assertThat(securityManager.authorize(mock(Principal.class),
      newResourcePermission(Resource.DATA, Operation.READ))).isTrue();
  }

  @Test
  public void authorizeWithNull() {
    assertThat(securityManager.authorize(null, null)).isFalse();
  }
}
