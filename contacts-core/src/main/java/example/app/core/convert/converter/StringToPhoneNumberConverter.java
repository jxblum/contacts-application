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

package example.app.core.convert.converter;

import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.convert.converter.Converter;

import example.app.model.PhoneNumber;

/**
 * The StringToPhoneNumberConverter class is a Spring {@link Converter} that converts
 * a {@link String} into a {@link PhoneNumber}.
 *
 * @author John Blum
 * @see java.lang.String
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see org.springframework.core.convert.converter.Converter
 * @see example.app.model.PhoneNumber
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class StringToPhoneNumberConverter implements Converter<String, PhoneNumber> {

	public static StringToPhoneNumberConverter INSTANCE = new StringToPhoneNumberConverter();

	protected static final char[] EMPTY_CHAR_ARRAY = new char[0];

	protected static final Pattern JSON_PHONE_NUMBER =
		Pattern.compile("\\{\"areaCode\":\"\\d{3}\",\"prefix\":\"\\d{3}\",\"suffix\":\"\\d{4}\"\\}");

	protected String getDigits(String value) {
		StringBuilder buffer = new StringBuilder();

		for (char character : nullSafeCharArray(value)) {
			if (Character.isDigit(character)) {
				buffer.append(character);
			}
		}

		return buffer.toString();
	}

	protected char[] nullSafeCharArray(String value) {
		return (value != null ? value.toCharArray() : EMPTY_CHAR_ARRAY);
	}

	@Override
	public PhoneNumber convert(String value) {
		try {
			if (JSON_PHONE_NUMBER.matcher(value).find()) {
				ObjectMapper objectMapper = new ObjectMapper();
				return objectMapper.readValue(value, PhoneNumber.class);
			}

			String digits = getDigits(value);

			if (digits.length() == 10) {
				return PhoneNumber.newPhoneNumber(digits.substring(0, 3), digits.substring(3, 6), digits.substring(6));
			}

			throw new IllegalArgumentException(String.format("Value [%s] is not valid PhoneNumber", value));
		}
		catch (Exception e) {
			throw new RuntimeException(String.format("Failed to convert value [%1$s] into a %2$s",
				value, PhoneNumber.class.getName()), e);
		}
	}
}
