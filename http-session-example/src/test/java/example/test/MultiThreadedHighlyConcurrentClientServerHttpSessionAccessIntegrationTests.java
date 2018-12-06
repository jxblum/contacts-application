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

package example.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.Session;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import example.test.support.AbstractSessionIntegrationTests;

/**
 * The MultiThreadedHighlyConcurrentClientServerHttpSessionAccessIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@RunWith(SpringRunner.class)
@ActiveProfiles("spring-session-gemfire")
@SuppressWarnings("unused")
public class MultiThreadedHighlyConcurrentClientServerHttpSessionAccessIntegrationTests
    extends AbstractSessionIntegrationTests {

  private static final boolean SESSION_REFERENCE_CHECK_ENABLED = false;

  private static final int THREAD_COUNT = 180;
  private static final int WORKLOAD_SIZE = 10000;

  private final AtomicInteger threadCounter = new AtomicInteger(0);
  private final AtomicReference<String> sessionId = new AtomicReference<>(null);

  private final List<String> sessionAttributesNames = Collections.synchronizedList(new ArrayList<>(WORKLOAD_SIZE));

  private final Random random = new Random(System.currentTimeMillis());

  private final Set<Integer> sessionReferences = Collections.synchronizedSet(new HashSet<>(WORKLOAD_SIZE));

  @Before
  public void setupSession() {

    Session session = newSession();

    assertThat(session).isNotNull();
    assertThat(session.getId()).isNotEmpty();
    assertThat(session.isExpired()).isFalse();
    assertThat(session.getAttributeNames()).isEmpty();
    assertThat(save(touch(session))).isNotNull();

    this.sessionId.set(session.getId());
  }

  private void assertUniqueSessionReference(Session session) {

    if (SESSION_REFERENCE_CHECK_ENABLED) {

      int sessionIdentityHashCode = System.identityHashCode(session);

      assertThat(this.sessionReferences.add(sessionIdentityHashCode))
        .describedAs("Session reference [%d] already exist; Set size is [%d]",
          sessionIdentityHashCode, this.sessionReferences.size())
        .isTrue();
    }
  }

  private ExecutorService newSessionWorkloadExecutor() {

    return Executors.newFixedThreadPool(THREAD_COUNT, runnable -> {

      Thread sessionThread = new Thread(runnable);

      sessionThread.setDaemon(true);
      sessionThread.setName(String.format("Session Thread %d", this.threadCounter.incrementAndGet()));
      sessionThread.setPriority(Thread.NORM_PRIORITY);

      return sessionThread;
    });
  }

  private Collection<Callable<Integer>> newSessionWorkloadTasks() {

    Collection<Callable<Integer>> sessionAccessWorkload = new ArrayList<>(WORKLOAD_SIZE);

    for (int count = 0; count < WORKLOAD_SIZE; count++) {
      sessionAccessWorkload.add(count % 79 != 0
        ? newAddSessionAttributeTask()
        : count % 237 != 0
        ? newRemoveSessionAttributeTask()
        : newSessionReaderTask());
    }

    return sessionAccessWorkload;
  }

  private Callable<Integer> newAddSessionAttributeTask() {

    return () -> {

      Session session = findById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.isExpired()).isFalse();
      assertUniqueSessionReference(session);

      String name = UUID.randomUUID().toString();
      String value = String.valueOf(System.currentTimeMillis());

      session.setAttribute(name, value);

      save(touch(session));

      this.sessionAttributesNames.add(name);

      return 1;
    };
  }

  @SuppressWarnings("all")
  private Callable<Integer> newRemoveSessionAttributeTask() {

    return () -> {

      int returnValue = 0;

      Session session = findById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.isExpired()).isFalse();
      assertUniqueSessionReference(session);

      String attributeName = null;

      synchronized (this.sessionAttributesNames) {

        int size = this.sessionAttributesNames.size();

        if (size > 0) {

          int index = this.random.nextInt(size);

          attributeName = this.sessionAttributesNames.remove(index);
        }
      }

      if (session.getAttributeNames().contains(attributeName)) {
        session.removeAttribute(attributeName);
        save(touch(session));
        returnValue = -1;
      }
      else {
        Optional.ofNullable(attributeName)
          .filter(StringUtils::hasText)
          .ifPresent(this.sessionAttributesNames::add);
      }

      return returnValue;
    };
  }

  private Callable<Integer> newSessionReaderTask() {

    return () -> {

      Session session = findById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.isExpired()).isFalse();
      assertUniqueSessionReference(session);

      save(touch(session));

      return 0;
    };
  }

  private <T> T safeFutureGet(Future<T> future) {

    try {
      return future.get();
    }
    catch (Exception cause) {
      throw new RuntimeException("FAIL", cause);
    }
  }

  private int runSessionWorkload() throws InterruptedException {

    ExecutorService sessionWorkloadExecutor = newSessionWorkloadExecutor();

    try {

      List<Future<Integer>> sessionWorkloadTasksFutures =
        sessionWorkloadExecutor.invokeAll(newSessionWorkloadTasks());

      return sessionWorkloadTasksFutures.stream()
        .mapToInt(this::safeFutureGet)
        .sum();
    }
    finally {
      Optional.of(sessionWorkloadExecutor)
        .ifPresent(ExecutorService::shutdownNow);
    }
  }

  @Test
  public void concurrentSessionAccessIsCorrect() throws InterruptedException {

    int sessionAttributeCount = runSessionWorkload();

    assertThat(sessionAttributeCount).isEqualTo(this.sessionAttributesNames.size());

    Session session = findById(this.sessionId.get());

    assertThat(session).isNotNull();
    assertThat(session.getId()).isEqualTo(this.sessionId.get());
    assertThat(session.isExpired()).isFalse();
    assertThat(session.getAttributeNames()).hasSize(sessionAttributeCount);
    assertThat(session.getAttributeNames())
      .containsOnly(this.sessionAttributesNames.toArray(new String[this.sessionAttributesNames.size()]));
  }

  @ClientCacheApplication(copyOnRead = true, logLevel = "error", subscriptionEnabled = true)
  @EnableGemFireHttpSession(
    clientRegionShortcut = ClientRegionShortcut.PROXY,
    poolName = "DEFAULT",
    regionName = "Sessions",
    sessionSerializerBeanName = GemFireHttpSessionConfiguration.SESSION_DATA_SERIALIZER_BEAN_NAME
  )
  @Profile("spring-session-gemfire")
  static class GemFireClientConfiguration { }

  @EnableRedisHttpSession
  @Profile("spring-session-redis")
  static class RedisHttpSessionConfiguration {

    @Bean
    public LettuceConnectionFactory connectionFactory() {
      return new LettuceConnectionFactory();
    }
  }
}
