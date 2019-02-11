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

package example.tests.geode;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;

/**
 * The MultiThreadedClientProxyRegionSessionIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class  MultiThreadedClientProxyRegionSessionIntegrationTests {

  private static final boolean COPY_ON_READ = true;
  private static final boolean SESSION_REFERENCE_CHECKING_ENABLED = false;

  private static final int LOCATOR_PORT = 10334;
  private static final int THREAD_COUNT = 180;
  private static final int WORKLOAD_SIZE = 10000;

  private static final ClientRegionShortcut CLIENT_REGION_SHORTCUT = ClientRegionShortcut.PROXY;

  private static final String GEMFIRE_LOG_LEVEL = "error";
  private static final String LOCATOR_HOST = "localhost";

  private final AtomicInteger sessionReferenceCounter = new AtomicInteger(0);
  private final AtomicInteger threadCounter = new AtomicInteger(0);

  private final AtomicReference<String> sessionId = new AtomicReference<>(null);

  private ClientCache clientCache;

  private final List<String> existingSessionAttributeNames = Collections.synchronizedList(new ArrayList<>());

  private final Random random = new Random(System.currentTimeMillis());

  private Region<String, Session> sessions;

  private final Set<Integer> sessionReferences = Collections.synchronizedSet(new HashSet<>(WORKLOAD_SIZE));

  /*
  private final Set<Session> sessionReferences =
    Collections.synchronizedSet(new TreeSet<>((sessionOne, sessionTwo) -> sessionOne == sessionTwo ? 0
      : sessionReferenceCounter.incrementAndGet() % 2 == 0 ? -1 : 1));
  */

  @Before
  public void setupGemFireClient() {

    this.clientCache = new ClientCacheFactory(gemfireProperties())
      .addPoolLocator(LOCATOR_HOST, LOCATOR_PORT)
      .setPoolMaxConnections(-1)
      .setPoolPingInterval(1000)
      .setPoolSubscriptionEnabled(true)
      .create();

    // TODO - REALLY?! See GEODE-6152.
    this.clientCache.setCopyOnRead(COPY_ON_READ);

    this.sessions = this.clientCache.<String, Session>createClientRegionFactory(CLIENT_REGION_SHORTCUT)
      .setKeyConstraint(String.class)
      .setValueConstraint(Session.class)
      .create("Sessions");

    this.sessions.registerInterestForAllKeys();

    setupSession();
  }

  private Properties gemfireProperties() {

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", "SessionClient");
    gemfireProperties.setProperty("log-level", GEMFIRE_LOG_LEVEL);

    return gemfireProperties;
  }

  private void setupSession() {

    Instant beforeCreationTime = Instant.now();

    Session session = Session.create();

    assertThat(session).isNotNull();
    assertThat(session.getId()).isNotEmpty();
    assertThat(session.getCreationTime()).isAfterOrEqualTo(beforeCreationTime);
    assertThat(session.getCreationTime()).isBeforeOrEqualTo(Instant.now());
    assertThat(session.getLastAccessedTime()).isEqualTo(session.getCreationTime());
    assertThat(session.hasDelta()).isTrue();
    assertThat(session.getAttributeNames()).isEmpty();

    this.sessionId.set(save(session).getId());
  }

  @After
  public void closeClientCache() {

    Optional.ofNullable(this.clientCache)
      .ifPresent(ClientCache::close);
  }

  private void assertUniqueSessionReference(Session session) {

    if (SESSION_REFERENCE_CHECKING_ENABLED) {

      int sessionIdentityHashCode = System.identityHashCode(session);

      assertThat(this.sessionReferences.add(sessionIdentityHashCode))
        .describedAs("Session reference [%d] already exist; Set size is [%d]",
          sessionIdentityHashCode, this.sessionReferences.size())
        .isTrue();
    }
  }

  private Session findById(String id) {

    return Optional.ofNullable(this.sessions.get(id))
      .map(Session::commit)
      .map(Session::touch)
      .orElseThrow(() -> new IllegalStateException(String.format("No Session with ID [%s] was found", id)));
  }

  // TODO - Save the Session iff the Session is actually "dirty"; See GEODE-6032 (https://issues.apache.org/jira/browse/GEODE-6032)
  // TODO - Also remember/see GEODE-6099 (https://issues.apache.org/jira/browse/GEODE-6099)
  private Session save(Session session) {

    //if (session != null) {
    if (session != null && session.hasDelta()) {
      this.sessions.put(session.getId(), session);
      session.commit();
    }

    return session;
  }

  private ExecutorService newSessionWorkloadExecutor() {

    return Executors.newFixedThreadPool(THREAD_COUNT, runnable -> {

      Thread sessionThread = new Thread(runnable);

      sessionThread.setName(String.format("Session Thread %d", this.threadCounter.incrementAndGet()));
      sessionThread.setDaemon(true);
      sessionThread.setPriority(Thread.NORM_PRIORITY);

      return sessionThread;
    });
  }

  private Collection<Callable<Integer>> newSessionWorkloadTasks() {

    Collection<Callable<Integer>> sessionWorkloadTasks = new ArrayList<>(WORKLOAD_SIZE);

    for (int count = 0; count < WORKLOAD_SIZE; count++) {
      sessionWorkloadTasks.add(count % 79 != 0
        ? newAddSessionAttributeTask()
        : count % 237 != 0
        ? newRemoveSessionAttributeTask()
        : newSessionReaderTask());
    }

    return sessionWorkloadTasks;
  }

  private Callable<Integer> newAddSessionAttributeTask() {

    return () -> {

      Instant beforeLastAccessedTime = Instant.now();

      Session session = findById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getLastAccessedTime()).isAfterOrEqualTo(beforeLastAccessedTime);
      assertUniqueSessionReference(session);

      String name = UUID.randomUUID().toString();
      Object value = System.currentTimeMillis();

      session.setAttribute(name, value);

      save(session);

      this.existingSessionAttributeNames.add(name);

      return 1;
    };
  }

  private Callable<Integer> newRemoveSessionAttributeTask() {

    return () -> {

      int returnValue = 0;

      Instant beforeLastAccessedTime = Instant.now();

      Session session = findById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getLastAccessedTime()).isAfterOrEqualTo(beforeLastAccessedTime);
      assertUniqueSessionReference(session);

      String attributeName = null;

      synchronized (this.existingSessionAttributeNames) {

        int size = this.existingSessionAttributeNames.size();

        if (size > 0) {

          int index = this.random.nextInt(size);

          attributeName = this.existingSessionAttributeNames.remove(index);
        }
      }

      if (session.getAttributeNames().contains(attributeName)) {
        session.removeAttribute(attributeName);
        returnValue = -1;
      }
      else {
        Optional.ofNullable(attributeName)
          .filter(it -> !it.trim().isEmpty())
          .ifPresent(this.existingSessionAttributeNames::add);
      }

      save(session);

      return returnValue;
    };
  }

  private Callable<Integer> newSessionReaderTask() {

    return () -> {

      Instant beforeLastAccessedTime = Instant.now();

      Session session = findById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getLastAccessedTime()).isAfterOrEqualTo(beforeLastAccessedTime);
      assertUniqueSessionReference(session);

      save(session.touch());

      return 0;
    };
  }

  private int safeFutureGet(Future<Integer> future) {

    try {
      return future.get();
    }
    catch (Exception cause) {
      throw new RuntimeException("Session access task failure", cause);
    }
  }

  private int runSessionWorkload() throws InterruptedException {

    ExecutorService sessionBatchWorkloadExecutor = newSessionWorkloadExecutor();

    try {

      Collection<Future<Integer>> results = sessionBatchWorkloadExecutor.invokeAll(newSessionWorkloadTasks());

      return results.stream()
        .mapToInt(this::safeFutureGet)
        .sum();
    }
    finally {
      Optional.of(sessionBatchWorkloadExecutor).ifPresent(ExecutorService::shutdownNow);
    }
  }

  @Test
  public void concurrentSessionAccessIsCorrect() throws InterruptedException {

    int sessionAttributeCount = runSessionWorkload();

    assertThat(sessionAttributeCount).isEqualTo(this.existingSessionAttributeNames.size());

    Session session = findById(this.sessionId.get());

    assertThat(session).isNotNull();
    assertThat(session.getId()).isEqualTo(this.sessionId.get());
    assertThat(session.getAttributeNames()).hasSize(sessionAttributeCount);
  }
}
