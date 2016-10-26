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

import static example.app.geode.security.model.Role.newRole;
import static example.app.geode.security.model.User.newUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.geode.security.ResourcePermission.Operation;
import org.apache.geode.security.ResourcePermission.Resource;

import example.app.geode.security.model.User;
import example.app.geode.security.repository.SecurityRepository;
import example.app.geode.security.repository.support.XmlSecurityRepository;

/**
 * Unit tests for {@link SimpleSecurityManager}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see example.app.geode.security.provider.SimpleSecurityManager
 * @see example.app.geode.security.repository.SecurityRepository
 * @see example.app.geode.security.repository.support.XmlSecurityRepository
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleSecurityManagerTests {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  private SimpleSecurityManager securityManager;

  @Mock
  private SecurityRepository<User> mockSecurityRepository;

  @Before
  public void setup() {
    securityManager = new SimpleSecurityManager(mockSecurityRepository);
  }

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
  public void constructSimpleSecurityManagerWithDefaultSecurityRepository() {
    SimpleSecurityManager securityManager = new SimpleSecurityManager();

    assertThat(securityManager.getSecurityRepository()).isInstanceOf(XmlSecurityRepository.class);
  }

  @Test
  public void constructSimpleSecurityManagerWithSecurityRepository() {
    SimpleSecurityManager securityManager = new SimpleSecurityManager(mockSecurityRepository);

    assertThat(securityManager.getSecurityRepository()).isSameAs(mockSecurityRepository);
  }

  @Test
  public void constructSimpleSecurityManagerWithNull() {
    exception.expect(IllegalArgumentException.class);
    exception.expectCause(is(nullValue(Throwable.class)));
    exception.expectMessage("SecurityRepository must not be null");

    new SimpleSecurityManager(null);
  }

  @Test
  public void authenticateReturnsPrinciple() {
    User jonDoe = newUser("jonDoe").with("p@s5w0rd");

    Properties securityProperties = new Properties();

    securityProperties.setProperty(SimpleSecurityManager.SECURITY_USERNAME_PROPERTY, jonDoe.getName());
    securityProperties.setProperty(SimpleSecurityManager.SECURITY_PASSWORD_PROPERTY, jonDoe.getCredentials());

    when(mockSecurityRepository.findBy(eq("jonDoe"))).thenReturn(jonDoe);

    Object principle = securityManager.authenticate(securityProperties);

    assertThat(principle).isSameAs(jonDoe);

    verify(mockSecurityRepository, times(1)).findBy(eq("jonDoe"));
  }

  @Test
  public void authenticateThrowsAuthenticationFailedException() {
    Properties securityProperties = new Properties();

    securityProperties.setProperty(SimpleSecurityManager.SECURITY_USERNAME_PROPERTY, "hacker");
    securityProperties.setProperty(SimpleSecurityManager.SECURITY_PASSWORD_PROPERTY, "letMeIn!");

    when(mockSecurityRepository.findBy(anyString())).thenReturn(null);

    try {
      exception.expect(AuthenticationFailedException.class);
      exception.expectCause(is(nullValue(Throwable.class)));
      exception.expectMessage("Failed to authenticate user [hacker]");

      securityManager.authenticate(securityProperties);
    }
    finally {
      verify(mockSecurityRepository, times(1)).findBy(eq("hacker"));
    }
  }

  @Test
  public void isAuthenticWithUsernameIsTrue() {
    SimpleSecurityManager securityManagerSpy = spy(securityManager);
    User root = newUser("root").with("password");

    when(mockSecurityRepository.findBy(eq("root"))).thenReturn(root);

    assertThat(securityManagerSpy.isAuthentic("root", "password")).isTrue();

    verify(securityManagerSpy, times(1)).isAuthentic(eq(root), eq("password"));
    verify(mockSecurityRepository, times(1)).findBy(eq("root"));
  }

  @Test
  public void isAuthenticWithNullUserIsFalse() {
    assertThat(securityManager.isAuthentic((User) null, "password")).isFalse();
  }

  @Test
  public void isAuthenticWithNoPasswordIsTrue() {
    assertThat(securityManager.isAuthentic(newUser("guest"), null)).isTrue();
  }

  @Test
  public void isAuthenticWithIncorrectPasswordIsFalse() {
    assertThat(securityManager.isAuthentic(newUser("admin").with("password"), "s3cr3t!")).isFalse();
  }

  @Test
  public void authorizeUserReturnsTrue() {
    ResourcePermission requiredPermission = newResourcePermission(Resource.CLUSTER, Operation.MANAGE);
    User root = newUser("root").with("password").in(newRole("ADMIN").with(requiredPermission));

    assertThat(securityManager.authorize(root, requiredPermission)).isTrue();
  }

  @Test
  public void authorizePrincipleReturnsTrue() {
    User analyst = newUser("analyst").with("password").in(
      newRole("DATA_ANALYST").with(newResourcePermission(Resource.DATA, Operation.MANAGE, "Example")));

    ResourcePermission requiredPermission = newResourcePermission(Resource.DATA, Operation.MANAGE,
      "Example", "Key[1-99]");

    Principal mockPrinciple = mock(Principal.class);

    when(mockPrinciple.getName()).thenReturn("analyst");
    when(mockSecurityRepository.findBy(eq("analyst"))).thenReturn(analyst);

    assertThat(securityManager.authorize(mockPrinciple, requiredPermission)).isTrue();

    verify(mockPrinciple, times(1)).getName();
    verify(mockSecurityRepository, times(1)).findBy(eq("analyst"));
  }

  @Test
  public void authorizeByUsernameReturnsTrue() {
    User scientist = newUser("scientist").with("password").in(
      newRole("ANALYST").with(newResourcePermission(Resource.DATA, Operation.READ)),
      newRole("SCIENTIST").with(newResourcePermission(Resource.DATA, Operation.WRITE, "Example")));

    ResourcePermission requiredPermission = newResourcePermission(Resource.DATA, Operation.WRITE, "Example", "Key69");

    when(mockSecurityRepository.findBy(eq("scientist"))).thenReturn(scientist);

    assertThat(securityManager.authorize("scientist", requiredPermission)).isTrue();

    verify(mockSecurityRepository, times(1)).findBy(eq("scientist"));
  }

  @Test
  public void isAuthorizedWhenResolvedUserIsNull() {
    when(mockSecurityRepository.findBy(anyString())).thenReturn(null);

    assertThat(securityManager.isAuthorized("test", newResourcePermission(Resource.DATA, Operation.READ))).isFalse();

    verify(mockSecurityRepository, times(1)).findBy(eq("test"));
  }

  @Test
  public void resolveUserResolvesToUser() {
    User root = newUser("root");

    assertThat((Object) securityManager.resolveUser(root)).isSameAs(root);

    verify(mockSecurityRepository, never()).findBy(anyString());
  }

  @Test
  public void resolveUserResolvesToSecurityRepositoryFindByCall() {
    User jonDoe = newUser("jonDoe");
    Principal mockPrincipal = mock(Principal.class);

    when(mockPrincipal.getName()).thenReturn(jonDoe.getName());
    when(mockSecurityRepository.findBy(eq("jonDoe"))).thenReturn(jonDoe);

    assertThat((Object) securityManager.resolveUser(mockPrincipal)).isSameAs(jonDoe);

    verify(mockPrincipal, times(1)).getName();
    verify(mockSecurityRepository, times(1)).findBy(eq(jonDoe.getName()));
  }

  @Test
  public void isPermittedIsTrue() {
    ResourcePermission clusterManage = newResourcePermission(Resource.CLUSTER, Operation.MANAGE);
    ResourcePermission dataRead = newResourcePermission(Resource.DATA, Operation.READ, "Example");

    assertThat(securityManager.isPermitted(clusterManage, clusterManage)).isTrue();
    assertThat(securityManager.isPermitted(dataRead, dataRead)).isTrue();
    assertThat(securityManager.isPermitted(dataRead,
      newResourcePermission(Resource.DATA, Operation.READ, "Example", "KeyOne"))).isTrue();
  }

  @Test
  public void isPermittedIsFalse() {
    ResourcePermission dataRead = newResourcePermission(Resource.DATA, Operation.READ, "Example");

    assertThat(securityManager.isPermitted(dataRead,
      newResourcePermission(Resource.CLUSTER, Operation.READ, "Example"))).isFalse();

    assertThat(securityManager.isPermitted(dataRead, newResourcePermission(Resource.DATA, Operation.READ))).isFalse();
    assertThat(securityManager.isPermitted(dataRead, newResourcePermission(Resource.DATA, Operation.WRITE))).isFalse();

    assertThat(securityManager.isPermitted(dataRead,
      newResourcePermission(Resource.DATA, Operation.READ, "Financials"))).isFalse();
  }

  @Test
  public void toPermissionDescriptorForResourceAndOperation() {
    ResourcePermission permission = newResourcePermission(Resource.CLUSTER, Operation.MANAGE);

    assertThat(securityManager.toPermissionDescriptor(permission)).isEqualTo("CLUSTER:MANAGE");
  }

  @Test
  public void toPermissionDescriptorForResourceOperationAndRegion() {
    ResourcePermission permission = newResourcePermission(Resource.CLUSTER, Operation.MANAGE, "Example");

    assertThat(securityManager.toPermissionDescriptor(permission)).isEqualTo("CLUSTER:MANAGE:Example");
  }

  @Test
  public void toPermissionDescriptorForResourceOperationRegionAndKey() {
    ResourcePermission permission = newResourcePermission(Resource.CLUSTER, Operation.MANAGE, "Example", "KeyOne");

    assertThat(securityManager.toPermissionDescriptor(permission)).isEqualTo("CLUSTER:MANAGE:Example:KeyOne");
  }

  @Test
  public void isRequiredPermissionAttributeIsTrue() {
    assertThat(securityManager.isRequiredPermissionAttribute("test")).isTrue();
    assertThat(securityManager.isRequiredPermissionAttribute("star")).isTrue();
  }

  @Test
  public void isRequiredPermissionAttributedIsFalse() {
    assertThat(securityManager.isRequiredPermissionAttribute(null)).isFalse();
    assertThat(securityManager.isRequiredPermissionAttribute("")).isFalse();
    assertThat(securityManager.isRequiredPermissionAttribute("  ")).isFalse();
    assertThat(securityManager.isRequiredPermissionAttribute("*")).isFalse();
    assertThat(securityManager.isRequiredPermissionAttribute("  * ")).isFalse();
  }
}
