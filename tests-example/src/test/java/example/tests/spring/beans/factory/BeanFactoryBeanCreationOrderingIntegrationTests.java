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
package example.tests.spring.beans.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

/**
 * Integration Test (falsely assuming &) asserting the bean creation order by the Spring Container.
 *
 * The test case generally passes, unless you explicitly change the bean definition ordering declared/listed in XML,
 * such as from:
 *
 * <code>
 *   <bean id="A" class="example.tests.spring.beans.factory.BeanFactoryBeanCreationOrderingIntegrationTests.TestBean" c:name="A"/>
 *   <bean id="B" class="example.tests.spring.beans.factory.BeanFactoryBeanCreationOrderingIntegrationTests.TestBean" c:name="B"/>
 *   <bean id="C" class="example.tests.spring.beans.factory.BeanFactoryBeanCreationOrderingIntegrationTests.TestBean" c:name="C"/>
 * </code>
 *
 * To:
 *
 * <code>
 *   <bean id="B" class="example.tests.spring.beans.factory.BeanFactoryBeanCreationOrderingIntegrationTests.TestBean" c:name="B"/>
 *   <bean id="A" class="example.tests.spring.beans.factory.BeanFactoryBeanCreationOrderingIntegrationTests.TestBean" c:name="A"/>
 *   <bean id="C" class="example.tests.spring.beans.factory.BeanFactoryBeanCreationOrderingIntegrationTests.TestBean" c:name="C"/>
 * </code>
 *
 * But, this should not be taken as guaranteed.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.test.annotation.Repeat
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
public class BeanFactoryBeanCreationOrderingIntegrationTests {

  private static final List<String> beanNames = new CopyOnWriteArrayList<>();

  @Test
  @Repeat(100)
  public void beanCreationOrder() {
    assertThat(beanNames).containsExactly("A", "B", "C");
  }

  public static class TestBean {

    private final String name;

    public TestBean(String name) {

      Assert.hasText(name, "Name is required");

      this.name = name;
      beanNames.add(name);
    }

    @Override
    public String toString() {
      return super.toString();
    }
  }
}
