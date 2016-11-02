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

package example.app.geode.security.model;

import static example.app.core.util.ArrayUtils.toArray;

import java.io.Serializable;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.geode.security.ResourcePermission;
import org.cp.elements.lang.Identifiable;
import org.springframework.util.Assert;

/**
 * The {@link User} class is an Abstract Data Type (ADT) modeling a user of a system.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see java.lang.Comparable
 * @see java.lang.Cloneable
 * @see java.lang.Iterable
 * @see java.security.Principal
 * @see org.apache.geode.security.ResourcePermission
 * @see org.cp.elements.lang.Identifiable
 * @see example.app.geode.security.model.Role
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class User implements Comparable<User>, Cloneable, Identifiable<String>, Iterable<Role>,
    Principal, Serializable {

  private String credentials;

  private final String name;

  private final Set<Role> roles = new HashSet<>();

  /**
   * Factory method to construct a new {@link User} initialized with the given username.
   *
   * @param username {@link String} indicating the name of the {@link User}.
   * @return a new {@link User} initialized with the given username.
   * @throws IllegalArgumentException if the username was not specified.
   * @see example.app.geode.security.model.User
   */
  public static User newUser(String username) {
    return new User(username);
  }

  /**
   * Constructs an instance of {@link User} initialized with the given username.
   *
   * @param name {@link String} indicating the name of the {@link User}.
   * @throws IllegalArgumentException if the username was not specified.
   */
  public User(String name) {
    Assert.hasText(name, "Username must be specified");
    this.name = name;
  }

  /**
   * Returns this {@link User User's} credentials (e.g. password).
   *
   * @return a {@link String} containing this {@link User User's} credentials.
   */
  public String getCredentials() {
    return credentials;
  }

  /**
   * @inheritDoc
   */
  @Override
  public String getId() {
    return getName();
  }

  /**
   * @inheritDoc
   */
  @Override
  public void setId(String id) {
    throw new UnsupportedOperationException("Operation Not Supported");
  }

  /**
   * @inheritDoc
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * @inheritDoc
   */
  @Override
  @SuppressWarnings("all")
  protected Object clone() throws CloneNotSupportedException {
    return newUser(getName()).with(getCredentials())
      .in(this.roles.toArray(new Role[this.roles.size()]));
  }

  /**
   * @inheritDoc
   */
  @Override
  @SuppressWarnings("all")
  public int compareTo(User user) {
    return this.getName().compareTo(user.getName());
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof User)) {
      return false;
    }

    User that = (User) obj;

    return this.getName().equals(that.getName());
  }

  /**
   * @inheritDoc
   */
  @Override
  public int hashCode() {
    int hashValue = 17;
    hashValue = 37 * hashValue + getName().hashCode();
    return hashValue;
  }

  /**
   * Determines whether this {@link User} has been granted (assigned) the given {@link ResourcePermission permission}.
   *
   * @param permission {@link ResourcePermission} to evalute.
   * @return a boolean value indicating whether this {@link User} has been granted (assigned)
   * the given {@link ResourcePermission}.
   * @see example.app.geode.security.model.Role#hasPermission(ResourcePermission)
   * @see org.apache.geode.security.ResourcePermission
   */
  public boolean hasPermission(ResourcePermission permission) {
    for (Role role : this) {
      if (role.hasPermission(permission)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether this {@link User} has the specified {@link Role}.
   *
   * @param role {@link Role} to evaluate.
   * @return a boolean value indicating whether this {@link User} has the specified {@link Role}.
   * @see example.app.geode.security.model.Role
   */
  public boolean hasRole(Role role) {
    return this.roles.contains(role);
  }

  /**
   * @inheritDoc
   */
  @Override
  public Iterator<Role> iterator() {
    return Collections.unmodifiableSet(this.roles).iterator();
  }

  /**
   * @inheritDoc
   */
  @Override
  public String toString() {
    return getName();
  }

  /**
   * Adds the array of {@link Role Roles} granting (resource) permissions to this {@link User}.
   *
   * @param roles array of {@link Role Roles} granting (resource) permissions to this {@link User}.
   * @return this {@link User}.
   * @see example.app.geode.security.model.Role
   */
  public User in(Role... roles) {
    Collections.addAll(this.roles, roles);
    return this;
  }

  /**
   * Adds the array of {@link Role Roles} granting (resource) permissions to this {@link User}.
   *
   * @param roles array of {@link Role Roles} granting (resource) permissions to this {@link User}.
   * @return this {@link User}.
   * @see example.app.geode.security.model.Role
   */
  public User in(Iterable<Role> roles) {
    return in(toArray(roles));
  }

  /**
   * Sets this {@link User User's} credentials (e.g. password) to the given value.
   *
   * @param credentials {@link String} containing this {@link User User's} credentials (e.g. password).
   * @return this {@link User}.
   * @see example.app.geode.security.model.User
   */
  public User with(String credentials) {
    this.credentials = credentials;
    return this;
  }
}
