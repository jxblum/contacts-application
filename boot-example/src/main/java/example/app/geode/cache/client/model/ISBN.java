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

package example.app.geode.cache.client.model;

import java.io.Serializable;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.StringUtils;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;

import lombok.Getter;

/**
 * The {@link ISBN} class is a Abstract Data Type (ADT) modeling a book 10 digit or 13 digit ISBN number.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see org.springframework.data.annotation.PersistenceConstructor
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ISBN implements Serializable {

  protected static final String TEN_DIGIT_FORMAT = "%1$s%2$s";
  protected static final String THIRTEEN_DIGIT_FORMAT = "%1$s-%2$s";

  public static ISBN of(String number) {
    return new ISBN(StringUtils.getDigits(number));
  }

  @Getter
  private final String number;

  @PersistenceConstructor
  private ISBN(String number) {

    Assert.hasText(number, "ISBN number is required");

    Assert.isTrue(number.length() == 10 || number.length() == 13,
      "ISBN number [%s] must be either 10 or 13 digits", number);

    this.number = number;
  }

  @Transient
  public boolean isTenDigit() {
    return getNumber().length() == 10;
  }

  @Transient
  public boolean isThirteenDigit() {
    return getNumber().length() == 13;
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof ISBN)) {
      return false;
    }

    ISBN that = (ISBN) obj;

    return this.getNumber().equalsIgnoreCase(that.getNumber());
  }

  @Override
  public int hashCode() {

    int hashValue = 17;

    hashValue = 37 * hashValue + getNumber().hashCode();

    return hashValue;
  }

  @Override
  public String toString() {

    String number = getNumber();

    return String.format(isThirteenDigit() ? THIRTEEN_DIGIT_FORMAT : TEN_DIGIT_FORMAT,
      number.substring(0, 3), number.substring(3));
  }
}
