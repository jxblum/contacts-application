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

package example.app.repo.provider;

import example.app.repo.GeocodingException;

/**
 * The GoogleMapsApiGeocodingException class is a {@link GeocodingException} extension indicating a problem
 * while accessing Google's Map API Geocoding web services.
 *
 * @author John Blum
 * @see example.app.repo.GeocodingException
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class GoogleMapsApiGeocodingException extends GeocodingException {

  /* (non-Javadoc) */
  public GoogleMapsApiGeocodingException() {
  }

  /* (non-Javadoc) */
  public GoogleMapsApiGeocodingException(String message) {
    super(message);
  }

  /* (non-Javadoc) */
  public GoogleMapsApiGeocodingException(Throwable cause) {
    super(cause);
  }

  /* (non-Javadoc) */
  public GoogleMapsApiGeocodingException(String message, Throwable cause) {
    super(message, cause);
  }
}
