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
