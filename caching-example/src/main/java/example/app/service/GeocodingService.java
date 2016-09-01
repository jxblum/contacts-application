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

package example.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import example.app.model.Address;
import example.app.repo.GeocodingRepository;
import example.app.service.support.AbstractCacheableService;

/**
 * The GeocodingService class is a Spring {@link Service} component capable of converting addresses
 * to geographic coordinates and geographic coordinates to addresses.
 *
 * @author John Blum
 * @see example.app.model.Address
 * @see example.app.repo.GeocodingRepository
 * @see example.app.service.support.AbstractCacheableService
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.data.geo.Point
 * @since 1.0.0
 */
@Service
@SuppressWarnings("unused")
public class GeocodingService extends AbstractCacheableService {

  private final GeocodingRepository geocodingRepository;

  @Autowired
  public GeocodingService(GeocodingRepository geocodingRepository) {
    Assert.notNull(geocodingRepository, "GeocodingRepository cannot be null");
    this.geocodingRepository = geocodingRepository;
  }

  @SuppressWarnings("all")
  protected GeocodingRepository getGeocodingRepository() {
    Assert.state(geocodingRepository != null, "GeocodingRepository was not properly configured and initialized");
    return this.geocodingRepository;
  }

  @Cacheable(value = "AddressToLatitudeLongitude")
  public Point geocode(Address address) {
    setCacheMiss();
    return getGeocodingRepository().geocode(address);
  }

  @Cacheable(value = "LatitudeLongitudeToAddress")
  public Address reverseGeocode(Point point) {
    setCacheMiss();
    return getGeocodingRepository().reverseGeocode(point);
  }
}
