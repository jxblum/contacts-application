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

import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.geode.cache.AttributesMutator;
import org.apache.geode.cache.Region;
import org.apache.shiro.util.Assert;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.gemfire.util.ArrayUtils;

/**
 * A Spring {@link BeanPostProcessor} used to mutate an Apache Geode or Pivotal GemFire {@link Region}
 * using the {@link AttributesMutator}.
 *
 * @author John Blum
 * @see java.util.stream.Stream
 * @see org.apache.geode.cache.AttributesMutator
 * @see org.apache.geode.cache.Region
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class RegionMutatorBeanPostProcessor implements BeanPostProcessor {

  private final String regionName;

  private String[] gatewaySenderIds;

  public RegionMutatorBeanPostProcessor(String regionName) {

    Assert.hasText(regionName, "Region name is required");

    this.regionName = regionName;
  }

  public String getRegionName() {
    return this.regionName;
  }

  public void setGatewaySenderIds(String[] gatewaySenderIds) {
    this.gatewaySenderIds = gatewaySenderIds;
  }

  public String[] getGatewaySenderIds() {
    return this.gatewaySenderIds;
  }

  public Stream<String> streamGatewaySenderIds() {
    return Arrays.stream(ArrayUtils.nullSafeArray(getGatewaySenderIds(), String.class));
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

    if (bean instanceof Region && getRegionName().equals(beanName)) {

      Region targetRegion = (Region) bean;

      AttributesMutator regionAttributesMutator = targetRegion.getAttributesMutator();

      streamGatewaySenderIds().forEach(regionAttributesMutator::addGatewaySenderId);
    }

    return bean;
  }
}
