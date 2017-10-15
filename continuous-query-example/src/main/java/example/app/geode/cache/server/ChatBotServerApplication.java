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

package example.app.geode.cache.server;

import org.apache.geode.cache.ExpirationAction;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.RegionAttributes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;

/**
 * The ChatBotServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@EnableLocator
@EnableManager
@CacheServerApplication(name = "ChatBotServer")
@SuppressWarnings("unused")
public class ChatBotServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatBotServerApplication.class, args);
  }

  @Bean("Chats")
  public PartitionedRegionFactoryBean<Integer, String> chatRegion(GemFireCache gemfireCache,
      @Qualifier("ChatsRegionAttributes") RegionAttributes<Integer, String> chatRegionAttributes) {

    PartitionedRegionFactoryBean<Integer, String> chatRegion = new PartitionedRegionFactoryBean<>();

    chatRegion.setAttributes(chatRegionAttributes);
    chatRegion.setCache(gemfireCache);
    chatRegion.setClose(false);
    chatRegion.setPersistent(false);

    return chatRegion;
  }

  @Bean("ChatsRegionAttributes")
  public RegionAttributesFactoryBean chatsRegionAttributes() {

    RegionAttributesFactoryBean chatRegionAttributes = new RegionAttributesFactoryBean();

    chatRegionAttributes.setEntryTimeToLive(new ExpirationAttributes(5, ExpirationAction.DESTROY));

    return chatRegionAttributes;
  }
}
