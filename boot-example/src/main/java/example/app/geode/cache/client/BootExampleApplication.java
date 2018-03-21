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

package example.app.geode.cache.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import example.app.geode.cache.client.model.Book;
import example.app.geode.cache.client.model.ISBN;
import example.app.geode.cache.client.repo.BookRepository;

/**
 * The BootExampleApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@EnableEntityDefinedRegions(basePackageClasses = Book.class,
  clientRegionShortcut = ClientRegionShortcut.LOCAL)
@SuppressWarnings("unused")
public class BootExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(BootExampleApplication.class, args);
  }

  @Bean
  ApplicationRunner runner(BookRepository bookRepository) {

    return args -> {

      ISBN isbn = ISBN.of("978-1449374648");
      Book cloudNativeJava = Book.newBook(isbn, "Cloud Native Java");

      assertThat(cloudNativeJava).isNotNull();
      assertThat(cloudNativeJava.getIsbn()).isEqualTo(isbn);
      assertThat(cloudNativeJava.getTitle()).isEqualTo("Cloud Native Java");

      cloudNativeJava = bookRepository.save(cloudNativeJava);

      assertThat(cloudNativeJava).isNotNull();

      Book cloudNativeJavaReloaded = bookRepository.findByTitle(cloudNativeJava.getTitle());

      assertThat(cloudNativeJavaReloaded).isNotNull();
      assertThat(cloudNativeJavaReloaded).isEqualTo(cloudNativeJava);

      System.out.println("'Cloud Native Java' has been Booked!");
    };
  }

  @RestController
  static class WebController {

    @GetMapping("/ping")
    String ping() {
      return "PONG";
    }
  }
}
