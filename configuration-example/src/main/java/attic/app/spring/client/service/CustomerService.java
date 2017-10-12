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

package attic.app.spring.client.service;

import java.io.Serializable;

import javax.persistence.Id;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import example.app.core.lang.RunnableUtils;
import example.app.model.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The CustomerService class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Service
public class CustomerService {

  @Cacheable("Accounts")
  public Account findBy(Customer customer) {
    return RunnableUtils.safeRunReturningValueWithoutThrowing(2000,
      () -> new Account(customer.getAccountNumber()));
  }

  @Data
  @AllArgsConstructor
  public static class Account implements Serializable {
    @Id private String number;
  }
}
