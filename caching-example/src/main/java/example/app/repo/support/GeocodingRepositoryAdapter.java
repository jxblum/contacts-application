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

package example.app.repo.support;

import org.springframework.data.geo.Point;

import example.app.model.Address;
import example.app.repo.GeocodingRepository;

/**
 * The GeocodingRepositoryAdapter class is an implementation of {@link GeocodingRepository} providing
 * default unsupported (no-op) implementations of the geocoding data access operations.
 *
 * @author John Blum
 * @see example.app.model.Address
 * @see example.app.repo.GeocodingRepository
 * @see org.springframework.data.geo.Point
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class GeocodingRepositoryAdapter implements GeocodingRepository {

  /* (non-Javadoc) */
  @Override
  public Point geocode(Address address) {
    throw new UnsupportedOperationException("geocoding addresses is not supported");
  }

  /* (non-Javadoc) */
  @Override
  public Address reverseGeocode(Point point) {
    throw new UnsupportedOperationException(
      "reverse geocoding geographic coordinates (e.g. Latitude/Longitude) is not supported");
  }
}
