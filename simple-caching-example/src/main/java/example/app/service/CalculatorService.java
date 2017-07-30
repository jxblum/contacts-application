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

package example.app.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import example.app.service.support.AbstractServiceSupport;
import example.app.service.support.Calculator;

/**
 * The CalculatorService class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Service
@SuppressWarnings("unused")
public class CalculatorService extends AbstractServiceSupport {

  /**
  @Cacheable("Factorials")
   */
  @Cacheable("Factorials")
  public long factorial(long number) {
    System.err.printf("%nCACHE MISS - factorial(%d)%n", number);
    sleep();
    return Calculator.factorial(number);
  }
}
