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

package example.tests.spring.context.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.tests.spring.context.annotation.AnnotationConfigIntegrationTests.ApplicationConfiguration;
import example.tests.spring.context.annotation.AnnotationConfigIntegrationTests.EnableFeature.FeaturePolicy;

/**
 * The AnnotationConfigIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ApplicationConfiguration.class)
@SuppressWarnings("unused")
public class AnnotationConfigIntegrationTests {

  private static final AtomicBoolean CONFIGURED = new AtomicBoolean(false);

  @Test
  public void testConfigurationBootstrapping() {
    assertThat(CONFIGURED.get()).isTrue();
  }

  @Configuration
  @EnableFeature(policies = {
    @FeaturePolicy(name = "one"),
    @FeaturePolicy(name = "two")
  })
  static class ApplicationConfiguration { }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Documented
  @Import(FeatureConfiguration.class)
  @interface EnableFeature {

    FeaturePolicy[] policies() default {};

    @interface FeaturePolicy {
        String name();
    }
  }

  @Configuration
  static class FeatureConfiguration implements ImportAware {

    @Override
    public void setImportMetadata(AnnotationMetadata annotationMetadata) {

      AnnotationAttributes enableFeatureAttributes =
        AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableFeature.class.getName()));

      assertThat(enableFeatureAttributes.annotationType()).isEqualTo(EnableFeature.class);

      Arrays.stream(enableFeatureAttributes.getAnnotationArray("policies"))
        .forEach(featurePolicyAttributes ->
          assertThat(featurePolicyAttributes.annotationType()).isEqualTo(FeaturePolicy.class));

      CONFIGURED.set(true);
    }
  }
}
