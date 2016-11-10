/*
 * Copyright 2016 the original author or authors.
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

package example.app.geode.security.repository;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cp.elements.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import example.app.geode.security.model.User;

/**
 * The {@link CachingSecurityRepository} class caches Security Configuration Meta-Data and is meant to be extended
 * by classes that are data store specified (e.g. JDBC/RDBMS, LDAP, etc).
 *
 * @author John Blum
 * @see example.app.geode.security.model.User
 * @see example.app.geode.security.repository.SecurityRepository
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class CachingSecurityRepository<T extends User> implements SecurityRepository<T> {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected final Map<String, T> users = new ConcurrentHashMap<>();

  /**
   * @inheritDoc
   */
  @Override
  public Iterable<T> findAll() {
    return Collections.unmodifiableCollection(users.values());
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean delete(T user) {
    // NOTE the following compound operations on the Map are not meant to be atomic
    users.remove(user.getName());
    return users.containsKey(user.getName());
  }

  /**
   * @inheritDoc
   */
  @Override
  public T save(T user) {
    Assert.notNull(user, "User must not be null");
    users.put(user.getName(), user);
    return user;
  }
}
