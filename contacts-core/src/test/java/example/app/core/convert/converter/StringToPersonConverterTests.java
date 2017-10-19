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

package example.app.core.convert.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import example.app.model.Person;

/**
 * Unit tests for {@link StringToPersonConverter}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see example.app.model.Person
 * @see org.springframework.core.convert.converter.Converter
 * @since 1.0.0
 */
public class StringToPersonConverterTests {

  private StringToPersonConverter converter = new StringToPersonConverter();

  @Test
  public void convertPersonNameWithSpace() {
    assertThat(this.converter.convert("Jon Doe")).isEqualTo(Person.newPerson("Jon", "Doe"));
  }

  @Test
  public void convertPersonNameWithSpaces() {
    assertThat(this.converter.convert("Jon   Doe")).isEqualTo(Person.newPerson("Jon", "Doe"));
  }

  @Test
  public void convertPersonNameWithUnderscore() {
    assertThat(this.converter.convert("Jack_Black")).isEqualTo(Person.newPerson("Jack", "Black"));
  }

  @Test
  public void convertPersonNameWithUnderscores() {
    assertThat(this.converter.convert("Jack___Black")).isEqualTo(Person.newPerson("Jack", "Black"));
  }

  @SuppressWarnings("all")
  @Test(expected = IllegalArgumentException.class)
  public void convertWithNullNameThrowsIllegalArgumentException() {

    try {
      converter.convert(null);
    }
    catch (IllegalArgumentException expected) {

      assertThat(expected).hasMessage("Name [null] is required");
      assertThat(expected).hasNoCause();

      throw expected;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void convertWithEmptyNameThrowsIllegalArgumentException() {

    try {
      converter.convert("");
    }
    catch (IllegalArgumentException expected) {

      assertThat(expected).hasMessage("Name [] is required");
      assertThat(expected).hasNoCause();

      throw expected;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void convertWithBlankNameThrowsIllegalArgumentException() {

    try {
      converter.convert("  ");
    }
    catch (IllegalArgumentException expected) {

      assertThat(expected).hasMessage("Name [  ] is required");
      assertThat(expected).hasNoCause();

      throw expected;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void convertIncompleteNameThrowsIllegalArgumentException() {

    try {
      converter.convert("JonDoe");
    }
    catch (IllegalArgumentException expected) {

      assertThat(expected).hasMessage("Expected full name [JonDoe] to have both a first and last name");
      assertThat(expected).hasNoCause();

      throw expected;
    }
  }
}
