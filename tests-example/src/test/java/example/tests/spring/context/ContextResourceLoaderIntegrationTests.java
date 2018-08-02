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

package example.tests.spring.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration test testing the 'context:' ResourceLoader prefix/identifier.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see <a href="https://stackoverflow.com/questions/51620460/pivotal-gemfire-cannot-see-cached-data-in-gfsh-or-pulse">Pivotal GemFire cannot see cache data in Gfsh or Pulse</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ContextResourceLoaderIntegrationTests {

  @Resource(name = "applicationProperties")
  private Properties testProperties;

  @Test
  public void expectedApplicationPropertiesArePresent() {

    assertThat(this.testProperties).isNotNull();
    assertThat(this.testProperties.size()).isGreaterThanOrEqualTo(2);
    assertThat(this.testProperties).containsKeys("name", "log-level");
    assertThat(this.testProperties.get("name")).isEqualTo("TestApplication");
    assertThat(this.testProperties.get("log-level")).isEqualTo("trace");
  }
}
