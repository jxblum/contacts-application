package example.app.service.support;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.cache.annotation.Cacheable;

/**
 * The AbstractCacheableService class is an abstract base class extended by {@link Cacheable} service classes
 * that want to record and track cache hits and misses.
 *
 * @author John Blum
 * @see org.springframework.cache.annotation.Cacheable
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class AbstractCacheableService {

  private final AtomicBoolean cacheMiss = new AtomicBoolean(false);

  /* (non-Javadoc) */
  public boolean isCacheHit() {
    return !isCacheMiss();
  }

  /* (non-Javadoc) */
  public boolean isCacheMiss() {
    return cacheMiss.compareAndSet(true, false);
  }

  /* (non-Javadoc) */
  protected boolean setCacheMiss() {
    return cacheMiss.getAndSet(true);
  }
}
