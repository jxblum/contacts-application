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

package example.app.service.support;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Calculator class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class Calculator {

  public static long factorial(long number) {

    assertThat(number).describedAs("Number [%d] must be greater than equal to 0", number);

    if (number <= 2L) {
      return (number < 2L ? 1L : 2L);
    }

    long result = number;

    while (number-- > 1) {
      result *= number;
    }

    return result;
  }
}
