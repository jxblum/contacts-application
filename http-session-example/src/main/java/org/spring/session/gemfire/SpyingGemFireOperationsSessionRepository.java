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

package org.spring.session.gemfire;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.time.Duration;

import org.apache.shiro.util.Assert;
import org.spring.session.debugging.TraceUtils;
import org.spring.session.gemfire.serialization.io.DataInputSpy;
import org.spring.session.gemfire.serialization.io.DataOutputSpy;
import org.spring.session.logging.SystemOutLogger;
import org.spring.session.time.TimeUtils;
import org.spring.session.utils.SessionUtils;
import org.springframework.data.gemfire.GemfireOperations;
import org.springframework.session.Session;
import org.springframework.session.data.gemfire.GemFireOperationsSessionRepository;
import org.springframework.util.ObjectUtils;

/**
 * The SpyingGemFireOperationsSessionRepository class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SpyingGemFireOperationsSessionRepository extends GemFireOperationsSessionRepository {

  public SpyingGemFireOperationsSessionRepository(GemfireOperations gemfireOperations) {
    super(gemfireOperations);
  }

  @Override
  public Session createSession() {
    return SpyingDeltaCapableGemFireSession.newSession(getMaxInactiveInterval());
  }

  // Test/Debug version of the DeltaCapableGemFireSession
  public static class SpyingDeltaCapableGemFireSession extends DeltaCapableGemFireSession {

    public static SpyingDeltaCapableGemFireSession newSession(Duration maxInactiveInterval) {

      SpyingDeltaCapableGemFireSession session = new SpyingDeltaCapableGemFireSession();

      session.setMaxInactiveInterval(maxInactiveInterval);

      return session;
    }

    public static SpyingDeltaCapableGemFireSession of(GemFireSession<?> session) {

      Assert.isInstanceOf(DeltaCapableGemFireSession.class, session,
        String.format("Session [%s] must be a DeltaCapableGemFireSession", ObjectUtils.nullSafeClassName(session)));

      SpyingDeltaCapableGemFireSession sessionCopy = new SpyingDeltaCapableGemFireSession(session);

      sessionCopy.commit();
      sessionCopy.triggerDelta(session.hasDelta());

      return sessionCopy;
    }

    public SpyingDeltaCapableGemFireSession() { }

    public SpyingDeltaCapableGemFireSession(String id) {
      super(id);
    }

    public SpyingDeltaCapableGemFireSession(Session session) {
      super(session);
    }

    @Override
    public synchronized void toDelta(DataOutput out) throws IOException {

      boolean hasDelta = hasDelta();

      DataOutputSpy spy = DataOutputSpy.from(out);

      String traceIdentifier = TraceUtils.resolveRequestTraceIdentifier();

      out.writeUTF(traceIdentifier);

      super.toDelta(spy);

      logToDelta(traceIdentifier, hasDelta, spy);
    }

    @Override
    public synchronized void fromDelta(DataInput in) throws IOException {

      DataInputSpy spy = DataInputSpy.from(in);

      String traceIdentifier = in.readUTF();

      super.fromDelta(spy);

      logFromDelta(traceIdentifier, spy);
    }

    private void logToDelta(String traceIdentifier, boolean hasDelta, DataOutputSpy out) {
      log(traceIdentifier,"ToDelta", hasDelta,"Writes", out.getWrites().toString());
    }

    private void logFromDelta(String traceIdentifier, DataInputSpy in) {
      log(traceIdentifier,"FromDelta", hasDelta(),"Reads", in.getReads().toString());
    }

    private void log(String traceIdentifier, String operation, boolean hasDelta, String label, String value) {

      String message = "[%s; id-%s] %s with Session {id = %s, delta = %s, lastAccessedTime = %s}"
        + "\n\tand Attributes [%s]"
        + "\n\tincludes %s [%s]%n";

      SystemOutLogger.log(message, TimeUtils.formattedTimestamp(), traceIdentifier, operation, getId(), hasDelta,
        getLastAccessedTime().toEpochMilli(), SessionUtils.getAttributeNames(this), label, value);

      TraceUtils.whenTracingEnabled(stackTrace -> SystemOutLogger.log("%s", stackTrace));
    }
  }
}
