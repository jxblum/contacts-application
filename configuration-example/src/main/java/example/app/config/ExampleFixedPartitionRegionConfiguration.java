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

package example.app.config;

import java.util.Collections;

import org.apache.geode.cache.FixedPartitionAttributes;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.PartitionAttributes;
import org.apache.geode.cache.RegionAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.FixedPartitionAttributesFactoryBean;
import org.springframework.data.gemfire.PartitionAttributesFactoryBean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;

/**
 * The ExampleFixedPartitionRegionConfiguration class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Configuration
public class ExampleFixedPartitionRegionConfiguration {

  @Bean("Example")
  public PartitionedRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache,
      RegionAttributes<Object, Object> regionAttributes) {

    PartitionedRegionFactoryBean<Object, Object> exampleRegion = new PartitionedRegionFactoryBean<>();

    exampleRegion.setAttributes(regionAttributes);
    exampleRegion.setCache(gemfireCache);
    exampleRegion.setClose(false);
    exampleRegion.setPersistent(false);

    return exampleRegion;
  }

  @Bean
  RegionAttributesFactoryBean exampleRegionAttributes(PartitionAttributes examplePartitionAttributes) {

    RegionAttributesFactoryBean exampleAttributes = new RegionAttributesFactoryBean();

    exampleAttributes.setPartitionAttributes(examplePartitionAttributes);

    return exampleAttributes;
  }


  @Bean
  PartitionAttributesFactoryBean examplePartitionAttributes(FixedPartitionAttributes exampleFixedPartitionAttributes) {

    PartitionAttributesFactoryBean examplePartitionAttributes =
      new PartitionAttributesFactoryBean();

    examplePartitionAttributes.setFixedPartitionAttributes(
      Collections.singletonList(exampleFixedPartitionAttributes));

    return examplePartitionAttributes;
  }

  @Bean
  FixedPartitionAttributesFactoryBean exampleFixedPartitionAttributes() {

    FixedPartitionAttributesFactoryBean exampleFixedPartitionAttributes =
      new FixedPartitionAttributesFactoryBean();

    exampleFixedPartitionAttributes.setPartitionName("Q1");
    exampleFixedPartitionAttributes.setPrimary(true);

    return exampleFixedPartitionAttributes;
  }
}
