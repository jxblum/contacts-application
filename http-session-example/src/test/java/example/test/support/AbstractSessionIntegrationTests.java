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

package example.test.support;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.util.Assert;

/**
 * Abstract base class providing support and functionality common to all Spring Session Integration Tests
 *
 * @author John Blum
 * @see org.springframework.session.Session
 * @see org.springframework.session.SessionRepository
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractSessionIntegrationTests {

  @Autowired
  private SessionRepository sessionRepository;

  protected SessionRepository getSessionRepository() {

    Assert.state(this.sessionRepository != null,
      "SessionRepository was not properly configured");

    return this.sessionRepository;
  }

  protected Session newSession() {
    return this.sessionRepository.createSession();
  }

  protected Session findById(String id) {
    return this.sessionRepository.findById(id);
  }

  protected Session forcedTouch(Session session) {

    session.setLastAccessedTime(session.getLastAccessedTime().plusMillis(1));

    return session;
  }

  @SuppressWarnings("unchecked")
  protected Session save(Session session) {

    this.sessionRepository.save(session);

    return session;
  }

  protected Session touch(Session session) {

    session.setLastAccessedTime(Instant.now());

    return session;
  }
}
