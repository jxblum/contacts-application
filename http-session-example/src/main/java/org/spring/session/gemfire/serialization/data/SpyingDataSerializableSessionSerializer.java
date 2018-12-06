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

package org.spring.session.gemfire.serialization.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spring.session.debugging.TraceUtils;
import org.spring.session.gemfire.SpyingGemFireOperationsSessionRepository.SpyingDeltaCapableGemFireSession;
import org.spring.session.gemfire.serialization.io.DataInputSpy;
import org.spring.session.gemfire.serialization.io.DataOutputSpy;
import org.spring.session.logging.SystemOutLogger;
import org.spring.session.time.TimeUtils;
import org.spring.session.utils.SessionUtils;
import org.springframework.session.data.gemfire.serialization.data.AbstractDataSerializableSessionSerializer;
import org.springframework.session.data.gemfire.serialization.data.provider.DataSerializableSessionAttributesSerializer;
import org.springframework.session.data.gemfire.serialization.data.provider.DataSerializableSessionSerializer;

/**
 * The SpyingDataSerializableSessionSerializer class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SpyingDataSerializableSessionSerializer
    extends AbstractDataSerializableSessionSerializer<SpyingDeltaCapableGemFireSession> {

  private final DataSerializableSessionSerializer sessionSerializer;

  public SpyingDataSerializableSessionSerializer() {
    this.sessionSerializer = new DataSerializableSessionSerializer();
    DataSerializableSessionAttributesSerializer.register();
  }

  @Override
  public int getId() {
    return 1593875959;
  }

  @Override
  public Class<?>[] getSupportedClasses() {

    List<Class<?>> supportedClasses = new ArrayList<>(Arrays.asList(this.sessionSerializer.getSupportedClasses()));

    supportedClasses.add(SpyingDeltaCapableGemFireSession.class);

    return supportedClasses.toArray(new Class<?>[supportedClasses.size()]);
  }

  @Override
  public void serialize(SpyingDeltaCapableGemFireSession session, DataOutput out) {

    boolean hasDelta = session.hasDelta();

    DataOutputSpy spy = DataOutputSpy.from(out);

    String traceIdentifier = TraceUtils.resolveRequestTraceIdentifier();

    safeWrite(out, it -> it.writeUTF(traceIdentifier));

    this.sessionSerializer.serialize(session, spy);

    logSerialization(session, traceIdentifier, hasDelta, spy);
  }

  @Override
  public SpyingDeltaCapableGemFireSession deserialize(DataInput in) {

    DataInputSpy spy = DataInputSpy.from(in);

    String traceIdentifier = safeRead(in, DataInput::readUTF);

    SpyingDeltaCapableGemFireSession session =
      SpyingDeltaCapableGemFireSession.of(this.sessionSerializer.deserialize(spy));

    logDeserialization(session, traceIdentifier, spy);

    return session;
  }

  private void logSerialization(SpyingDeltaCapableGemFireSession session, String traceIdentifier,
      boolean hasDelta, DataOutputSpy out) {

    log(session, traceIdentifier,"Serialized", hasDelta, "Writes", out.getWrites().toString());
  }

  private void logDeserialization(SpyingDeltaCapableGemFireSession session, String traceIdentifier, DataInputSpy in) {
    log(session, traceIdentifier,"Deserialized", session.hasDelta(),"Reads", in.getReads().toString());
  }

  private void log(SpyingDeltaCapableGemFireSession session, String traceIdentifier, String operation,
      boolean hasDelta, String label, String value) {

    String message = "[%s; id-%s] %s with Session {id = %s, delta = %s, lastAccessedTime = %s}"
      + "\n\tand Attributes [%s]"
      + "\n\tincludes %s [%s]%n";

    SystemOutLogger.log(message, TimeUtils.formattedTimestamp(), traceIdentifier, operation, session.getId(),
      hasDelta, session.getLastAccessedTime().toEpochMilli(), SessionUtils.getAttributeNames(session), label, value);

    TraceUtils.whenTracingEnabled(stackTrace -> SystemOutLogger.log("%s", stackTrace));
  }
}
