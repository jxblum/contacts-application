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

package example.app.geode.cache.loader.support;

import com.gemstone.gemfire.cache.AttributesMutator;
import com.gemstone.gemfire.cache.CacheLoader;

import example.app.geode.cache.support.DeclarableSupport;

/**
 * The CacheLoaderSupport class is a default implementation of the GemFire/Geode {@link CacheLoader} callback interface
 * that gets called when a cache miss for a particular key occurs.
 *
 * @author John Blum
 * @see com.gemstone.gemfire.cache.CacheLoader
 * @see example.app.geode.cache.support.DeclarableSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class CacheLoaderSupport<K, V> extends DeclarableSupport implements CacheLoader<K, V> {

  /**
   * Called when the Region containing this callback is closed or destroyed, when the cache is closed,
   * or when a callback is removed from a Region using an {@link AttributesMutator}.
   */
  @Override
  public void close() {
  }
}
