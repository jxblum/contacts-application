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

package example.app.geode.cache.client;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.cp.elements.lang.Constants;
import org.cp.elements.lang.Identifiable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication.Locator;
import org.springframework.data.gemfire.config.annotation.EnableContinuousQueries;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;

import example.app.geode.cache.client.service.ChatBotService;

/**
 * The ChatBotClientApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@ClientCacheApplication(name = "ChatBotClient", locators = @Locator, subscriptionEnabled = true)
@EnableContinuousQueries(poolName = "DEFAULT")
@EnableScheduling
@SuppressWarnings("unused")
public class ChatBotClientApplication implements Identifiable<String> {

  public static void main(String[] args) {
    SpringApplication.run(ChatBotClientApplication.class, args);
  }

  @Value("${example.app.chat.bot.id:Client 0}")
  private String chatBotAppId;

  @Override
  public String getId() {
    return this.chatBotAppId;
  }

  @Override
  public final void setId(String id) {
    throw new UnsupportedOperationException(Constants.OPERATION_NOT_SUPPORTED);
  }

  @Bean("Chats")
  public ClientRegionFactoryBean<Integer, String> chatRegion(GemFireCache gemfireCache) {

    ClientRegionFactoryBean<Integer, String> chatRegion = new ClientRegionFactoryBean<>();

    chatRegion.setCache(gemfireCache);
    chatRegion.setClose(false);
    chatRegion.setShortcut(ClientRegionShortcut.PROXY);

    return chatRegion;
  }

  @Bean
  @DependsOn("Chats")
  public GemfireTemplate chatTemplate(GemFireCache gemfireCache) {
    return new GemfireTemplate(gemfireCache.getRegion("/Chats"));
  }

  @Bean
  public SchedulingConfigurer scheduleFixedRateChatBot(ChatBotService chatBotService,
      @Value("${example.app.chat.bot.schedule.interval:5000}") int interval) {

    return scheduledTaskRegistrar ->
      scheduledTaskRegistrar.addFixedRateTask(() -> chatBotService.sendChat(getId()), interval);
  }
}
