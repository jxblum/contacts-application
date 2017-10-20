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

package example.app.core.mapping.json.jackson.serialization;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * The {@link LocalDateSerializer} class is a Jackson {@link StdSerializer} implementation that maps a Java 8
 * {@link LocalDate} object to JSON data.
 *
 * @author John Blum
 * @see java.time.LocalDate
 * @see com.fasterxml.jackson.core.JsonGenerator
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see com.fasterxml.jackson.databind.SerializerProvider
 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer
 * @since 1.0.0
 */
public class LocalDateSerializer extends StdSerializer<LocalDate> {

  public static final LocalDateSerializer INSTANCE = new LocalDateSerializer();

  public static ObjectMapper register(ObjectMapper objectMapper) {

    return objectMapper.registerModule(new SimpleModule(LocalDateSerializer.class.getSimpleName())
      .addSerializer(LocalDate.class, INSTANCE));
  }

  public LocalDateSerializer() {
    super(LocalDate.class);
  }

  @Override
  public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {

    if (localDate != null) {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeNumberField("year", localDate.getYear());
      jsonGenerator.writeStringField("month", localDate.getMonth().toString());
      jsonGenerator.writeNumberField("dayOfMonth", localDate.getDayOfMonth());
      jsonGenerator.writeEndObject();
    }
    else {
      jsonGenerator.writeString("null");
    }
  }
}
