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

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import example.app.geode.security.repository.SecurityRepository;

/**
 * Integrate tests for {@link XmlSecurityRepository}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.geode.security.repository.SecurityRepository
 * @see example.app.geode.security.repository.support.AbstractSecurityRepositoryIntegrationTests
 * @see example.app.geode.security.repository.support.XmlSecurityRepository
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class XmlSecurityRepositoryIntegrationTests extends AbstractSecurityRepositoryIntegrationTests {

  @Autowired
  private XmlSecurityRepository securityRepository;

  /**
   * @inheritDoc
   */
  @Override
  @SuppressWarnings("unchecked")
  protected <T extends SecurityRepository> T getSecurityRepository() {
    Assert.state(this.securityRepository != null, "XmlSecurityRepository was not properly configured");
    return (T) this.securityRepository;
  }

  @Configuration
  static class XmlSecurityRepositoryConfiguration {

    @Bean
    XmlSecurityRepository securityRepository() {
      return new XmlSecurityRepository();
    }
  }
}
