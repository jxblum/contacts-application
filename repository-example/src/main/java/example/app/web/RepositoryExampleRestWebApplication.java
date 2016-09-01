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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app.web;

import static example.app.model.Address.newAddress;
import static example.app.model.Contact.newContact;
import static example.app.model.Customer.newCustomer;
import static example.app.model.PhoneNumber.newPhoneNumber;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

import example.app.config.ApplicationConfiguration;
import example.app.core.convert.converter.StringToPhoneNumberConverter;
import example.app.core.mapping.json.jackson.serialization.LocalDateDeserializer;
import example.app.model.Contact;
import example.app.model.Customer;
import example.app.model.Gender;
import example.app.model.State;
import example.app.repo.gemfire.ContactRepository;
import example.app.repo.gemfire.CustomerRepository;

/**
 * The RepositoryExampleRestWebApplication class is a {@link SpringBootApplication} demonstrating how to make
 * the Repository example application {@link ContactRepository} interface REST-ful using Spring Data REST.
 *
 * @author John Blum
 * @see org.springframework.boot.CommandLineRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see example.app.config.ApplicationConfiguration
 * @see example.app.model.Address
 * @see example.app.model.Contact
 * @see example.app.model.Customer
 * @see example.app.model.PhoneNumber
 * @see example.app.repo.gemfire.ContactRepository
 * @see example.app.repo.gemfire.CustomerRepository
 * @link http://projects.spring.io/spring-data-gemfire
 * @link http://projects.spring.io/spring-data-rest
 * @link https://spring.io/guides/gs/accessing-gemfire-data-rest/
 * @since 1.0.0
 */
@SpringBootApplication
@Import(ApplicationConfiguration.class)
@SuppressWarnings("unused")
public class RepositoryExampleRestWebApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(RepositoryExampleRestWebApplication.class, args);
	}

	@Bean
	RepositoryRestConfigurer repositoryRestConfigurer() {
		return new RepositoryRestConfigurerAdapter() {
			@Override public void configureJacksonObjectMapper(ObjectMapper objectMapper) {
				super.configureJacksonObjectMapper(objectMapper);
				LocalDateDeserializer.register(objectMapper);
			}

			@Override public void configureConversionService(ConfigurableConversionService conversionService) {
				super.configureConversionService(conversionService);
				conversionService.addConverter(StringToPhoneNumberConverter.INSTANCE);
			}
		};
	}

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Override
	public void run(String... args) throws Exception {
		Customer jonDoe = newCustomer("Jon", "Doe").with("123").as(Gender.MALE).age(42).identifiedBy(1L);

		customerRepository.save(jonDoe);

		Contact jonDoeContact = newContact(jonDoe, "jonDoe@work.com")
			.with(newAddress("100 Main St.", "Portland", State.OREGON, "97205"))
			.with(newPhoneNumber("503", "541", "1234"))
			.identifiedBy(1L);

		contactRepository.save(jonDoeContact);
	}
}
