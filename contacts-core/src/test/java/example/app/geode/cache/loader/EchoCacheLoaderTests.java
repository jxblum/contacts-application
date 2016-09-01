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

package example.app.geode.cache.loader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.gemstone.gemfire.cache.LoaderHelper;

/**
 * Test suite of test cases testing the contract and functionality of the {@link EchoCacheLoader} class.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mockito
 * @see example.app.geode.cache.loader.EchoCacheLoader
 * @since 1.0.0
 */
public class EchoCacheLoaderTests {

  @Test
  @SuppressWarnings("unchecked")
  public void loadReturnsKeyAsValue() {
    LoaderHelper<String, String> mockLoaderHelper = mock(LoaderHelper.class);

    when(mockLoaderHelper.getKey()).thenReturn("test");

    assertThat(EchoCacheLoader.getInstance().load(mockLoaderHelper)).isEqualTo("test");

    verify(mockLoaderHelper, times(1)).getKey();
  }
}
