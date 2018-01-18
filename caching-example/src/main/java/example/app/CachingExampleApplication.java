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

package example.app;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

import example.app.core.lang.RunnableUtils;
import example.app.model.Address;
import example.app.service.GeocodingService;

/**
 * The CachingExampleApplication class is a {@link SpringBootApplication} demonstrating Spring's Cache Abstraction
 * framework using Spring Data Geode with Apache Geode as the [JSR-107] caching provider.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @since 1.0.0
 */
@SpringBootApplication
@ComponentScan(excludeFilters = @Filter(type = FilterType.REGEX, pattern = "example\\.app\\.geode\\.security\\..*"))
public class CachingExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(CachingExampleApplication.class, args);
  }

  @Bean
  @SuppressWarnings("unused")
  ApplicationRunner runner(GeocodingService geocodingService) {

    return args -> {

      Address pivotal = Address.parse("15220 NW Greenbrier Pkwy, Beaverton, OR, 97006");

      RunnableUtils.timedRun(() ->
        System.err.printf("Address [%1$s] is at [%2$s]%n", pivotal, geocodingService.geocode(pivotal))
      ).ifPresent(milliseconds -> System.err.printf("%d ms%n", milliseconds));

      Address ebay = Address.parse("1400 Southwest 5th Avenue, Portland, OR, 97201");

      RunnableUtils.timedRun(() ->
        System.err.printf("Address [%1$s] is at [%2$s]%n", ebay, geocodingService.geocode(ebay))
      ).ifPresent(milliseconds -> System.err.printf("%d ms%n", milliseconds));

      RunnableUtils.timedRun(() ->
        System.err.printf("Address [%1$s] is at [%2$s]%n", pivotal, geocodingService.geocode(pivotal))
      ).ifPresent(milliseconds -> System.err.printf("%d ms%n", milliseconds));
    };
  }
}
