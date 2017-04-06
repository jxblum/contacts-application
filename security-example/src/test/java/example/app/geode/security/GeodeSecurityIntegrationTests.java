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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;

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
import org.apache.geode.internal.security.shiro.GeodePermissionResolver;
import org.apache.geode.security.NotAuthorizedException;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.AllSuccessfulStrategy;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.AuthenticatingSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.realm.text.PropertiesRealm;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableAuth;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.config.annotation.EnableSecurity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.core.bean.factory.config.BeanPostProcessorSupport;
import example.app.core.util.CollectionUtils;
import example.app.geode.cache.loader.EchoCacheLoader;
import example.app.geode.security.GeodeSecurityIntegrationTests.GeodeClientConfiguration;
import example.app.geode.security.model.User;
import example.app.geode.security.provider.SimpleSecurityManager;
import example.app.geode.security.repository.SecurityRepository;
import example.app.geode.security.repository.support.JdbcSecurityRepository;
import example.app.geode.security.repository.support.XmlSecurityRepository;
import example.app.geode.tests.integration.AbstractGeodeIntegrationTests;
import example.app.shiro.authc.pam.ShiroAuthenticationStrategy;
import example.app.shiro.realm.SecurityRepositoryAuthorizingRealm;

/**
 * Integration tests testing the configuration and use of Apache Geode's Integrated Security feature (framework)
 * as well as Apache Shiro in a Spring context to secure Apache Geode.
 *
 * @author John Blum
 * @see org.junit.FixMethodOrder
 * @see org.junit.Test
 * @see org.springframework.test.annotation.DirtiesContext
 * @see org.springframework.test.context.ActiveProfiles
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.geode.security.GeodeSecurityIntegrationTests.GeodeClientConfiguration
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

  protected static final int CACHE_SERVER_MAX_TIME_BETWEEN_PINGS = 60000;
  protected static final int CACHE_SERVER_PORT = 40404;
  protected static final int CLIENT_CACHE_READ_TIMEOUT = 600000;

  protected static final long CLIENT_CACHE_PING_INTERVAL = 30000L;

  protected static final AtomicInteger TEST_CASE_COUNT = new AtomicInteger(0);

  protected static final String CACHE_SERVER_HOST = "localhost";
  protected static final String GEODE_LOG_LEVEL = "config";

  private static Process geodeServer;

  @BeforeClass
  public static void runGeodeServer() throws IOException {
    geodeServer = run(GeodeServerConfiguration.class,
      String.format("-Dgemfire.log-level=%s", logLevel()),
      String.format("-Dshiro.authentication.strategy=%s",
        systemProperty("shiro.authentication.strategy", "atLeastOneSuccessful")),
      String.format("-Dspring.profiles.active=apache-geode-server,%s",
        systemProperty("security-example-profile", "geode-security-manager-proxy-configuration")));

    waitForServerToStart(geodeServer, CACHE_SERVER_HOST, CACHE_SERVER_PORT);
  }

  @AfterClass
  public static void stopGeodeServer() throws IOException {
    stop(geodeServer);
  }

  protected static String systemProperty(String propertyName, String defaultValue) {
    return System.getProperty(propertyName, defaultValue);
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

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
  public void unauthorizedUser() {
    assertThat(echo.get("one")).isEqualTo("one");

    exception.expect(ServerOperationException.class);
    exception.expectCause(is(instanceOf(NotAuthorizedException.class)));
    exception.expectMessage(containsString("analyst not authorized for DATA:WRITE:Echo:two"));

    echo.put("two", "four");
  }

  @ClientCacheApplication(name = "GeodeSecurityIntegrationTestsClient", logLevel = GEODE_LOG_LEVEL,
    pingInterval = CLIENT_CACHE_PING_INTERVAL, readTimeout = CLIENT_CACHE_READ_TIMEOUT,
    servers = { @ClientCacheApplication.Server(port = CACHE_SERVER_PORT) })
  @EnableSecurity(clientAuthenticationInitializer = "example.app.geode.security.GeodeClientAuthInitialize.create")
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

  @CacheServerApplication(name = "GeodeSecurityIntegrationTestsServer", logLevel = GEODE_LOG_LEVEL,
    maxTimeBetweenPings = CACHE_SERVER_MAX_TIME_BETWEEN_PINGS, port = CACHE_SERVER_PORT, useBeanFactoryLocator = true)
  @EnableManager(start = true)
  @Import({
    GeodeIntegratedSecurityConfiguration.class,
    GeodeIntegratedSecurityProxyConfiguration.class,
    ApacheShiroIniConfiguration.class,
    ApacheShiroCustomRealmConfiguration.class,
    ApacheShiroProvidedRealmConfiguration.class,
    ApacheShiroProvidedOrderedMultiRealmConfiguration.class
  })
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
      logger.info("Geode Distributed System Properties [{}]%n",
        CollectionUtils.toString(gemfireCache.getDistributedSystem().getProperties()));
    }
  }

  @Configuration
  //@EnableSecurity(securityManagerClass = SimpleSecurityManager.class)
  //@EnableSecurity(securityManagerClassName = "example.app.geode.security.provider.NonSecureSecurityManager")
  @EnableSecurity(securityManagerClassName = "example.app.geode.security.provider.SimpleSecurityManager")
  @Profile("geode-security-manager-configuration")
  static class GeodeIntegratedSecurityConfiguration {
  }

  @Configuration
  @EnableSecurity(securityManagerClassName = "example.app.geode.security.SecurityManagerProxy")
  @Profile("geode-security-manager-proxy-configuration")
  static class GeodeIntegratedSecurityProxyConfiguration {

    @Bean
    DataSource hsqlDataSource() {
      return new EmbeddedDatabaseBuilder()
        .setName("geode_security")
        .setScriptEncoding("UTF-8")
        .setType(EmbeddedDatabaseType.HSQL)
        .addScript("geode-security-schema-ddl.sql")
        .addScript("define-roles-table-ddl.sql")
        .addScript("define-roles-permissions-table-ddl.sql")
        .addScript("define-users-table-ddl.sql")
        .addScript("define-users-roles-table-ddl.sql")
        .addScript("insert-roles-dml.sql")
        .addScript("insert-roles-permissions-dml.sql")
        .addScript("insert-users-dml.sql")
        .addScript("insert-users-roles-dml.sql")
        .build();
    }

    @Bean
    JdbcTemplate hsqlTemplate(DataSource hsqlDataSource) {
      return new JdbcTemplate(hsqlDataSource);
    }

    @Bean
    JdbcSecurityRepository securityRepository(JdbcTemplate hsqlTemplate) {
      return new JdbcSecurityRepository(hsqlTemplate);
    }

    @Bean
    SimpleSecurityManager securityManager(SecurityRepository<User> securityRepository) {
      return new SimpleSecurityManager(securityRepository);
    }
  }

  @Configuration
  @EnableSecurity(shiroIniResourcePath = "shiro.ini")
  @Profile("shiro-ini-configuration")
  static class ApacheShiroIniConfiguration {
  }

  @Configuration
  @EnableSecurity
  @Profile("shiro-custom-realm-configuration")
  static class ApacheShiroCustomRealmConfiguration {

    @Bean
    SecurityRepository<User> securityRepository() {
      return new XmlSecurityRepository();
    }

    @Bean
    Realm geodeRealm(SecurityRepository<User> securityRepository) {
      return new SecurityRepositoryAuthorizingRealm<>(securityRepository);
    }
  }

  @Configuration
  @EnableSecurity
  @Profile("shiro-provided-realm-configuration")
  static class ApacheShiroProvidedRealmConfiguration {

    @Bean
    PropertiesRealm shiroRealm() {
      PropertiesRealm propertiesRealm = new PropertiesRealm();
      propertiesRealm.setResourcePath("classpath:shiro.properties");
      propertiesRealm.setPermissionResolver(new GeodePermissionResolver());
      return propertiesRealm;
    }
  }

  @Configuration
  @EnableSecurity
  @Profile("shiro-provided-ordered-multi-realm-configuration")
  static class ApacheShiroProvidedOrderedMultiRealmConfiguration {

    @Bean
    BeanPostProcessor shiroSecurityManagerPostProcessor() {
      return new BeanPostProcessorSupport() {
        @Override public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
          if ("shiroSecurityManager".equals(beanName) && bean instanceof AuthenticatingSecurityManager) {
            Authenticator authenticator = ((AuthenticatingSecurityManager) bean).getAuthenticator();

            if (authenticator instanceof ModularRealmAuthenticator) {
              ((ModularRealmAuthenticator) authenticator).setAuthenticationStrategy(authenticationStrategyResolver());
            }
          }

          return bean;
        }
      };
    }

    @SuppressWarnings("all")
    private AuthenticationStrategy authenticationStrategyResolver() {
      String authenticationStategyName = systemProperty("shiro.authentication.strategy", "atLeastOneSuccessful");

      ShiroAuthenticationStrategy authenticationStrategy =
        ShiroAuthenticationStrategy.findBy(authenticationStategyName);

      switch (authenticationStrategy) {
        case ALL_SUCCESSFUL:
          return allSuccessfulStrategy();
        case FIRST_SUCCESSFUL:
          return firstSuccessfulStrategy();
        default:
          return atLeastOneSuccessfulStrategy();
      }
    }

    private AllSuccessfulStrategy allSuccessfulStrategy() {
      return new AllSuccessfulStrategy();
    }

    private AtLeastOneSuccessfulStrategy atLeastOneSuccessfulStrategy() {
      return new AtLeastOneSuccessfulStrategy();
    }

    private FirstSuccessfulStrategy firstSuccessfulStrategy() {
      return new FirstSuccessfulStrategy();
    }

    @Bean
    @Order(1)
    IniRealm iniRealm() {
      IniRealm iniRealm = new IniRealm("classpath:partial-shiro.ini");
      iniRealm.setPermissionResolver(new GeodePermissionResolver());
      return iniRealm;
    }

    @Bean
    @Order(2)
    PropertiesRealm propertiesRealm() {
      PropertiesRealm propertiesRealm = new PropertiesRealm();
      propertiesRealm.setResourcePath("classpath:partial-shiro.properties");
      propertiesRealm.setPermissionResolver(new GeodePermissionResolver());
      return propertiesRealm;
    }
  }
}
