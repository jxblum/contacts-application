package example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The CachingExampleApplication class is a {@link SpringBootApplication} demonstrating Spring's Cache Abstraction
 * framework using Spring Data Geode with Apache Geode as the [JSR-107] caching provider.
 *
 * @author John Blum
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @since 1.0.0
 */
@SpringBootApplication
public class CachingExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(CachingExampleApplication.class, args);
  }
}
