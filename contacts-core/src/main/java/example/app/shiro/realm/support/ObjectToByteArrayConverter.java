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

package example.app.shiro.realm.support;

import org.apache.shiro.codec.CodecSupport;
import org.springframework.core.convert.converter.Converter;

/**
 * The {@link ObjectToByteArrayConverter} class is a Spring {@link Converter} extending Apache Shiro's
 * {@link CodecSupport} to convert {@link Object Objects} to a {@code byte[]}.
 *
 * @author John Blum
 * @see org.apache.shiro.codec.CodecSupport
 * @see org.springframework.core.convert.converter.Converter
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ObjectToByteArrayConverter extends CodecSupport implements Converter<Object, byte[]> {

  public static final ObjectToByteArrayConverter INSTANCE = new ObjectToByteArrayConverter();

  /**
   * @inheritDoc
   */
  @Override
  public byte[] convert(Object source) {
    return toBytes(source);
  }
}
