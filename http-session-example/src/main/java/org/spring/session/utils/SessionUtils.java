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

package org.spring.session.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpSession;

import org.springframework.session.Session;
import org.springframework.session.data.gemfire.support.AbstractSession;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * The SessionUtils class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class SessionUtils {

  public static List<String> getAttributeNames(HttpSession session) {
    return getAttributeNames(ReadOnlySessionAdapter.from(session));
  }

  public static List<String> getAttributeNames(Session session) {

    return session.getAttributeNames().stream()
      .sorted()
      .collect(Collectors.toList());
  }

  public static class ReadOnlySessionAdapter extends AbstractSession {

    public static ReadOnlySessionAdapter from(HttpSession session) {
      return new ReadOnlySessionAdapter(session);
    }

    private final HttpSession session;

    private ReadOnlySessionAdapter(HttpSession session) {

      Assert.notNull(session, "HttpSession is required");

      this.session = session;
    }

    @Override
    public String getId() {
      return this.session.getId();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attributeName) {
      return (T) this.session.getAttribute(attributeName);
    }

    @Override
    public Set<String> getAttributeNames() {
      return toSet(this.session.getAttributeNames());
    }

    @Override
    public Instant getCreationTime() {
      return Instant.ofEpochMilli(this.session.getCreationTime());
    }

    @Override
    public Instant getLastAccessedTime() {
      return Instant.ofEpochMilli(this.session.getLastAccessedTime());
    }

    @Override
    public Duration getMaxInactiveInterval() {
      return Duration.ofSeconds(this.session.getMaxInactiveInterval());
    }

    private <T> Set<T> toSet(Enumeration<T> enumeration) {

      return StreamSupport.stream(toIterable(enumeration).spliterator(), false)
        .collect(Collectors.toSet());
    }

    private <T> Iterable<T> toIterable(Enumeration<T> enumeration) {

      return () -> Optional.ofNullable(enumeration)
        .map(CollectionUtils::toIterator)
        .orElseGet(Collections::emptyIterator);
    }
  }
}
