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

import static example.app.geode.security.model.User.newUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import example.app.geode.security.model.Role;
import example.app.geode.security.model.User;

/**
 * The {@link SecurityRepository} interface is a contract for Data Access Objects (DAO) implementing this interface
 * to perform CRUD and query operations on {@link User} information, pertinent to the security of the system.
 *
 * @author John Blum
 * @see example.app.geode.security.model.Role
 * @see example.app.geode.security.model.User
 * @see org.springframework.stereotype.Repository
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public interface SecurityRepository<T extends User> {

  /**
   * Finds all {@link User Users} of the system.
   *
   * @return an {@link Iterable} of {@link User Users} of the system.
   * @see example.app.geode.security.model.User
   * @see java.lang.Iterable
   */
  Iterable<T> findAll();

  /**
   * Deletes the given {@link User} from the system.
   *
   * @param user {@link User} to delete.
   * @return a boolean value indicating if the {@link User} was deleted successfully.
   * @see example.app.geode.security.model.User
   */
  boolean delete(T user);

  /**
   * Records (persist) the information (state) of the {@link User}.
   *
   * @param user {@link User} to store.
   * @return the {@link User}.
   * @see example.app.geode.security.model.User
   */
  T save(T user);

  /* (non-Javadoc) */
  default int count() {
    Iterable<T> users = findAll();

    int size = (users instanceof Collection ? ((Collection) users).size()
      : (users instanceof Map) ? ((Map) users).size() : -1);

    if (size == -1) {
      size = 0;

      for (T user : users) {
        size++;
      }
    }

    return size;
  }

  /* (non-Javadoc) */
  @SuppressWarnings("unchecked")
  default T create(String username, Role... roles) {
    return save((T) newUser(username).in(roles));
  }

  /* (non-Javadoc) */
  default boolean delete(String username) {
    return delete(findBy(username));
  }

  /* (non-Javadoc) */
  default boolean deleteAll() {
    return deleteAll(findAll());
  }

  /* (non-Javadoc) */
  default boolean deleteAll(String... usernames) {
    return deleteAll(findAll(usernames));
  }

  /* (non-Javadoc) */
  @SuppressWarnings("all")
  default boolean deleteAll(T... users) {
    return deleteAll(Arrays.asList(users));
  }

  /* (non-Javadoc) */
  default boolean deleteAll(Iterable<T> users) {
    boolean result = true;

    for (T user : users) {
      result &= delete(user);
    }

    return result;
  }

  /* (non-Javadoc) */
  default boolean exists(String username) {
    return (findBy(username) != null);
  }

  /* (non-Javadoc) */
  default Iterable<T> findAll(String... usernames) {
    return findAll(Arrays.asList(usernames));
  }

  /* (non-Javadoc) */
  default Iterable<T> findAll(Iterable<String> usernames) {
    List<T> users = new ArrayList<>();

    for (String username : usernames) {
      users.add(findBy(username));
    }

    return users;
  }

  /* (non-Javadoc) */
  default T findBy(String username) {
    for (T user : findAll()) {
      if (user.getName().equals(username)) {
        return user;
      }
    }

    return null;
  }

  /* (non-Javadoc) */
  @SuppressWarnings("all")
  default T[] saveAll(T... users) {
    saveAll(Arrays.asList(users));
    return users;
  }

  /* (non-Javadoc) */
  default Iterable<T> saveAll(Iterable<T> users) {
    for (T user : users) {
      save(user);
    }

    return users;
  }
}
