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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import example.app.model.PhoneNumber;

/**
 * Test suite of test cases testing the contract and functionality of the {@link StringToPhoneNumberConverter} class.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see example.app.core.convert.converter.StringToPhoneNumberConverter
 * @see example.app.model.PhoneNumber
 * @since 1.0.0
 */
public class StringToPhoneNumberConverterTests {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void convertJsonPhoneNumberFormatIsSuccessful() {
    String JSON_PHONE_NUMBER = "{\"areaCode\":\"503\", \"prefix\":\"555\", \"suffix\":\"1234\"";

    assertThat(StringToPhoneNumberConverter.INSTANCE.convert(JSON_PHONE_NUMBER))
      .isEqualTo(PhoneNumber.newPhoneNumber("503", "555", "1234"));
  }

  @Test
  public void convertStandardPhoneNumberFormatsIsSuccessful() {
    assertThat(StringToPhoneNumberConverter.INSTANCE.convert("(503) 555-1234"))
      .isEqualTo(PhoneNumber.newPhoneNumber("503", "555", "1234"));

    assertThat(StringToPhoneNumberConverter.INSTANCE.convert("503-555-1234"))
      .isEqualTo(PhoneNumber.newPhoneNumber("503", "555", "1234"));
  }

  @Test
  public void convertPhoneNumberWithTooFewDigitsThrowsIllegalArgument() {
    exception.expect(IllegalArgumentException.class);
    exception.expectCause(is(nullValue(Throwable.class)));
    exception.expectMessage(is(equalTo("Value [555-1234] is not valid phone number")));

    StringToPhoneNumberConverter.INSTANCE.convert("555-1234");
  }

  @Test
  public void getDigitsForNumber() {
    assertThat(StringToPhoneNumberConverter.INSTANCE.getDigits("0123456789")).isEqualTo("0123456789");
  }

  @Test
  public void getDigitsForAlphanumeric() {
    assertThat(StringToPhoneNumberConverter.INSTANCE.getDigits("$Ol2E4^SG7Bg!%")).isEqualTo("247");
  }

  @Test
  public void getDigitsForNonNumericValue() {
    assertThat(StringToPhoneNumberConverter.INSTANCE.getDigits("OxACCBEFD")).isEqualTo("");
  }

  @Test
  public void getDigitsForEmptyString() {
    assertThat(StringToPhoneNumberConverter.INSTANCE.getDigits("  ")).isEqualTo("");
    assertThat(StringToPhoneNumberConverter.INSTANCE.getDigits("")).isEqualTo("");
  }

  @Test
  public void getDigitsForNull() {
    assertThat(StringToPhoneNumberConverter.INSTANCE.getDigits(null)).isEqualTo("");
  }

  @Test
  public void nullSafeCharArrayForCharArray() {
    assertThat(StringToPhoneNumberConverter.INSTANCE.nullSafeCharArray("abc")).isEqualTo(new char[] { 'a', 'b', 'c' });
  }

  @Test
  public void nullSafeCharArrayForNull() {
    assertThat(StringToPhoneNumberConverter.INSTANCE.nullSafeCharArray(null)).isEqualTo(new char[0]);
  }
}
