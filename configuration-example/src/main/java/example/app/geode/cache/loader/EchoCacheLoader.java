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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app.geode.cache.loader;

import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.CacheLoaderException;
import com.gemstone.gemfire.cache.LoaderHelper;

import example.app.geode.cache.support.DeclarableSupport;

/**
 * The EchoCacheLoader class is a Apache Geode {@link CacheLoader} implementation that echoes back the key
 * as the value for the key.
 *
 * @author John Blum
 * @see com.gemstone.gemfire.cache.CacheLoader
 * @see example.app.geode.cache.support.DeclarableSupport
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class EchoCacheLoader extends DeclarableSupport implements CacheLoader<String, String> {

	private static final EchoCacheLoader INSTANCE = new EchoCacheLoader();

	public static EchoCacheLoader getInstance() {
		return INSTANCE;
	}

	@Override
	public String load(LoaderHelper<String, String> helper) throws CacheLoaderException {
		return helper.getKey();
	}

	@Override
	public void close() {
	}
}
