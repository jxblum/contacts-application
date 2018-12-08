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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.apache.geode.DataSerializable;
import org.apache.geode.DataSerializer;
import org.apache.geode.Delta;
import org.apache.geode.InvalidDeltaException;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;

/**
 * The TwoThreadsClientProxyRegionSessionIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public class TwoThreadsClientProxyRegionSessionIntegrationTests {

  private static final boolean COPY_ON_READ = false;

  private static final int LOCATOR_PORT = 10334;

  private static final ClientRegionShortcut CLIENT_REGION_SHORTCUT = ClientRegionShortcut.PROXY;

  private static final String GEMFIRE_LOG_LEVEL = "config";
  private static final String LOCATOR_HOST = "localhost";

  private ClientCache clientCache;

  private Region<String, Session> sessions;

  @Before
  public void setupGemFireClient() {

    this.clientCache = new ClientCacheFactory(gemfireProperties())
      .addPoolLocator(LOCATOR_HOST, LOCATOR_PORT)
      .setPoolMaxConnections(-1)
      .setPoolPingInterval(1000)
      .setPoolSubscriptionEnabled(true)
      .create();

    this.clientCache.setCopyOnRead(COPY_ON_READ);

    this.sessions = this.clientCache.<String, Session>createClientRegionFactory(CLIENT_REGION_SHORTCUT)
      .setKeyConstraint(String.class)
      .setValueConstraint(Session.class)
      .create("Sessions");

    this.sessions.registerInterestForAllKeys();
  }

  private Properties gemfireProperties() {

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", "SessionClient");
    gemfireProperties.setProperty("log-level", GEMFIRE_LOG_LEVEL);

    return gemfireProperties;
  }

  @After
  public void closeClientCache() {

    Optional.ofNullable(this.clientCache)
      .ifPresent(ClientCache::close);
  }

  private Session getById(String id) {

    return Optional.ofNullable(this.sessions.get(id))
      .map(Session::commit)
      .map(Session::touch)
      .orElseThrow(() -> new IllegalStateException(String.format("No Session with ID [%s] was found", id)));
  }

  @SuppressWarnings({ "unchecked", "unused" })
  private Session queryById(String id) {

    QueryService queryService = this.clientCache.getQueryService();

    Query select = queryService.newQuery("SELECT * FROM /Sessions WHERE id = $1");

    try {

      Object result = select.execute(id);

      return Optional.ofNullable(result)
        .filter(SelectResults.class::isInstance)
        .map(SelectResults.class::cast)
        .map(it -> (Session) it.stream()
          .filter(Session.class::isInstance)
          .findFirst()
          .map(session -> ((Session) session).commit())
          .map(session -> ((Session) session).touch())
          .orElse(null))
        .orElse(null);
    }
    catch (Exception cause) {
      throw new RuntimeException(String.format("Session with ID [%s] not found", id));
    }
  }

  private boolean isDirty(Session session) {
    return session != null && session.hasDelta();
  }

  // TODO - Save the Session iff the Session is actually "dirty"; See GEODE-6032 (https://issues.apache.org/jira/browse/GEODE-6032)
  // TODO - Also remember/see GEODE-6099 (https://issues.apache.org/jira/browse/GEODE-6099)
  private Session safeSave(Session session) {
    return safeSave(session, WaitTask.doNotWait());
  }

  private Session safeSave(Session session, WaitTask waitTask) {

    if (isDirty(session)) {
      this.sessions.put(session.getId(), session);
      waitTask.doWait();
      session.commit();
    }

    return session;
  }

  // Susceptible to non-dirty Session saves
  private Session unsafeSave(Session session) {

    if (session != null) {
      this.sessions.put(session.getId(), session);
      session.commit();
    }

    return session;
  }

  // Test for GEM-2277
  // Test for GEODE-6152
  /**
   * @see <a href="https://jira-pivotal.atlassian.net/browse/GEM-2277">Java client with PROXY region may return the same object instance</a>
   * @see <a href="https://issues.apache.org/jira/browse/GEODE-6152">Client PROXY Regions may return the same Object instance on get(key) and cause Thread-safety issues</a>
   */
  @Test
  public void concurrentClientProxyRegionGetReturningSharedSessionInstanceIsNotThreadSafe() throws Throwable {
    TestFramework.runOnce(new ConcurrentClientProxyRegionGetReturnsSharedSessionInstanceTestCase());
  }

  // Simple Test for GEODE-6032

  /**
   * @see <a href="https://issues.apache.org/jira/browse/GEODE-6032">Entire object is serialized again (and again) when Delta.hasDelta() returns false and client is using PROXY Region</a>
   */
  @Test
  public void concurrentNonDirtySessionPutWithClientProxyRegionResultsInDataLoss() throws Throwable {
    TestFramework.runOnce(new ConcurrentNonDirtySessionPutWithClientProxyRegionTestCase());
  }

  // Complex Test for GEODE-6032
  /**
   * @see <a href="https://issues.apache.org/jira/browse/GEODE-6032">Entire object is serialized again (and again) when Delta.hasDelta() returns false and client is using PROXY Region</a>
   */
  @Test
  public void concurrentDirtySessionPutContainingNonDirtyObjectsUsingClientProxyRegionResultsInDataLoss()
      throws Throwable {

    TestFramework.runOnce(new ConcurrentDirtySessionPutContainingNonDirtyObjectsUsingClientProxyRegionTestCase());
  }

  @SuppressWarnings("unused")
  public final class ConcurrentClientProxyRegionGetReturnsSharedSessionInstanceTestCase extends MultithreadedTestCase {

    private final AtomicReference<String> sessionId = new AtomicReference<>();

    private final Set<Integer> sessionReferences = Collections.synchronizedSet(new HashSet<>(1));

    private void assertNonUniqueSessionReference(Session session) {
      assertThat(this.sessionReferences.add(System.identityHashCode(session))).isFalse();
    }

    private void assertUniqueSessionReference(Session session) {
      assertThat(this.sessionReferences.add(System.identityHashCode(session))).isTrue();
    }

    @Override
    public void initialize() {

      Instant beforeCreationTime = Instant.now();

      Session session = Session.create();

      assertThat(session).isNotNull();
      assertUniqueSessionReference(session);
      assertThat(session.getId()).isNotEmpty();
      assertThat(session.getCreationTime()).isAfterOrEqualTo(beforeCreationTime);
      assertThat(session.getCreationTime()).isBeforeOrEqualTo(Instant.now());
      assertThat(session.getLastAccessedTime()).isEqualTo(session.getCreationTime());
      assertThat(session.getAttributeNames()).isEmpty();

      session.setAttribute("A", 1);

      assertThat(session.getAttributeNames()).containsExactly("A");
      assertThat(session.getAttribute("A")).isEqualTo(1);
      assertThat(session.hasDelta()).isTrue();

      safeSave(session.touch());

      assertThat(session.hasDelta()).isFalse();
      assertThat(session.wasToDataCalled()).isTrue();
      assertThat(session.wasToDeltaCalled()).isTrue();

      this.sessionId.set(session.getId());
    }

    public void thread1() throws Exception {

      Thread.currentThread().setName("User Session One");

      assertTick(0);

      Instant beforeLastAccessedTime = Instant.now();

      Session session = getById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getLastAccessedTime()).isAfterOrEqualTo(beforeLastAccessedTime);
      assertThat(session.getAttributeNames()).containsExactly("A");
      assertThat(session.getAttribute("A")).isEqualTo(1);
      assertUniqueSessionReference(session);

      waitForTick(1);
      assertTick(1);

      session.setAttribute("B", 2);

      assertThat(session.getAttributeNames()).contains("A", "B");
      assertThat(session.getAttribute("B")).isEqualTo(2);
      assertThat(session.hasDelta()).isTrue();

      safeSave(session, () -> {
        waitForTick(3);
        assertTick(3);
      });

      assertThat(session.hasDelta()).isFalse();
      assertThat(session.wasToDataCalled()).isFalse();
      assertThat(session.wasToDeltaCalled()).isTrue();
    }

    public void thread2() throws Exception {

      Thread.currentThread().setName("User Session Two");

      assertTick(0);

      Instant beforeLastAccessedTime = Instant.now();

      Session session = getById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getLastAccessedTime()).isAfterOrEqualTo(beforeLastAccessedTime);
      assertThat(session.getAttributeNames()).containsExactly("A");
      assertThat(session.getAttribute("A")).isEqualTo(1);

      waitForTick(2);
      assertTick(2);

      assertNonUniqueSessionReference(session); // FAILS when copy-on-read is set to true

      session.setAttribute("C", 3);

      assertThat(session.getAttributeNames()).contains("A", "C");
      assertThat(session.getAttribute("C")).isEqualTo(3);
      assertThat(session.hasDelta()).isTrue();

      waitForTick(4);
      assertTick(4);

      assertThat(session.hasDelta()).isFalse(); // FAILS when copy-on-read is set to true

      safeSave(session);

      assertThat(session.hasDelta()).isFalse();
      assertThat(session.wasToDataCalled()).isFalse();
      assertThat(session.wasToDeltaCalled()).isFalse(); // FAILS when copy-on-read is set to true
    }

    @Override
    public void finish() {

      Instant beforeLastAccessedTime = Instant.now();

      Session session = getById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertUniqueSessionReference(session);
      assertThat(session.getLastAccessedTime()).isAfterOrEqualTo(beforeLastAccessedTime);
      assertThat(session.getAttributeNames()).containsOnly("A", "B", "C"); // FAILS when copy-on-read is set to false
      assertThat(session.getAttribute("A")).isEqualTo(1);
      assertThat(session.getAttribute("B")).isEqualTo(2);
      assertThat(session.getAttribute("C")).isEqualTo(3);
    }
  }

  @SuppressWarnings("unused")
  public final class ConcurrentNonDirtySessionPutWithClientProxyRegionTestCase extends MultithreadedTestCase {

    private final AtomicReference<String> sessionId = new AtomicReference<>(null);

    @Override
    public void initialize() {

      Instant beforeCreationTime = Instant.now();

      Session session = Session.create();

      assertThat(session).isNotNull();
      assertThat(session.getId()).isNotEmpty();
      assertThat(session.getCreationTime()).isAfterOrEqualTo(beforeCreationTime);
      assertThat(session.getCreationTime()).isBeforeOrEqualTo(Instant.now());
      assertThat(session.getLastAccessedTime()).isEqualTo(session.getCreationTime());
      assertThat(session.getAttributeNames()).isEmpty();

      session.setAttribute("A", 1);

      assertThat(session.getAttributeNames()).containsExactly("A");
      assertThat(session.getAttribute("A")).isEqualTo(1);
      assertThat(session.hasDelta()).isTrue();

      unsafeSave(session.touch());

      assertThat(session.hasDelta()).isFalse();
      assertThat(session.wasToDataCalled()).isTrue();
      assertThat(session.wasToDeltaCalled()).isTrue();

      this.sessionId.set(session.getId());
    }

    public void thread1() {

      Thread.currentThread().setName("User Session One");

      assertTick(0);

      Instant beforeLastAccessedTime = Instant.now();

      Session session = getById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getLastAccessedTime()).isAfterOrEqualTo(beforeLastAccessedTime);
      assertThat(session.getAttributeNames()).containsExactly("A");

      waitForTick(2);
      assertTick(2);

      session.setAttribute("B", 2);

      assertThat(session.getAttributeNames()).containsExactly("A", "B");
      assertThat(session.hasDelta()).isTrue();

      unsafeSave(session);
    }

    public void thread2() {

      Thread.currentThread().setName("User Session Two");

      waitForTick(1);
      assertTick(1);

      Instant beforeLastAccessedTime = Instant.now();

      Session session = getById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getLastAccessedTime()).isAfterOrEqualTo(beforeLastAccessedTime);
      assertThat(session.getAttributeNames()).containsExactly("A");

      // Because we will only "read" the Session in this Thread, and I want to simulate a highly concurrent context/env
      // where multiple Threads are accessing the same "logical" Session at the same time, let's assume nothing changed,
      // not even the lastAccessedTime timestamp, before we attempted to save (i.e. Region.put(session.getId(), session)
      // the Session again.
      session.commit();

      assertThat(session.hasDelta()).isFalse();

      waitForTick(3);
      assertTick(3);

      assertThat(session.getAttributeNames()).containsExactly("A");
      assertThat(session.hasDelta()).isFalse();

      unsafeSave(session);
    }

    @Override
    public void finish() {

      Instant beforeLastAccessedTime = Instant.now();

      Session session = getById(this.sessionId.get());

      assertThat(session).isNotNull();
      assertThat(session.getLastAccessedTime()).isAfterOrEqualTo(beforeLastAccessedTime);
      // FAILS since Delta.hasDelta() leads to full serialization of Session caused by thread2.
      assertThat(session.getAttributeNames()).containsOnly("A", "B");
      assertThat(session.getAttribute("A")).isEqualTo(1);
      assertThat(session.getAttribute("B")).isEqualTo(2);
    }
  }

  @SuppressWarnings("unused")
  public final class ConcurrentDirtySessionPutContainingNonDirtyObjectsUsingClientProxyRegionTestCase
      extends MultithreadedTestCase {

    private final AtomicReference<String> sessionId = new AtomicReference<>(null);

    private volatile Account jonDoesAccount = null;

    @Override
    public void initialize() {

      Instant beforeCreationTime = Instant.now();

      UninformedDeltaAwareSession session = UninformedDeltaAwareSession.create();

      assertThat(session).isNotNull();
      assertThat(session.getId()).isNotEmpty();
      assertThat(session.getCreationTime()).isAfterOrEqualTo(beforeCreationTime);
      assertThat(session.getCreationTime()).isBeforeOrEqualTo(Instant.now());
      assertThat(session.getLastAccessedTime()).isEqualTo(session.getCreationTime());
      assertThat(session.getAttributeNames()).isEmpty();

      this.jonDoesAccount = Account.of("JonDoesAccount").addPoints(50);

      session.setAttribute(jonDoesAccount.getId().toString(), jonDoesAccount);

      assertThat(session.getAttributeNames()).containsExactly(jonDoesAccount.getId().toString());
      assertThat(session.getAttribute(jonDoesAccount.getId().toString())).isEqualTo(jonDoesAccount);
      assertThat(session.hasDelta()).isTrue();

      unsafeSave(session);

      assertThat(session.hasDelta()).isFalse();

      this.sessionId.set(session.getId());
    }

    public void thread1() {

      Thread.currentThread().setName("User Session One");

      assertTick(0);

      Session session = getById(this.sessionId.get());

      assertThat(session).isInstanceOf(UninformedDeltaAwareSession.class);
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getAttributeNames()).containsExactly(this.jonDoesAccount.getId().toString());

      Account jonDoesAccountCopy = (Account) session.getAttribute(this.jonDoesAccount.getId().toString());

      assertThat(jonDoesAccountCopy).isEqualTo(this.jonDoesAccount);
      assertThat(jonDoesAccountCopy).isNotSameAs(this.jonDoesAccount);
      assertThat(jonDoesAccountCopy.getPoints()).isEqualTo(50);
      assertThat(jonDoesAccountCopy.hasDelta()).isFalse();

      waitForTick(2);
      assertTick(2);

      jonDoesAccountCopy.addPoints(10);

      assertThat(jonDoesAccountCopy.getPoints()).isEqualTo(60);
      assertThat(jonDoesAccountCopy.hasDelta()).isTrue();
      assertThat(session.hasDelta()).isTrue();

      unsafeSave(session);

      assertThat(jonDoesAccountCopy.hasDelta()).isFalse();
      assertThat(session.hasDelta()).isFalse();
    }

    // Bad Thread!
    public void thread2() {

      Thread.currentThread().setName("User Session Two");

      waitForTick(1);
      assertTick(1);

      Session session = getById(this.sessionId.get());

      assertThat(session).isInstanceOf(UninformedDeltaAwareSession.class);
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getAttributeNames()).containsExactly(this.jonDoesAccount.getId().toString());

      Account jonDoesAccountCopy = (Account) session.getAttribute(this.jonDoesAccount.getId().toString());

      assertThat(jonDoesAccountCopy).isEqualTo(this.jonDoesAccount);
      assertThat(jonDoesAccountCopy).isNotSameAs(this.jonDoesAccount);
      assertThat(jonDoesAccountCopy.getPoints()).isEqualTo(50);
      assertThat(jonDoesAccountCopy.hasDelta()).isFalse();

      waitForTick(4);
      assertTick(4);

      session.touch();

      assertThat(session.hasDelta()).isTrue();
      assertThat(jonDoesAccountCopy.hasDelta()).isFalse();

      unsafeSave(session);

      assertThat(session.hasDelta()).isFalse();
    }

    public void thread3() {

      Thread.currentThread().setName("User Session Three");

      waitForTick(3);
      assertTick(3);

      Session session = getById(this.sessionId.get());

      assertThat(session).isInstanceOf(UninformedDeltaAwareSession.class);
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getAttributeNames()).containsExactly(this.jonDoesAccount.getId().toString());

      Account jonDoesAccountCopy = (Account) session.getAttribute(this.jonDoesAccount.getId().toString());

      assertThat(jonDoesAccountCopy).isEqualTo(this.jonDoesAccount);
      assertThat(jonDoesAccountCopy).isNotSameAs(this.jonDoesAccount);
      assertThat(jonDoesAccountCopy.getPoints()).isEqualTo(60);
    }

    @Override
    public void finish() {

      Session session = getById(this.sessionId.get());

      assertThat(session).isInstanceOf(UninformedDeltaAwareSession.class);
      assertThat(session.getId()).isEqualTo(this.sessionId.get());
      assertThat(session.getAttributeNames()).containsExactly(this.jonDoesAccount.getId().toString());

      Account jonDoesAccountCopy = (Account) session.getAttribute(this.jonDoesAccount.getId().toString());

      assertThat(jonDoesAccountCopy).isEqualTo(this.jonDoesAccount);
      assertThat(jonDoesAccountCopy).isNotSameAs(this.jonDoesAccount);
      assertThat(jonDoesAccountCopy.getPoints()).isEqualTo(60); // FAILS!!!
    }
  }

  @FunctionalInterface
  interface WaitTask {

    static WaitTask doNotWait() {
      return () -> {};
    }

    void doWait();

  }

  public static class UninformedDeltaAwareSession extends Session {

    public static UninformedDeltaAwareSession create() {
      return new UninformedDeltaAwareSession();
    }

    @Override
    public synchronized boolean hasDelta() {

      return getAttributes().values().stream()
          .filter(Delta.class::isInstance)
          .map(Delta.class::cast)
          .anyMatch(Delta::hasDelta)
        || super.hasDelta();
    }

    @Override
    public synchronized void toDelta(DataOutput out) throws IOException {

      out.writeLong(this.getLastAccessedTime().toEpochMilli());

      // Try to send everything since the Session does not know what user application domain objects may or may not
      // have changed and "assume" GemFire/Geode only sends changes (i.e. the delta) iff there is indeed changes
      // (i.e. a delta) to the user's application domain objects.
      // NOTE: It also may very well be expensive to call Object.equals(:Object) on the users application domain objects
      // with the previous and new value.
      // NOTE: It is also potentially dangerous to use the users' application domain object equals method if 1) they
      // did not override Object.equals(:Object) or 2) their equals method only reflects "logical" identity
      // and not changes (reasonable).  So, assume users will implement o.a.g.Delta in their own application domain
      // objects to properly reflect changes and rely on Delta.hasDelta() as way to determine which application domain
      // objects have truly changed thereby letting GemFire/Geode sort'em out.  #2 is certainly the case with "Account"
      // since Lombok will implement Object.equals(:Object) with only accountId.
      // NOTE: This overridden toDelta(:DataOutput) method/version deliberately does not handle removes
      // (for the time being).
      for (Object attributeValue : getAttributes().values()) {
        DataSerializer.writeObject(attributeValue, out);
      }
    }

    @Override
    @SuppressWarnings("all")
    public synchronized void fromDelta(DataInput in) throws IOException, InvalidDeltaException {

      setLastAccessedTime(Instant.ofEpochMilli(in.readLong()));

      try {
        // Attempt to read until no more changes are received (signaled by the EOFException; Dumb? Yes, perhaps,
        // but ceraintly possible).
        while (true) {

          Object attributeValue = DataSerializer.readObject(in);

          Optional.ofNullable(attributeValue)
            .filter(Identifiable.class::isInstance)
            .map(Identifiable.class::cast)
            .ifPresent(it -> setAttribute(String.valueOf(it.getId()), it));
        }
      }
      catch (ClassNotFoundException cause) {
        throw new IOException("Failed to deserialize Session attribute value", cause);
      }
      catch (EOFException ignore) {
        // We are done!
      }
    }
  }

  @FunctionalInterface
  private interface Identifiable<T> {
    T getId();
  }

  @ToString(of = "accountId")
  @EqualsAndHashCode(onlyExplicitlyIncluded = true)
  @RequiredArgsConstructor(staticName = "of")
  private static class Account implements Identifiable<Object>, DataSerializable, Delta {

    @Getter
    private boolean delta;

    private AtomicInteger points = new AtomicInteger(0);

    @NonNull @EqualsAndHashCode.Include
    private Object accountId;

    // To satisfy GemFire/Geode #sigh
    @SuppressWarnings("unused")
    public Account() { }

    public void setId(Object id) {
      this.accountId = id;
    }

    @Override
    public Object getId() {
      return this.accountId;
    }

    public synchronized int getPoints() {
      return this.points.get();
    }

    public synchronized Account addPoints(int points) {

      this.points.addAndGet(points);
      this.delta |= points != 0;

      return this;
    }

    @Override
    public synchronized void toData(DataOutput out) throws IOException {

      DataSerializer.writeObject(getId(), out);
      out.writeInt(getPoints());

      this.delta = false;
    }

    @Override
    public synchronized void fromData(DataInput in) throws IOException, ClassNotFoundException {

      setId(DataSerializer.readObject(in));
      addPoints(in.readInt());

      this.delta = false;
    }

    @Override
    public synchronized boolean hasDelta() {
      return this.delta;
    }

    @Override
    public synchronized void toDelta(DataOutput out) throws IOException {

      out.writeInt(getPoints());

      this.delta = false;
    }

    @Override
    public synchronized void fromDelta(DataInput in) throws IOException, InvalidDeltaException {

      addPoints(in.readInt());

      this.delta = false;
    }
  }
}
