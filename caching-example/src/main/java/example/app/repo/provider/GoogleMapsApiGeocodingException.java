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
