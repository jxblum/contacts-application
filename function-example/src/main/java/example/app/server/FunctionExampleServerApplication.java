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

package example.app.server;

import org.apache.geode.management.internal.cli.functions.ListIndexFunction;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.admin.functions.CreateIndexFunction;
import org.springframework.data.gemfire.config.admin.functions.CreateRegionFunction;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctions;

import example.app.repo.GeocodingRepository;
import example.app.repo.provider.GoogleMapsApiGeocodingRepository;
import example.app.server.functions.AddressGeocodingFunction;
import example.app.service.GeocodingService;

/**
 * The FunctionExampleServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(name = "FunctionExampleServerApplication")
@EnableManager(start = true)
@EnableLocator
@EnableGemfireFunctions
@SuppressWarnings("unused")
public class FunctionExampleServerApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(FunctionExampleServerApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(args);
  }

  @Bean
  AddressGeocodingFunction addressGeocodingFunction(GeocodingService geocodingService) {
    return new AddressGeocodingFunction(geocodingService);
  }

  @Bean
  @SuppressWarnings("unchecked")
  public <T extends GeocodingRepository> T geocodingRepository() {
    return (T) new GoogleMapsApiGeocodingRepository();
  }

  @Bean
  public GeocodingService geocodingService() {
    return new GeocodingService(geocodingRepository());
  }

  @Configuration
  @Profile("non-http-cluster-config")
  static class ApacheGeodeClusterConfiguration {

    @Bean
    CreateRegionFunction createRegionFunction() {
      return new CreateRegionFunction();
    }

    @Bean
    CreateIndexFunction createIndexFunction() {
      return new CreateIndexFunction();
    }

    @Bean
    ListIndexFunction listIndexFunction() {
      return new ListIndexFunction();
    }
  }
}
