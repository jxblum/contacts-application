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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Optional;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Integration tests testing the construction and destruction of a Spring {@link ConfigurableApplicationContext}
 * instance per test case method.
 *
 * This test class is in direct response to SDG CI build failure @
 * <a href="https://build.spring.io/browse/SGF-SDGEODE-826">Upgrade to Spring Framework 5.1.0.RC1</a>
 * to test the proper behavior of the Spring container as of the {@literal 5.1.0.RC1} release.
 *
 * Also see <a href="https://groups.google.com/a/pivotal.io/d/msg/spring-developer/qP9nkkbM4gw/TBdldoPsDgAJ">@EventListener on org.springframework classes</a>.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see org.springframework.beans.factory.config.BeanFactoryPostProcessor
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 * @see org.springframework.context.ConfigurableApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @since 1.0.0
 */
@SuppressWarnings("unused")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiAnnotationConfigApplicationContextConstructingIntegrationTests {

  private ConfigurableApplicationContext applicationContext;

  @After
  public void tearDown() {
    Optional.ofNullable(this.applicationContext).ifPresent(ConfigurableApplicationContext::close);
  }

  private ConfigurableApplicationContext newApplicationContext(Class<?>... annotatedClasses) {
    return new AnnotationConfigApplicationContext(annotatedClasses);
  }

  @Test
  public void firstTestMockObjects() {

    this.applicationContext = newApplicationContext(MockTestConfiguration.class);

    assertThat(this.applicationContext).isNotNull();
    assertThat(this.applicationContext.containsBean("dependentObject")).isTrue();

    Collaborator mockCollaborator = this.applicationContext.getBean("MockCollaborator", Collaborator.class);

    assertThat(mockCollaborator).isNotNull();
    assertThat(mockCollaborator.getName()).isEqualTo("mock");

    DependentObject dependentObject =
      this.applicationContext.getBean("dependentObject", DependentObject.class);

    assertThat(dependentObject).isNotNull();
    assertThat(dependentObject.getCollaborator().orElse(null)).isEqualTo(mockCollaborator);
  }

  @Test
  public void thenTestRealObjects() {

    this.applicationContext = newApplicationContext(RealTestConfiguration.class);

    assertThat(this.applicationContext).isNotNull();
    assertThat(this.applicationContext.containsBean("dependentObject")).isTrue();

    Collaborator testCollaborator = this.applicationContext.getBean(Collaborator.class);

    assertThat(testCollaborator).isNotNull();
    assertThat(testCollaborator.getName()).isEqualTo("test");

    DependentObject dependentObject =
      this.applicationContext.getBean("dependentObject", DependentObject.class);

    assertThat(dependentObject).isNotNull();
    assertThat(dependentObject.getCollaborator().orElse(null)).isEqualTo(testCollaborator);
  }

  @Configuration
  @EnableCollaborator(collaboratorBeanName = "MockCollaborator")
  static class MockTestConfiguration {

    @Bean
    DependentObject dependentObject() {
      return new DependentObject();
    }

    @Bean("MockCollaborator")
    Collaborator mockCollaborator() {

      Collaborator mockCollaborator = mock(Collaborator.class);

      when(mockCollaborator.getName()).thenReturn("mock");

      return mockCollaborator;
    }
  }

  @Configuration
  @EnableCollaborator
  static class RealTestConfiguration {

    @Bean
    DependentObject dependentObject(Collaborator collaborator) {
      return new DependentObject(collaborator);
    }
  }

  interface Collaborator {
    String getName();
  }

  static class DependentObject {

    private Collaborator collaborator;

    public DependentObject() {
      this(null);
    }

    public DependentObject(Collaborator collaborator) {
      this.collaborator = collaborator;
    }

    public Optional<Collaborator> getCollaborator() {
      return Optional.ofNullable(this.collaborator);
    }

    public void setCollaborator(Collaborator collaborator) {
      this.collaborator = collaborator;
    }
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Import(CollaboratorConfiguration.class)
  @interface EnableCollaborator {
    String collaboratorBeanName() default "TestCollaborator";
  }

  @Configuration
  static class CollaboratorConfiguration implements ImportAware {

    private static final String DEFAULT_COLLABORATOR_BEAN_NAME = "TestCollaborator";

    private String collaboratorBeanName = DEFAULT_COLLABORATOR_BEAN_NAME;

    @Override
    public void setImportMetadata(AnnotationMetadata annotationMetadata) {

      if (annotationMetadata.hasAnnotation(EnableCollaborator.class.getName())) {

        AnnotationAttributes enableCollaboratorAttributes =
          AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableCollaborator.class.getName()));

        this.collaboratorBeanName = enableCollaboratorAttributes.getString("collaboratorBeanName");
      }
    }

    // Configure the DependencyObject with the Collaborator resolved from the Spring context.
    @Bean
    BeanFactoryPostProcessor dependentObjectCollaboratorResolver() {

      String resolvedCollaboratorBeanName = this.collaboratorBeanName;

      return beanFactory ->
        Arrays.stream(beanFactory.getBeanDefinitionNames()).forEach(beanName -> {
          Optional.of(beanFactory.getBeanDefinition(beanName))
            .filter(beanDefinition -> isDependentObject(beanName, beanDefinition, beanFactory))
            .ifPresent(beanDefinition ->
              beanDefinition.getPropertyValues().addPropertyValue("collaborator",
                new RuntimeBeanReference(resolvedCollaboratorBeanName))
            );
        });
    }

    private boolean isDependentObject(String beanName, BeanDefinition beanDefinition,
        ConfigurableListableBeanFactory beanFactory) {

      return Optional.ofNullable(beanDefinition)
        .flatMap(it -> resolveBeanClass(it, beanFactory.getBeanClassLoader()))
        .filter(DependentObject.class::isAssignableFrom)
        .isPresent();
    }

    private Optional<Class<?>> resolveBeanClass(BeanDefinition beanDefinition, ClassLoader classLoader) {

      Class<?> beanClass = beanDefinition instanceof AbstractBeanDefinition
        ? safeResolveType(() -> ((AbstractBeanDefinition) beanDefinition).resolveBeanClass(classLoader))
        : null;

      if (beanClass == null) {
        beanClass = resolveBeanClassName(beanDefinition)
          .map(beanClassName ->
            safeResolveType(() ->
              ClassUtils.forName(beanClassName, classLoader))).orElse(null);
      }

      return Optional.ofNullable(beanClass);
    }

    private Optional<String> resolveBeanClassName(BeanDefinition beanDefinition) {

      Optional<String> beanClassName = Optional.ofNullable(beanDefinition)
        .map(BeanDefinition::getBeanClassName)
        .filter(StringUtils::hasText);

      if (!beanClassName.isPresent()) {
        beanClassName = Optional.ofNullable(beanDefinition)
          .filter(it -> it instanceof AnnotatedBeanDefinition)
          .filter(it -> StringUtils.hasText(it.getFactoryMethodName()))
          .map(it -> ((AnnotatedBeanDefinition) it).getFactoryMethodMetadata())
          .map(MethodMetadata::getReturnTypeName);
      }

      return beanClassName;
    }

    private <T> Class<T> safeResolveType(TypeResolver<T> typeResolver) {

      try {
        return typeResolver.resolve();
      }
      catch (ClassNotFoundException | NoClassDefFoundError cause) {
        return null;
      }
    }

    @Bean(DEFAULT_COLLABORATOR_BEAN_NAME)
    public Collaborator testCollaborator() {
      return () -> "test";
    }
  }

  interface TypeResolver<T> {
    Class<T> resolve() throws ClassNotFoundException;
  }
}
