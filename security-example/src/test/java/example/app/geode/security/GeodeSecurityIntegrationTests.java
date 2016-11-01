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

import static example.app.geode.security.model.User.newUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.client.ServerOperationException;
import org.apache.geode.security.AuthenticationFailedException;
import org.apache.geode.security.NotAuthorizedException;
import org.apache.geode.security.ResourcePermission;
import org.apache.shiro.realm.Realm;
import org.cp.elements.lang.Assert;
import org.cp.elements.util.PropertiesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableAuth;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.EnableSecurity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.core.util.CollectionUtils;
import example.app.geode.cache.loader.EchoCacheLoader;
import example.app.geode.security.GeodeSecurityIntegrationTests.GeodeClientConfiguration;
import example.app.geode.security.model.User;
import example.app.geode.security.repository.SecurityRepository;
import example.app.geode.security.repository.support.XmlSecurityRepository;
import example.app.geode.security.support.AuthInitializeSupport;
import example.app.geode.tests.integration.AbstractGeodeIntegrationTests;
import example.app.shiro.realm.SecurityRepositoryAuthorizingRealm;

/**
 * Integration tests testing the configuration and use of Apache Geode's Integrated Security feature (framework)
 * as well as Apache Shiro in a Spring context to secure Apache Geode.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see example.app.geode.tests.integration.AbstractGeodeIntegrationTests
 * @see <a href="http://shiro.apache.org/">Apache Shiro</a>
 * @see <a href="https://cwiki.apache.org/confluence/display/GEODE/Geode+Integrated+Security">Geode Integrated Security</a>
 * @see <a href="https://cwiki.apache.org/confluence/display/GEODE/Using+Custom+SecurityManager">Using a Custom SecurityManager</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration(classes = GeodeClientConfiguration.class)
@ActiveProfiles({ "apache-geode-client" })
@SuppressWarnings("unused")
public class GeodeSecurityIntegrationTests extends AbstractGeodeIntegrationTests {

  protected static final int GEODE_CACHE_SERVER_MAX_TIME_BETWEEN_PINGS = 60000;
  protected static final int GEODE_CACHE_SERVER_PORT = 40404;
  protected static final int GEODE_CLIENT_CACHE_READ_TIMEOUT = 600000;

  protected static final long GEODE_CLIENT_CACHE_PING_INTERVAL = 30000L;

  protected static final AtomicInteger RUN_COUNT = new AtomicInteger(0);

  protected static final String DEFAULT_GEMFIRE_LOG_LEVEL = "config";
  protected static final String GEODE_CACHE_SERVER_HOST = "localhost";

  private static Process geodeServer;

  @BeforeClass
  public static void geodeServerSetup() throws IOException {
    List<String> arguments = new ArrayList<>();

    arguments.add(String.format("-Dgemfire.log-level=%s", logLevel()));

    arguments.add(String.format("-Dspring.profiles.active=apache-geode-server,%s",
      System.getProperty("security-example-profile", "apache-geode-security")));

    geodeServer = run(geodeServerDirectoryPathname(), GeodeServerConfiguration.class,
      arguments.toArray(new String[arguments.size()]));

    waitForServerToStart(geodeServer, GEODE_CACHE_SERVER_HOST, GEODE_CACHE_SERVER_PORT);
  }

  static String geodeServerDirectoryPathname() {
    return String.format("%1$s-server-%2$s", GeodeSecurityIntegrationTests.class.getSimpleName(),
      LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss")));
  }

  static String logLevel() {
    return System.getProperty("gemfire.log.level", DEFAULT_GEMFIRE_LOG_LEVEL);
  }

  @AfterClass
  public static void geodeServerShutdown() throws IOException {
    stop(geodeServer);
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  @Resource(name = "Echo")
  private Region<String, String> echo;

  @Test
  @DirtiesContext
  public void authorizedUser() {
    assertThat(echo.get("one")).isEqualTo("one");
    assertThat(echo.put("two", "four")).isNull();
    assertThat(echo.get("two")).isEqualTo("four");
  }

  @Test
  public void notAuthorizedUser() {
    assertThat(echo.get("one")).isEqualTo("one");

    exception.expect(ServerOperationException.class);
    exception.expectCause(is(instanceOf(NotAuthorizedException.class)));
    exception.expectMessage(containsString("analyst not authorized for DATA:WRITE:Echo:two"));

    echo.put("two", "four");
  }

  @ClientCacheApplication(name = "GeodeSecurityIntegrationTestsClient", logLevel = "config",
    pingInterval = GEODE_CLIENT_CACHE_PING_INTERVAL, readTimeout = GEODE_CLIENT_CACHE_READ_TIMEOUT,
    servers = { @ClientCacheApplication.Server(port = GEODE_CACHE_SERVER_PORT)})
  @EnableAuth(clientAuthenticationInitializer = "example.app.geode.security.GeodeSecurityIntegrationTests$TestAuthInitialize.create")
  @Profile("apache-geode-client")
  static class GeodeClientConfiguration {

    @Bean("Echo")
    ClientRegionFactoryBean<String, String> echoRegion(GemFireCache gemfireCache) {
      ClientRegionFactoryBean<String, String> echoRegion = new ClientRegionFactoryBean<>();

      echoRegion.setCache(gemfireCache);
      echoRegion.setClose(false);
      echoRegion.setShortcut(ClientRegionShortcut.PROXY);

      return echoRegion;
    }
  }

  public static class TestAuthInitialize extends AuthInitializeSupport {

    protected static final User ANALYST = newUser("analyst").with("p@55w0rd");
    protected static final User SCIENTIST = newUser("scientist").with("w0rk!ng4u");

    private final User user;

    /* (non-Javadoc) */
    public static TestAuthInitialize create() {
      return new TestAuthInitialize(RUN_COUNT.incrementAndGet() < 2 ? SCIENTIST : ANALYST);
    }

    /* (non-Javadoc) */
    public TestAuthInitialize(User user) {
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

    /* (non-Javadoc) */
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

  @CacheServerApplication(name = "GeodeSecurityIntegrationTestsServer", logLevel = "config",
    maxTimeBetweenPings = GEODE_CACHE_SERVER_MAX_TIME_BETWEEN_PINGS, port = GEODE_CACHE_SERVER_PORT)
  @EnableManager(start = true)
  @Import({ GeodeIntegratedSecurityConfiguration.class, ApacheShiroIniConfiguration.class,
    ApacheShiroSpringConfiguration.class })
  @Profile("apache-geode-server")
  public static class GeodeServerConfiguration {

    public static void main(String[] args) {
      SpringApplication.run(GeodeServerConfiguration.class, args);
    }

    @Autowired
    private GemFireCache gemfireCache;

    @Bean("Echo")
    LocalRegionFactoryBean<String, String> echoRegion(GemFireCache gemfireCache) {
      LocalRegionFactoryBean<String, String> echoRegion = new LocalRegionFactoryBean<>();

      echoRegion.setCache(gemfireCache);
      echoRegion.setCacheLoader(EchoCacheLoader.getInstance());
      echoRegion.setClose(false);
      echoRegion.setPersistent(false);

      return echoRegion;
    }

    @PostConstruct
    public void postProcess() {
      System.err.printf("Geode Distributed System Properties [%s]%n",
        CollectionUtils.toString(gemfireCache.getDistributedSystem().getProperties()));
    }
  }

  @Configuration
  //@EnableSecurity(securityManagerClass = SimpleSecurityManager.class)
  @EnableSecurity(securityManagerClassName = "example.app.geode.security.provider.SimpleSecurityManager")
  //@EnableSecurity(securityManagerClassName = "example.app.geode.security.GeodeSecurityIntegrationTests$NonSecureSecurityManager")
  @Profile("geode-security-system-property-configuration")
  static class GeodeIntegratedSecurityConfiguration {
  }

  /* (non-Javadoc) */
  public static class NonSecureSecurityManager extends SecurityManagerAdapter {

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
      logger.debug("Principal {} requires Permission {}", principal, permission);
      return super.authorize(principal, permission);
    }
  }

  @Configuration
  @EnableSecurity(shiroIniResourcePath = "geode-security-shiro.ini")
  @Profile("shiro-security-ini-configuration")
  static class ApacheShiroIniConfiguration {
  }

  @Configuration
  @EnableSecurity
  @Profile("shiro-security-spring-configuration")
  static class ApacheShiroSpringConfiguration {

    @Bean
    SecurityRepository<User> securityRepository() {
      return new XmlSecurityRepository();
    }

    @Bean
    Realm geodeRealm(SecurityRepository<User> securityRepository) {
      return new SecurityRepositoryAuthorizingRealm<>(securityRepository);
    }
  }
}
