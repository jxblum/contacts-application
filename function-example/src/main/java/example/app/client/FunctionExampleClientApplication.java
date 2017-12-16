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

package example.app.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.apache.geode.cache.query.CqEvent;
import org.cp.elements.lang.Identifiable;
import org.cp.elements.lang.IdentifierSequence;
import org.cp.elements.lang.support.SimpleIdentifierSequence;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableContinuousQueries;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctionExecutions;
import org.springframework.data.gemfire.listener.annotation.ContinuousQuery;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

import example.app.client.functions.AddressGeocodingFunctionExecution;
import example.app.client.repo.AddressRepository;
import example.app.model.Address;
import example.app.model.Contact;
import example.app.model.Customer;
import example.app.model.Person;
import example.app.model.State;

/**
 * The FunctionExampleClientApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@ClientCacheApplication(name = "FunctionExampleClientApplication", subscriptionEnabled = true)
@EnableContinuousQueries
@EnableEntityDefinedRegions(basePackageClasses = Address.class,
  excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE,
    classes = { Person.class, Customer.class, Contact.class }))
@EnableGemfireFunctionExecutions(basePackageClasses = AddressGeocodingFunctionExecution.class)
@EnableGemfireRepositories(basePackageClasses = AddressRepository.class)
@EnableClusterConfiguration(useHttp = true)
public class FunctionExampleClientApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(FunctionExampleClientApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(args);
  }

  private final CountDownLatch countDownLatch = new CountDownLatch(2);

  private final IdentifierSequence<Long> identifierSequence = new SimpleIdentifierSequence();

  private void assertAddresss(Address actual, Address expected) {

    assertThat(actual).isNotNull();
    assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
    assertThat(actual.getCity()).isEqualTo(expected.getCity());
    assertThat(actual.getState()).isEqualTo(expected.getState());
    assertThat(actual.getZipCode()).isEqualTo(expected.getZipCode());
  }

  private <T extends Identifiable<Long>> T identify(Identifiable<Long> identifiable) {
    return identifiable.identifiedBy(this.identifierSequence.nextId());
  }

  @ContinuousQuery(name = "GeocodedAddressEvents",
    query = "SELECT * FROM /Address WHERE location != NULL")
  public void geocodedAddress(CqEvent event) {

    Optional.ofNullable(event)
      .map(CqEvent::getNewValue)
      .filter(address -> address instanceof Address)
      .ifPresent(address -> System.err.printf("Address [%s] was geocoded%n", event.getNewValue()));

    this.countDownLatch.countDown();
  }

  @Bean
  @SuppressWarnings("unused")
  ApplicationRunner runner(AddressRepository addressRepository,
      AddressGeocodingFunctionExecution addressGeocodingFunction) {

    return args -> {

      Address mountRushmore = Address.newAddress("13000 SD-244",
        "Keystone", State.SOUTH_DAKOTA, "57751");

      Address whiteHouse = Address.newAddress("1600 Pennsylvania AVE NW",
        "Washington", State.DISTRICT_OF_COLUMBIA, "20500");

      try {
        whiteHouse = addressRepository.save(identify(whiteHouse));
        mountRushmore = addressRepository.save(identify(mountRushmore));

        assertThat(whiteHouse).isNotNull();
        assertThat(whiteHouse.getLocation()).isNull();
        assertThat(mountRushmore).isNotNull();
        assertThat(mountRushmore.getLocation()).isNull();

        addressGeocodingFunction.geocodeAddresses();

        this.countDownLatch.await();

        Address reloadedWhiteHouse = addressRepository.findById(whiteHouse.getId()).orElse(null);

        assertAddresss(reloadedWhiteHouse, whiteHouse);
        assertThat(reloadedWhiteHouse).isNotSameAs(whiteHouse);
        assertThat(reloadedWhiteHouse.getLocation()).isNotNull();

        System.err.printf("The White House is located at [%s]%n", reloadedWhiteHouse);

        Address reloadedMountRushmore = addressRepository.findById(mountRushmore.getId()).orElse(null);

        assertAddresss(reloadedMountRushmore, mountRushmore);
        assertThat(reloadedMountRushmore).isNotSameAs(mountRushmore);
        assertThat(reloadedMountRushmore.getLocation()).isNotNull();

        System.err.printf("Mount Rushmore is located at [%s]%n", reloadedMountRushmore);
      }
      finally {
        addressRepository.deleteAll(Arrays.asList(mountRushmore, whiteHouse));
      }
    };
  }
}
