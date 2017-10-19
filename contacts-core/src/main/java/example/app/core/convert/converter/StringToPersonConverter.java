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

import static org.cp.elements.lang.RuntimeExceptionsFactory.newIllegalArgumentException;

import java.util.Optional;

import org.cp.elements.lang.Assert;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

import example.app.model.Person;

/**
 * The {@link StringToPersonConverter} class is a Spring {@link Converter} that converts
 * a {@link String} to a {@link Person}.
 *
 * The {@link String} is expected to contain the {@link Person Person's} name, both first and last name are required.
 * The first and last {@link String name} components maybe be separated by 1 or more {@link String spaces}
 * or 1 or more {@link String underscores}.
 *
 * @author John Blum
 * @see java.lang.String
 * @see org.springframework.core.convert.converter.Converter
 * @see example.app.model.Person
 * @since 1.0.0
 */
public class StringToPersonConverter implements Converter<String, Person> {

  @Nullable @Override
  public Person convert(String name) {

    Assert.hasText(name, "Name [%s] is required", name);

    String[] nameParts = Optional.of(name.split("[\\s|_]+"))
      .filter(it -> it.length > 1)
      .orElseThrow(() ->
        newIllegalArgumentException("Expected full name [%s] to have both a first and last name", name));

    return Person.newPerson(nameParts[0], nameParts[1]);
  }
}
