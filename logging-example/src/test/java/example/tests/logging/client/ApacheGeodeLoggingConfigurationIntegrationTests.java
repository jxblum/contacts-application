/*
 * Copyright 2018 the original author or authors.
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
package example.tests.logging.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.distributed.internal.DistributionConfig;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableLogging;
import org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport;
import org.springframework.data.gemfire.util.ArrayUtils;
import org.springframework.data.gemfire.util.PropertiesBuilder;
import org.springframework.util.StringUtils;

/**
 * Integration Tests testing the configuration of Apache Geode Logging.
 *
 * @author John Blum
 * @see java.util.Properties
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.distributed.internal.DistributionConfig
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.core.env.PropertiesPropertySource
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableLogging
 * @see org.springframework.data.gemfire.tests.integration.SpringApplicationContextIntegrationTestsSupport
 * @see org.springframework.data.gemfire.util.PropertiesBuilder
 * @since 1.0.0
 */
public class ApacheGeodeLoggingConfigurationIntegrationTests extends SpringApplicationContextIntegrationTestsSupport {

  private AtomicReference<Properties> propertiesReference = new AtomicReference<>(null);

  @Before
  public void setup() {

    FileFilter gemfireTestLogFileFilter = file ->
      file.getName().startsWith("gemfire-test") & file.getName().endsWith(".log");

    File[] files = new File(System.getProperty("user.dir")).listFiles(gemfireTestLogFileFilter);

    Arrays.stream(ArrayUtils.nullSafeArray(files, File.class)).forEach(File::delete);

    this.propertiesReference.set(null);
  }

  private void with(Properties properties) {
    Optional.ofNullable(properties).ifPresent(this.propertiesReference::set);
  }

  @Override
  protected ConfigurableApplicationContext processBeforeRefresh(ConfigurableApplicationContext applicationContext) {

    Optional.ofNullable(this.propertiesReference.get())
      .ifPresent(properties -> {
        applicationContext.getEnvironment().getPropertySources()
          .addFirst(new PropertiesPropertySource("Test Properties", properties));
      });

    return super.processBeforeRefresh(applicationContext);
  }

  private void assertGemFireCacheLogLevelAndLogFile(String logLevel, String logFile) {

    GemFireCache gemfireCache = getApplicationContext().getBean(GemFireCache.class);

    logFile = StringUtils.hasText(logFile) ? logFile : "";

    assertThat(gemfireCache).isNotNull();
    assertThat(gemfireCache.getDistributedSystem()).isNotNull();

    Properties distributedSystemProperties = gemfireCache.getDistributedSystem().getProperties();

    assertThat(distributedSystemProperties).isNotNull();
    assertThat(distributedSystemProperties.getProperty(DistributionConfig.LOG_LEVEL_NAME)).isEqualTo(logLevel);
    assertThat(distributedSystemProperties.getProperty(DistributionConfig.LOG_FILE_NAME)).isEqualTo(logFile);
  }

  @Test
  public void defaultLoggingConfigurationUsingClientCacheApplication() {

    newApplicationContext(DefaultClientCacheApplicationTestConfiguration.class);

    assertGemFireCacheLogLevelAndLogFile("config", null);
  }

  @Test
  public void logLevelAttributeConfiguredClientCacheApplication() {

    newApplicationContext(LogLevelConfiguredClientCacheApplicationTestConfiguration.class);

    assertGemFireCacheLogLevelAndLogFile("finer", null);
  }

  @Test
  public void logLevelPropertyConfiguredClientCacheApplication() {

    with(PropertiesBuilder.create().setProperty("spring.data.gemfire.cache.log-level", "info").build());

    newApplicationContext(LogLevelConfiguredClientCacheApplicationTestConfiguration.class);

    assertGemFireCacheLogLevelAndLogFile("info", null);

  }

  @Test
  public void withCustomLoggingEnabledClientCacheApplicationConfiguration() {

    newApplicationContext(DefaultClientCacheApplicationTestConfiguration.class, CustomLoggingTestConfiguration.class);

    assertGemFireCacheLogLevelAndLogFile("warning", "gemfire-test-zero.log");
  }

  @Test
  public void withCustomLoggingEnabledAndLogLevelPropertyConfiguredClientCacheApplicationConfiguration() {

    with(PropertiesBuilder.create()
      .setProperty("spring.data.gemfire.logging.log-file", "gemfire-test-one.log")
      .setProperty("spring.data.gemfire.logging.level", "error")
      .build());

    newApplicationContext(LogLevelConfiguredClientCacheApplicationTestConfiguration.class,
      CustomLoggingTestConfiguration.class);

    assertGemFireCacheLogLevelAndLogFile("error", "gemfire-test-one.log");
  }

  @Test
  public void withDefaultLoggingEnabledAndLogLevelAttributeConfiguredClientCacheApplicationConfiguration() {

    newApplicationContext(LogLevelConfiguredClientCacheApplicationTestConfiguration.class,
      DefaultLoggingTestConfiguration.class);

    assertGemFireCacheLogLevelAndLogFile("finer", null);
  }

  @Test
  public void withoutLoggingEnabledLoggingPropertiesHaveNoEffect() {

    with(PropertiesBuilder.create()
      .setProperty("spring.data.gemfire.logging.log-file", "gemfire-test-two.log")
      .setProperty("spring.data.gemfire.logging.level", "error")
      .build());

    newApplicationContext(LogLevelConfiguredClientCacheApplicationTestConfiguration.class);

    assertGemFireCacheLogLevelAndLogFile("finer", null);
  }

  @ClientCacheApplication(name = "DefaultClientCacheApplication")
  static class DefaultClientCacheApplicationTestConfiguration { }

  @ClientCacheApplication(name = "ClientCacheApplicationWithLogLevel", logLevel = "finer")
  static class LogLevelConfiguredClientCacheApplicationTestConfiguration { }

  @EnableLogging
  static class DefaultLoggingTestConfiguration { }

  @EnableLogging(logLevel = "warning", logFile = "gemfire-test-zero.log")
  static class CustomLoggingTestConfiguration { }

}
