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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.gemstone.gemfire.cache.LoaderHelper;

/**
 * Test suite of test cases testing the contract and functionality of the {@link FactorialsCacheLoader} class.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class FactorialsCacheLoaderTests {

  @Mock
  private LoaderHelper<Long, Long> mockLoaderHelper;

  protected void assertFactorialOfNumberIs(long number, long result) {
    when(mockLoaderHelper.getKey()).thenReturn(number);
    assertThat(FactorialsCacheLoader.getInstance().load(mockLoaderHelper)).isEqualTo(result);
    verify(mockLoaderHelper, atLeastOnce()).getKey();
  }
  @Test
  public void loadReturnsFactorialOfKeys() {
    assertFactorialOfNumberIs(0L, 1L);
    assertFactorialOfNumberIs(1L, 1L);
    assertFactorialOfNumberIs(2L, 2L);
    assertFactorialOfNumberIs(3L, 6L);
    assertFactorialOfNumberIs(4L, 24L);
    assertFactorialOfNumberIs(5L, 120L);
    assertFactorialOfNumberIs(6L, 720L);
    assertFactorialOfNumberIs(7L, 5040L);
    assertFactorialOfNumberIs(8L, 40320L);
    assertFactorialOfNumberIs(9L, 362880L);
    assertFactorialOfNumberIs(10L, 3628800L);
    assertFactorialOfNumberIs(20L, 2432902008176640000L);
  }
}
