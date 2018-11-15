/*
 * Copyright 2018 the original author or authors.
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

package example.app.geode.cache.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheTransactionManager;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.TransactionListener;
import org.apache.geode.cache.TransactionWriter;
import org.apache.geode.cache.util.TransactionListenerAdapter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer;
import org.springframework.data.gemfire.transaction.config.EnableGemfireCacheTransactions;

/**
 * Simple {@link SpringBootApplication Spring Boot application} with an embedded, Apache Geode {@link Cache peer cache}
 * instance, asserting the configuration of Geode {@link CacheTransactionManager} with registered
 * {@link TransactionListener TransactionListeners} and a registered {@link TransactionWriter}.
 *
 * @author John Blum
 * @see org.apache.geode.cache.Cache
 * @see org.apache.geode.cache.CacheTransactionManager
 * @see org.apache.geode.cache.GemFireCache
 * @see org.apache.geode.cache.TransactionListener
 * @see org.apache.geode.cache.TransactionWriter
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.PeerCacheConfigurer
 * @see org.springframework.data.gemfire.transaction.config.EnableGemfireCacheTransactions
 * @since 1.0.0
 */
@SpringBootApplication
@PeerCacheApplication(name = "TransactionExample")
@EnableGemfireCacheTransactions
@SuppressWarnings("unused")
public class BootGeodeLocalCacheTransactionServerApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(BootGeodeLocalCacheTransactionServerApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(args);
  }

  @Bean
  ApplicationRunner runner(GemFireCache gemfireCache) {

    return args -> {

      assertThat(gemfireCache).isNotNull();

      CacheTransactionManager cacheTransactionManager = gemfireCache.getCacheTransactionManager();

      assertThat(cacheTransactionManager).isNotNull();
      assertThat(cacheTransactionManager.getListeners())
        .containsExactly(transactionListenerOne(), transactionListenerTwo());
      assertThat(cacheTransactionManager.getWriter()).isSameAs(transactionWriter());

      System.err.println("SUCCESS!");
    };
  }

  @Bean
  PeerCacheConfigurer transactionListenersWriterConfigurer(List<TransactionListener> transactionListenerBeans,
      TransactionWriter transactionWriterBean) {

    return (beanName, peerCacheFactoryBean) -> {
      peerCacheFactoryBean.setTransactionListeners(transactionListenerBeans);
      peerCacheFactoryBean.setTransactionWriter(transactionWriterBean);
    };
  }

  @Bean
  TransactionListener transactionListenerOne() {
    return new TransactionListenerAdapter() { };
  }

  @Bean
  TransactionListener transactionListenerTwo() {
    return new TransactionListenerAdapter() { };
  }

  @Bean
  TransactionWriter transactionWriter() {
    return event -> { };
  }
}
