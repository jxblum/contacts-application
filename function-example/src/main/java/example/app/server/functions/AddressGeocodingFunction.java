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

package example.app.server.functions;

import java.util.Optional;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.partition.PartitionRegionHelper;
import org.cp.elements.lang.Assert;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

import example.app.model.Address;
import example.app.service.GeocodingService;

/**
 * The {@link AddressGeocodingFunction} class is a Spring POJO component defining an Apache Geode {@link Function}
 * using SDG POJO Function annotation-based implementation support to geocode an {@link Address}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.execute.Function
 * @see org.springframework.data.gemfire.function.annotation.GemfireFunction
 * @see org.springframework.stereotype.Component
 * @since 1.0.0
 */
@Component
@SuppressWarnings("unused")
public class AddressGeocodingFunction {

  private final GeocodingService geocodingService;

  public AddressGeocodingFunction(GeocodingService geocodingService) {

    Assert.notNull(geocodingService, "GeocodingService is required");

    this.geocodingService = geocodingService;
  }

  @GemfireFunction(id = "GeocodeAddresses", optimizeForWrite = true)
  public void geocodeAddresses(FunctionContext functionContext) {

    System.err.println("Geocoding Addresses...");

    Optional.ofNullable(functionContext)
      .filter(it -> it instanceof RegionFunctionContext)
      .map(it -> (RegionFunctionContext) it)
      .ifPresent(regionFunctionContext -> {

        Region<Long, Address> localAddresses = PartitionRegionHelper.getLocalDataForContext(regionFunctionContext);

        localAddresses.values().stream()
          .filter(address -> address.getLocation() == null)
          .forEach(address -> {

            System.err.printf("Geocoding Address [%s]...%n", address);

            Point location = this.geocodingService.geocode(address);

            System.err.printf("Location is [%s]%n", location);

            address.setLocation(location);
            regionFunctionContext.getDataSet().replace(address.getId(), address);
          });
      });
  }
}
