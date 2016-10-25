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

package example.app.geode.security.repository.support;

import static example.app.geode.security.model.Role.newRole;
import static example.app.geode.security.model.User.newUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.geode.security.ResourcePermission;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import example.app.geode.security.model.Role;
import example.app.geode.security.model.User;
import example.app.geode.security.repository.CachingSecurityRepository;

/**
 * The {@link XmlSecurityRepository} class...
 *
 * @author John Blum
 * @see org.springframework.beans.factory.InitializingBean
 * @see example.app.geode.security.model.Role
 * @see example.app.geode.security.model.User
 * @see example.app.geode.security.repository.CachingSecurityRepository
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class XmlSecurityRepository extends CachingSecurityRepository<User> implements InitializingBean {

  protected static final String ROLES_PERMISSIONS_XML = "roles-permissions.xml";
  protected static final String USERS_ROLES_XML = "users-roles.xml";

  private final Resource rolesPermissions;
  private final Resource usersRoles;

  /* (non-Javadoc) */
  public XmlSecurityRepository() {
    this(new ClassPathResource(ROLES_PERMISSIONS_XML), new ClassPathResource(USERS_ROLES_XML));
  }

  /* (non-Javadoc) */
  public XmlSecurityRepository(Resource rolesPermissions, Resource usersRoles) {
    Assert.notNull(rolesPermissions, "The rolesPermissions Resource cannot be null");
    Assert.notNull(usersRoles, "The usersRoles Resource cannot be null");

    this.rolesPermissions = rolesPermissions;
    this.usersRoles = usersRoles;
  }

  /* (non-Javadoc) */
  protected Resource getRolesPermissions() {
    return this.rolesPermissions;
  }

  /* (non-Javadoc) */
  protected Resource getUsersRoles() {
    return this.usersRoles;
  }

  /**
   * @inheritDoc
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    saveAll(parseUsersRoles(getUsersRoles(), parseRolesPermissions(getRolesPermissions())));
  }

  /* (non-Javadoc) */
  protected Map<String, Role> parseRolesPermissions(Resource rolesPermissions) throws Exception {
    Document document = new SAXBuilder().build(rolesPermissions.getInputStream());
    Element rolesElement = document.getRootElement();

    Map<String, Role> roleNameMapping = new HashMap<>(rolesElement.getChildren().size());

    for (Element roleElement : rolesElement.getChildren()) {
      Role role = newRole(roleElement.getAttributeValue("name"));

      for (Element permissionElement : roleElement.getChild("permissions").getChildren()) {
        role.with(new ResourcePermission(permissionElement.getAttributeValue("resource"),
          permissionElement.getAttributeValue("operation"), permissionElement.getAttributeValue("region"),
            permissionElement.getAttributeValue("key")));
      }

      roleNameMapping.put(role.getName(), role);
    }

    return roleNameMapping;
  }

  /* (non-Javadoc) */
  protected List<User> parseUsersRoles(Resource usersRoles, Map<String, Role> roles) throws Exception {
    Document document = new SAXBuilder().build(usersRoles.getInputStream());
    Element usersElement = document.getRootElement();

    List<User> users = new ArrayList<>(usersElement.getChildren().size());

    for (Element userElement : usersElement.getChildren()) {
      User user = newUser(userElement.getAttributeValue("name")).with(userElement.getAttributeValue("password"));

      for (Element roleElement : userElement.getChild("roles").getChildren()) {
        user.in(roles.get(roleElement.getAttributeValue("name")));
      }

      users.add(user);
    }

    return users;
  }
}
