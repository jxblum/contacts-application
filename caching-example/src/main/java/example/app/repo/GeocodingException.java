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

package example.app.repo;

/**
 * The GeocodingException class is a Java {@link RuntimeException} indicating a problem while geocoding an address
 * or reverse goecoding geographic coordinates.
 *
 * @author John Blum
 * @see java.lang.RuntimeException
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GeocodingException extends RuntimeException {

  /* (non-Javadoc) */
  public GeocodingException() {
  }

  /* (non-Javadoc) */
  public GeocodingException(String message) {
    super(message);
  }

  /* (non-Javadoc) */
  public GeocodingException(Throwable cause) {
    super(cause);
  }

  /* (non-Javadoc) */
  public GeocodingException(String message, Throwable cause) {
    super(message, cause);
  }
}
