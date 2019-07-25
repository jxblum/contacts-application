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

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.DataPolicy;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionAttributes;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.Scope;
import org.apache.geode.cache.wan.GatewaySender;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.ReplicatedRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.EnableGemFireProperties;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit Tests for {@link RegionMutatorBeanPostProcessor}.
 *
 * To run this test class as a Integration Test, then comment out the Spring bean definition:
 *
 * <code>
 *   <bean class="org.springframework.data.gemfire.tests.mock.config.GemFireMockObjectsBeanPostProcessor"/>
 * </code>
 *
 * In the {@literal NonPrimaryNonSecondaryGatewaySenderConfiguredRegionUnitTests-context.xml} file.
 *
 * Then, additionally, you must start a Locator and Server using Gfsh, then create the "Example" {@link Region}
 * and add 2 {@link GatewaySender GatewaySenders} as follows:
 *
 * <code>
 * $ gfsh
 *     _________________________     __
 *    / _____/ ______/ ______/ /____/ /
 *   / /  __/ /___  /_____  / _____  /
 *  / /__/ / ____/  _____/ / /    / /
 * /______/_/      /______/_/    /_/    1.6.0
 *
 * Monitor and Manage Apache Geode
 *
 *
 * gfsh>start locator --name=LocatorOne --log-level=config
 * Starting a Geode Locator in /Users/jblum/pivdev/lab/LocatorOne...
 * ....
 *
 * gfsh>start server --name=ServerOne --log-level=config
 * Starting a Geode Server in /Users/jblum/pivdev/lab/ServerOne...
 * ....
 *
 * gfsh>list members
 *    Name    | Id
 * ---------- | -----------------------------------------------------------------
 * LocatorOne | 10.99.199.24(LocatorOne:51813:locator)<ec><v0>:1024 [Coordinator]
 * ServerOne  | 10.99.199.24(ServerOne:51839)<v1>:1025
 *
 *
 *
 * gfsh>create region --name=Example --type=REPLICATE
 *  Member   | Status
 * --------- | ----------------------------------------
 * ServerOne | Region "/Example" created on "ServerOne"
 *
 *  * gfsh>list regions
 * List of regions
 * ---------------
 * Example
 *
 * gfsh>describe region --name=/Example
 * ..........................................................
 * Name            : Example
 * Data Policy     : replicate
 * Hosting Members : ServerOne
 *
 * Non-Default Attributes Shared By Hosting Members
 *
 *  Type  |    Name     | Value
 * ------ | ----------- | ---------------
 * Region | data-policy | REPLICATE
 *        | size        | 0
 *        | scope       | distributed-ack
 *
 *
 *
 * gfsh>create gateway-sender --id=testGatewaySenderIdOne --remote-distributed-system-id=1
 *  Member   | Status
 * --------- | ------------------------------------------------------------
 * ServerOne | GatewaySender "testGatewaySenderIdOne" created on "ServerOne"
 *
 * gfsh>create gateway-sender --id=testGatewaySenderIdTwo --remote-distributed-system-id=1
 *  Member   | Status
 * --------- | -------------------------------------------------------------
 * ServerOne | GatewaySender "testGatewaySenderIdTwo" created on "ServerOne"
 *
 * gfsh>list gateways
 * Gateways
 *
 *
 * GatewaySender
 *
 *    GatewaySender Id    |                 Member                 | Remote Cluster Id |  Type  | Status  | Queued Events | Receiver Location
 * ---------------------- | -------------------------------------- | ----------------- | ------ | ------- | ------------- | -----------------
 * testGatewaySenderIdTwo | 10.99.199.24(ServerOne:51839)<v1>:1025 | 1                 | Serial | Running | 0             | null
 * testGatwaySenderIdOne  | 10.99.199.24(ServerOne:51839)<v1>:1025 | 1                 | Serial | Running | 0             | null
 *
 * </code>
 *
 * Then, you can re-run this test and this test will connect as {@literal peer} {@link Cache} server member
 * of the cluster started with Gfsh, as shown above.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.wan.GatewaySender
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.tests.geode.wan.RegionMutatorBeanPostProcessor
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class NonPrimaryNonSecondaryGatewaySenderConfiguredRegionUnitTests {

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

  @EnableGemFireProperties(distributedSystemId = 1)
  //@PeerCacheApplication(locators = "localhost[11235]")
  static class PeerCacheConfiguration {

    @Bean("Example")
    ReplicatedRegionFactoryBean<Object, Object> exampleRegion(GemFireCache gemfireCache) {

      ReplicatedRegionFactoryBean<Object, Object> exampleRegion = new ReplicatedRegionFactoryBean<Object, Object>() {

        @Override
        protected RegionFactory<Object, Object> configure(RegionFactory<Object, Object> regionFactory) {

          regionFactory.addGatewaySenderId("testGatewaySenderIdOne");
          regionFactory.addGatewaySenderId("testGatewaySenderIdTwo");

          return super.configure(regionFactory);
        }
      };

      exampleRegion.setCache(gemfireCache);
      exampleRegion.setClose(false);
      exampleRegion.setPersistent(false);
      exampleRegion.setScope(Scope.DISTRIBUTED_ACK);

      return exampleRegion;
    }
  }
}
