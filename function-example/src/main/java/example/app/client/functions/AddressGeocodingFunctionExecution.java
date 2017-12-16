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

package example.app.client.functions;

import org.apache.geode.cache.Region;
import org.springframework.data.gemfire.function.annotation.FunctionId;
import org.springframework.data.gemfire.function.annotation.OnRegion;

import example.app.model.Address;

/**
 * The {@link AddressGeocodingFunctionExecution} interface is an Spring Data for Apache Geode client-side Function execution
 * definition for geocoding {@link Address} objects persisted to the {@literal Address} {@link Region}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.springframework.data.gemfire.function.annotation.OnRegion
 * @see example.app.model.Address
 * @since 1.0.0
 */
@OnRegion(region = "Address")
public interface AddressGeocodingFunctionExecution {

  @FunctionId("GeocodeAddresses")
  void geocodeAddresses();

}
