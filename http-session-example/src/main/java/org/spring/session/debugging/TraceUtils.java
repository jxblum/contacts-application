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

package org.spring.session.debugging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.spring.session.utils.RequestUtils;

/**
 * The TraceUtils class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class TraceUtils {

  private static final AtomicBoolean tracingEnabled = new AtomicBoolean(false);

  public static String getStackTrace() {

    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    List<StackTraceElement> resolvedStackTrace = Arrays.stream(stackTrace)
      .filter(stackTraceElementFilter())
      .collect(Collectors.toList());

    StringWriter writer = new StringWriter();

    Throwable throwable = new Throwable("STACK TRACE DUMP");

    throwable.setStackTrace(resolvedStackTrace.toArray(new StackTraceElement[resolvedStackTrace.size()]));
    throwable.printStackTrace(new PrintWriter(writer));

    return writer.toString();
  }

  private static Predicate<StackTraceElement> stackTraceElementFilter() {
    return element -> !TraceUtils.class.getName().equals(element.getClassName());
  }

  public static boolean isTracingEnabled() {
    return tracingEnabled.get();
  }

  public static String resolveRequestTraceIdentifier() {

    return String.format("%s/%s",
      Optional.ofNullable(RequestUtils.resolveServerIdentifier()).orElse(resolveUniversalTraceIdentifier()),
      Optional.ofNullable(RequestUtils.resolveRequestIdentifier()).orElse("*"));
  }

  public static String resolveUniversalTraceIdentifier() {

    String id = UUID.randomUUID().toString();
    String[] idElements = id.split("-");

    return String.format("%s%s%s", idElements[0].substring(0, 3), idElements[3],
      idElements[idElements.length - 1].substring(idElements[idElements.length - 1].length() - 3));
  }

  public static void whenTracingEnabled(Consumer<String> stackTraceConsumer) {

    if (isTracingEnabled()) {
      stackTraceConsumer.accept(getStackTrace());
    }
  }

  public void withTracing() {
    tracingEnabled.set(true);
  }

  public void withoutTracing() {
    tracingEnabled.set(false);
  }
}
