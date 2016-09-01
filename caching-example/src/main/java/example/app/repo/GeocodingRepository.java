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

package example.app.repo;

import org.springframework.data.geo.Point;

import example.app.model.Address;

/**
 * The GeocodingRepository class is a Spring {@link org.springframework.stereotype.Repository} for performing
 * geocoding data access operations.
 *
 * @author John Blum
 * @see example.app.model.Address
 * @see org.springframework.data.geo.Point
 * @since 1.0.0
 */
public interface GeocodingRepository {

  Point geocode(Address address);

  Address reverseGeocode(Point point);

}
