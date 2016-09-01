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

package example.app.core.mapping.json.jackson.serialization;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * The LocalDateDeserializer class is a Jackson {@link StdDeserializer} implementation that maps JSON data to a
 * Java 8 {@link LocalDate} object.
 *
 * @author John Blum
 * @see java.time.LocalDate
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.databind.deser.std.StdDeserializer
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class LocalDateDeserializer extends StdDeserializer<LocalDate> {

	public static final LocalDateDeserializer INSTANCE = new LocalDateDeserializer();

	public static ObjectMapper register(ObjectMapper objectMapper) {
		return objectMapper.registerModule(new SimpleModule(LocalDateDeserializer.class.getSimpleName())
			.addDeserializer(LocalDate.class, INSTANCE));
	}

	public LocalDateDeserializer() {
		super(LocalDate.class);
	}

	@Override
	public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		JsonNode node = parser.getCodec().readTree(parser);

		int dayOfMonth = node.get("dayOfMonth").asInt();
		int year = node.get("year").asInt();

		Month month = Month.valueOf(node.get("month").asText());

		return LocalDate.of(year, month, dayOfMonth);
	}
}
