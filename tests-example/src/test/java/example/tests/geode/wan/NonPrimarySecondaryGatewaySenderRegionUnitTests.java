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
package example.tests.geode.wan;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit Tests for {@link RegionMutatorBeanPostProcessor}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see example.tests.geode.wan.RegionMutatorBeanPostProcessor
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class NonPrimarySecondaryGatewaySenderRegionUnitTests {

  @Resource(name = "Example")
  private Region<?, ?> example;

  @Test
  public void exampleRegionHasTestGatewaySenderIdOneAndTwo() {

    assertThat(this.example).isNotNull();
    assertThat(this.example.getName()).isEqualTo("Example");

    RegionAttributes<?, ?> exampleAttributes = this.example.getAttributes();

    assertThat(exampleAttributes).isNotNull();
    assertThat(exampleAttributes.getDataPolicy()).isEqualTo(DataPolicy.REPLICATE);
    assertThat(exampleAttributes.getGatewaySenderIds())
      .containsExactlyInAnyOrder("testGatewaySenderIdOne", "testGatewaySenderIdTwo");
    assertThat(exampleAttributes.getScope()).isEqualTo(Scope.DISTRIBUTED_ACK);
  }
}
