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
