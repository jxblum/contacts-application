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

package example.app.repo.provider;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.geo.Point;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.config.CachingExampleConfiguration;
import example.app.model.Address;
import example.app.model.State;
import example.app.service.GeocodingService;

/**
 * Test suite of test cases testing the contract and functionality of the {@link GoogleMapsApiGeocodingRepository}
 * data access object (DAO).
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.springframework.boot.test.context.SpringBootTest
 * @see org.springframework.data.geo.Point
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see example.app.config.CachingExampleConfiguration
 * @see example.app.model.Address
 * @see example.app.repo.provider.GoogleMapsApiGeocodingRepository
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CachingExampleConfiguration.class, webEnvironment = WebEnvironment.NONE)
public class GoogleMapsApiGeocodingRepositoryIntegrationTests {

  @Autowired
  @SuppressWarnings("unused")
  private GeocodingService geocodingService;

  protected Point newPoint(double latitude, double longitude) {
    return new Point(latitude, longitude);
  }

  @Test
  public void gecodeAddressIsSuccessful() {
    Address address = Address.newAddress("1600 Amphitheatre Parkway", "Mountain View", State.CALIFORNIA, "94043");
    Point latitudeLongitude = newPoint(37.422344d, -122.0844266d);

    assertThat(geocodingService.geocode(address)).isEqualTo(latitudeLongitude);
  }

  @Test
  public void reverseGeocodeLatitudeLongitudeIsSuccessful() {
    Address address = Address.newAddress("1600 Amphitheatre Parkway", "Mountain View", State.CALIFORNIA, "94043");
    Point latitudeLongitude = newPoint(37.422344d, -122.0844266d);

    assertThat(geocodingService.reverseGeocode(latitudeLongitude)).isEqualTo(address);
  }
}
