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

package example.app.geode.cache.client.config;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import example.app.core.convert.converter.StringToPersonConverter;
import example.app.core.mapping.json.jackson.serialization.LocalDateDeserializer;
import example.app.core.mapping.json.jackson.serialization.LocalDateSerializer;

/**
 * The {@link WebClientConfiguration} class is a Spring {@link Configuration} class and {@link WebMvcConfigurer}
 * implementation used to customize the Spring Web MVC framework.
 *
 * @author John Blum
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer
 * @since 1.0.0
 */
@Configuration
@EnableWebMvc
@SuppressWarnings("unused")
public class WebClientConfiguration implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringToPersonConverter());
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {

    converters.stream()
      .filter(converter -> converter instanceof AbstractJackson2HttpMessageConverter)
      .forEach(converter -> {

        ObjectMapper objectMapper = ((AbstractJackson2HttpMessageConverter) converter).getObjectMapper();

        LocalDateDeserializer.register(objectMapper);
        LocalDateSerializer.register(objectMapper);
      });
  }
}
