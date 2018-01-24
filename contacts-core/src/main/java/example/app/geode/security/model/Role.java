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

import static org.cp.elements.util.ArrayUtils.asArray;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.geode.security.ResourcePermission;
import org.cp.elements.lang.Identifiable;
import org.springframework.util.Assert;

/**
 * The {@link Role} class is an Abstract Data Type (ADT) modeling a role of a user (e.g. Admin).
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see java.lang.Comparable
 * @see java.lang.Iterable
 * @see org.apache.geode.security.ResourcePermission
 * @see org.cp.elements.lang.Identifiable
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Role implements Comparable<Role>, Identifiable<String>, Iterable<ResourcePermission>, Serializable {

  private final String name;

  private Set<ResourcePermission> permissions = new HashSet<>();

  /**
   * Factory method used to construct a new instance of {@link Role} initialized with the given name.
   *
   * @param name {@link String} indicating the name of the new {@link Role}.
   * @return a new {@link Role} initialized with the given name.
   * @see example.app.geode.security.model.Role
   */
  public static Role newRole(String name) {
    return new Role(name);
  }

  /**
   * Constructs an instance of {@link Role} with the given name.
   *
   * @param name {@link String} name of this {@link Role}.
   * @throws IllegalArgumentException if name was not specified.
   */
  public Role(String name) {
    Assert.hasText(name, "Name must be specified");
    this.name = name;
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
   * Returns the name of this {@link Role}.
   *
   * @return a {@link String} indicating the name of this {@link Role}.
   */
  public String getName() {
    return name;
  }

  /**
   * @inheritDoc
   */
  @Override
  @SuppressWarnings("all")
  public int compareTo(Role role) {
    return this.getName().compareTo(role.getName());
  }

  /**
   * @inheritDoc
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof Role)) {
      return false;
    }

    Role that = (Role) obj;

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
   * Determines whether this {@link Role} has been assigned (granted) the given {@link ResourcePermission permission}.
   *
   * @param permission {@link ResourcePermission} to evaluate.
   * @return a boolean value indicating whether this {@link Role} has been assigned (granted)
   * the given {@link ResourcePermission permission}.
   * @see org.apache.geode.security.ResourcePermission
   */
  public boolean hasPermission(ResourcePermission permission) {
    return this.permissions.contains(permission);
  }

  /**
   * @inheritDoc
   */
  @Override
  public Iterator<ResourcePermission> iterator() {
    return Collections.unmodifiableSet(this.permissions).iterator();
  }

  /**
   * @inheritDoc
   */
  @Override
  public String toString() {
    return getName();
  }

  /**
   * Adds (assigns/grants) all given {@link ResourcePermission persmissions} to this {@link Role}.
   *
   * @param permissions {@link ResourcePermission}s to assign/grant to this {@link Role}.
   * @return this {@link Role}.
   * @see org.apache.geode.security.ResourcePermission
   */
  public Role with(ResourcePermission... permissions) {
    Collections.addAll(this.permissions, permissions);
    return this;
  }

  /**
   * Adds (assigns/grants) all given {@link ResourcePermission persmissions} to this {@link Role}.
   *
   * @param permissions {@link ResourcePermission}s to assign/grant to this {@link Role}.
   * @return this {@link Role}.
   * @see org.apache.geode.security.ResourcePermission
   */
  public Role with(Iterable<ResourcePermission> permissions) {
    return with(asArray(permissions, ResourcePermission.class));
  }
}
