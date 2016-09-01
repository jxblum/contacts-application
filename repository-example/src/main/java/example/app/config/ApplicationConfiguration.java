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

package example.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctionExecutions;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctions;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

import example.app.config.gemfire.GemFireConfiguration;
import example.app.function.CustomerFunctions;
import example.app.function.executions.CustomerFunctionExecutions;
import example.app.repo.gemfire.ContactRepository;
import example.app.repo.gemfire.CustomerRepository;
import example.app.service.CustomerService;

/**
 * Spring @{@link Configuration} class used to configure the application services and data access objects.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.context.annotation.Import
 * @see org.springframework.data.gemfire.function.config.EnableGemfireFunctions
 * @see org.springframework.data.gemfire.function.config.EnableGemfireFunctionExecutions
 * @see org.springframework.data.gemfire.repository.config.EnableGemfireRepositories
 * @see example.app.config.gemfire.GemFireConfiguration
 * @since 1.0.0
 */
@Configuration
@EnableGemfireFunctions
@EnableGemfireFunctionExecutions(basePackageClasses = CustomerFunctionExecutions.class)
@EnableGemfireRepositories(basePackageClasses = example.app.repo.gemfire.ContactRepository.class)
@Import(GemFireConfiguration.class)
@SuppressWarnings("unused")
public class ApplicationConfiguration {

	@Bean
	public CustomerFunctions customerFunctions() {
		return new CustomerFunctions();
	}

	@Bean
	public CustomerService customerService(ContactRepository contactRepository, CustomerRepository customerRepository) {
		return new CustomerService(contactRepository, customerRepository);
	}
}
