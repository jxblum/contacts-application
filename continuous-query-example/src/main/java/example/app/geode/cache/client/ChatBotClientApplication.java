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

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.gemfire.cache.config.EnableGemfireCaching;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication.Locator;
import org.springframework.data.gemfire.config.annotation.EnableContinuousQueries;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import example.app.geode.cache.client.model.Chat;
import example.app.geode.cache.client.repo.ChatRepository;
import example.app.geode.cache.client.service.ChatService;
import example.app.geode.cache.client.service.provider.ChatBotService;

/**
 * The {@link ChatBotClientApplication} class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
//@ClientCacheApplication(name = "ChatBotClient", durableClientId = "abc123", keepAlive = true, locators = @Locator, subscriptionEnabled = true)
@ClientCacheApplication(name = "ChatBotClient", locators = @Locator, subscriptionEnabled = true)
@EnableContinuousQueries(poolName = "DEFAULT")
@EnableEntityDefinedRegions(basePackageClasses = Chat.class)
@EnableGemfireCaching
@EnableGemfireRepositories(basePackageClasses = ChatRepository.class)
@EnableScheduling
@SuppressWarnings("unused")
public class ChatBotClientApplication extends AbstractChatBotClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatBotClientApplication.class, args);
  }

  @Autowired
  private ChatService chatService;

  @PostConstruct
  public void postConstruct() {

    Optional.of(this.chatService)
      .filter(it -> it instanceof ChatBotService)
      .map(it -> (ChatBotService) it)
      .ifPresent(chatBotService -> chatBotService.receive(this::log));
  }
}
