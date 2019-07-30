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
package example.tests.spring.data.geode.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.CacheWriter;
import org.apache.geode.cache.CacheWriterException;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionEvent;
import org.apache.geode.cache.util.CacheListenerAdapter;
import org.apache.geode.cache.util.CacheWriterAdapter;
import org.cp.elements.util.ArrayUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import example.tests.spring.data.geode.cache.GeodeClusterReadyApplicationListener.GeodeClusterReadyApplicationEvent;

/**
 * Integration Tests to test and assert the behavior of the {@link GeodeClusterReadyApplicationListener}.
 *
 * @author John Blum
 * @see java.util.concurrent.Callable
 * @see java.util.concurrent.CountDownLatch
 * @see java.util.concurrent.CopyOnWriteArrayList
 * @see java.util.concurrent.Execurtors
 * @see org.apache.geode.cache.CacheWriter
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.Region
 * @see org.springframework.context.ApplicationListener
 * @see org.springframework.data.gemfire.LocalRegionFactoryBean
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("all")
public class GeodeClusterReadyIntegrationTests {

  // TODO: Switch this boolean flag from true to false to observe the test failing, which is also equivalent to
  //  using GemFire/Geode objects (e.g. Regions) before they are fully initialized across the cluster.
  private static final boolean GEMFIRE_OBJECT_INITIALIZATION_SAFETY_ENABLED = true;

  private static final List<String> TEST_OPERATION_LOG = new CopyOnWriteArrayList<>();

  private static void log(String message, Object... args) {
    TEST_OPERATION_LOG.add(String.format(message, args));
  }

  private static void printTestOperationLog() {
    TEST_OPERATION_LOG.forEach(System.err::println);
  }

  @BeforeClass
  public static void preSetup() {

    System.setProperty("spring.data.gemfire.object.initialization-safety.enabled",
      String.valueOf(GEMFIRE_OBJECT_INITIALIZATION_SAFETY_ENABLED));
  }

  @AfterClass
  public static void postTearDown() {

    System.getProperties().stringPropertyNames().stream()
      .filter(propertyName -> propertyName.startsWith("spring.data.gemfire."))
      .forEach(System::clearProperty);
  }

  @Autowired
  private GemFireCache gemfireCache;

  private Region<Object, Object> a;

  private Region<Object, Object> b;

  @Autowired
  private RegionUsingApplicationComponent applicationComponent;

  @Before
  public void setup() {

    assertThat(this.gemfireCache).isNotNull();

    this.a = this.gemfireCache.getRegion("/A");
    this.b = this.gemfireCache.getRegion("/B");

    assertThat(this.a).isNotNull();
    assertThat(this.a.getName()).isEqualTo("A");
    assertThat(this.b).isNotNull();
    assertThat(this.b.getName()).isEqualTo("B");
  }

  @Test
  public void beanCreationOrderAndInteractionsAreCorrect() {

    try {

      this.applicationComponent.run();

      assertThat(this.b.containsKey(1)).isFalse();
      assertThat(this.a.put(1, "TEST-ONE")).isNull();
      assertThat(this.b.get(1)).isEqualTo("TEST-ONE");
      assertThat(this.a.get(1)).isEqualTo("TEST-ONE");
      assertThat(this.a.get(2)).isEqualTo("TEST-TWO");
      assertThat(this.b.get(2)).isEqualTo("TEST-TWO");

      assertThat(TEST_OPERATION_LOG)
        .describedAs("TEST_OPERATION_LOG was [%s]", TEST_OPERATION_LOG)
        .containsExactly(
          "REGION [B] CREATE",
          "COPY TO [B] CACHE_WRITER INITIALIZED",
          "REGION [A] CREATE",
          "APPLICATION_COMPONENT DEPENDENT ON REGION [A] CONSTRUCTED",
          "APPLICATION_COMPONENT BEFORE REGION [A] PUT(2, \"TEST-TWO\")",
          "APPLICATION_COMPONENT DEPENDENT ON REGION [A] INITIALIZED",
          "REGION [B] ENTRY [2] CREATE",
          "REGION [A] ENTRY [2] CREATE",
          "APPLICATION_COMPONENT AFTER REGION [A] PUT(2, \"TEST-TWO\")",
          "REGION [B] ENTRY [1] CREATE",
          "REGION [A] ENTRY [1] CREATE"
        );
    }
    finally {
      printTestOperationLog();
    }
  }

  @PeerCacheApplication
  // NOTE: Java-based Spring Container configuration in-place of Spring XML configuration (very similar)
  // NOTE: Order of declared bean definitions below does NOT determine the order in which the beans
  // are created by the Spring Containter.
  static class TestGeodePeerCacheNodeConfiguration {

    @Bean
    RegionUsingApplicationComponent regionUsingApplicationComponent(@Qualifier("A") Region<Object, Object> a) {
      return new RegionUsingApplicationComponent(a);
    }

    @Bean("A")
    LocalRegionFactoryBean<Object, Object> aRegion(GemFireCache gemfireCache,
        @Qualifier("copyToBCacheWriter") CacheWriter<Object, Object> copyCacheWriter) {

      LocalRegionFactoryBean<Object, Object> aRegion = new LocalRegionFactoryBean<>();

      aRegion.setCache(gemfireCache);
      aRegion.setCacheListeners(ArrayUtils.asArray(operationLoggingCacheListener()));
      aRegion.setCacheWriter(copyCacheWriter);
      aRegion.setPersistent(false);

      return aRegion;
    }

    @Bean("B")
    LocalRegionFactoryBean<Object, Object> bRegion(GemFireCache gemfireCache) {

      LocalRegionFactoryBean<Object, Object> bRegion = new LocalRegionFactoryBean<>();

      bRegion.setCache(gemfireCache);
      bRegion.setCacheListeners(ArrayUtils.asArray(operationLoggingCacheListener()));
      bRegion.setPersistent(false);

      return bRegion;
    }

    @Bean
    CopyCacheWriter<Object, Object> copyToBCacheWriter() {
      return new CopyToBCacheWriter();
    }

    @Bean
    OperationLoggingCacheListener operationLoggingCacheListener() {
      return new OperationLoggingCacheListener();
    }

    @Bean
    // NOTE: The most important Spring Bean ever! ;-)
    GeodeClusterReadyApplicationListener geodeClusterReadyApplicationListener() {
      return new GeodeClusterReadyApplicationListener();
    }
  }

  static abstract class CopyCacheWriter<K, V> extends CacheWriterAdapter<K, V>
      implements CountDownLatchInitializationSafeApplicationComponent {

    protected abstract Optional<Region<K, V>> getRegion();

    @Override
    public void beforeCreate(EntryEvent<K, V> event) throws CacheWriterException {

      getRegion().ifPresent(region ->
        doInitializationDependentOperationSafely(() ->
          region.put(event.getKey(), event.getNewValue())));
    }

    @Override
    public void beforeDestroy(EntryEvent<K, V> event) throws CacheWriterException {

      getRegion().ifPresent(region ->
        doInitializationDependentOperationSafely(() ->
          region.remove(event.getKey())));
    }

    @Override
    public void beforeUpdate(EntryEvent<K, V> event) throws CacheWriterException {

      getRegion().ifPresent(region ->
        doInitializationDependentOperationSafely(() ->
          region.replace(event.getKey(), event.getOldValue(), event.getNewValue())));
    }

    @Override
    public String toString() {
      return getRegion().map(Region::getName).orElse("?");
    }
  }

  static class CopyToBCacheWriter extends CopyCacheWriter<Object, Object> implements InitializingBean {

    @Value("${spring.data.gemfire.object.initialization-safety.enabled:true}")
    private boolean initializationSafetyEnabled;

    @Autowired
    @Qualifier("B")
    //@Resource(name = "B")
    private Region<Object, Object> b;

    @Override
    protected Optional<Region<Object, Object>> getRegion() {
      return Optional.ofNullable(this.b);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
      log("COPY TO [%s] CACHE_WRITER INITIALIZED", this);
    }

    @Override
    public boolean isInitializationSafetyEnabled() {
      return this.initializationSafetyEnabled;
    }
  }

  static interface InitializationSafeApplicationComponent
      extends ApplicationListener<GeodeClusterReadyApplicationEvent> {

    void block() throws InterruptedException;

    boolean isInitializationSafetyEnabled();

    void release();

    @Override
    default void onApplicationEvent(GeodeClusterReadyApplicationEvent geodeClusterReadyApplicationEvent) {
      release();
    }

    // NOTE: This method is the absolutely necessary!
    // This utility method absolutely depends on the GeodeClusterReadyApplicationListener, which must be registered
    // in the Spring ApplicationContext (No Exceptions)!
    default <T> T doInitializationDependentOperationSafely(Callable<T> callable) {

      return doThrowableOperationSafely(() -> {
        block();
        return callable.call();
      });
    }

    default <T> T doThrowableOperationSafely(ThrowableOperation<T> operation) {

      try {
        return operation.doOp();
      }
      // TODO: Dumb, but simple, InterruptedException handler! Do better!!
      catch (InterruptedException cause) {
        Thread.currentThread().interrupt();
        return null;
      }
      catch (Throwable cause) {
        throw new RuntimeException(cause);
      }
    }
  }

  static interface CountDownLatchInitializationSafeApplicationComponent
      extends InitializationSafeApplicationComponent {

    static CountDownLatch latch = new CountDownLatch(1);

    @Override
    default void block() throws InterruptedException {

      if (isInitializationSafetyEnabled()) {
        this.latch.await();
      }
    }

    @Override
    default void release() {
      this.latch.countDown();
    }
  }

  static class OperationLoggingCacheListener extends CacheListenerAdapter<Object, Object> {

    @Override
    public void afterCreate(EntryEvent<Object, Object> event) {
      log("REGION [%s] ENTRY [%s] CREATE", event.getRegion().getName(), event.getKey());
    }

    @Override
    public void afterDestroy(EntryEvent<Object, Object> event) {
      log("REGION [%s] ENTRY [%s] DESTROY", event.getRegion().getName(), event.getKey());
    }

    @Override
    public void afterInvalidate(EntryEvent<Object, Object> event) {
      log("REGION [%s] ENTRY [%s] INVALIDATE", event.getRegion().getName(), event.getKey());
    }

    @Override
    public void afterUpdate(EntryEvent<Object, Object> event) {
      log("REGION [%s] ENTRY [%s] UPDATE", event.getRegion().getName(), event.getKey());
    }

    @Override
    public void afterRegionCreate(RegionEvent<Object, Object> event) {
      log("REGION [%s] CREATE", event.getRegion().getName());
    }
  }

  static class RegionUsingApplicationComponent
      implements CountDownLatchInitializationSafeApplicationComponent, InitializingBean, Runnable {

    @Value("${spring.data.gemfire.object.initialization-safety.enabled:true}")
    private boolean initializationSafetyEnabled;

    private final CountDownLatch latchAfter = new CountDownLatch(1);
    private final CountDownLatch latchBefore = new CountDownLatch(1);

    private final Region<Object, Object> region;

    RegionUsingApplicationComponent(Region<Object, Object> region) {

      Assert.notNull(region, "Region must not be null");

      this.region = region;

      log("APPLICATION_COMPONENT DEPENDENT ON REGION [%s] CONSTRUCTED", this.region.getName());
    }

    @Override
    public void afterPropertiesSet() throws Exception {

      String regionName = this.region.getName();

      Executors.newSingleThreadExecutor().submit(() -> {

        log("APPLICATION_COMPONENT BEFORE REGION [%s] PUT(2, \"TEST-TWO\")", regionName);
        this.latchBefore.countDown();

        doInitializationDependentOperationSafely(() -> this.region.put(2, "TEST-TWO"));

        log("APPLICATION_COMPONENT AFTER REGION [%s] PUT(2, \"TEST-TWO\")", regionName);
        this.latchAfter.countDown();
      });

      // NOTE: Force the initialization of this application component (bean) to at least wait for the spawned Thread
      // to try and prematurely access Region B indirectly through Region A.
      doThrowableOperationSafely(() -> {
        this.latchBefore.await();
        Thread.sleep(1000);
        log("APPLICATION_COMPONENT DEPENDENT ON REGION [%s] INITIALIZED", regionName);
        return null;
      });
    }

    @Override
    public boolean isInitializationSafetyEnabled() {
      return this.initializationSafetyEnabled;
    }

    @Override
    public void run() {
      // NOTE: Wait for this application component to complete its work before the test attempts to modify the Regions.
      doThrowableOperationSafely(() -> { this.latchAfter.await(); return null; });
    }
  }

  @FunctionalInterface
  interface ThrowableOperation<T> {
    T doOp() throws Throwable;
  }
}
