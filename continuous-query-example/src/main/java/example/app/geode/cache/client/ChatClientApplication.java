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

import org.apache.geode.cache.client.ClientCache;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication.Locator;
import org.springframework.data.gemfire.config.annotation.EnableContinuousQueries;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;

import example.app.chat.bot.config.EnableChatBot;
import example.app.chat.model.Chat;
import example.app.chat.service.ChatService;

/**
 * The {@link ChatClientApplication} class is a {@link SpringBootApplication} as well as an Apache Geode
 * {@link ClientCache} application that functions as a chat client enabling senders/receivers to send
 * and receive chat messages back and forth.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.springframework.boot.ApplicationRunner
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableContinuousQueries
 * @see org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions
 * @see example.app.geode.cache.client.AbstractChatClientApplication
 * @see example.app.chat.bot.config.EnableChatBot
 * @since 1.0.0
 */
@SpringBootApplication
//@ClientCacheApplication(name = "ChatBotClient", durableClientId = "abc123", locators = @Locator,
//  readyForEvents = true, subscriptionEnabled = true)
@ClientCacheApplication(name = "ChatBotClient", locators = @Locator, subscriptionEnabled = true)
@EnableEntityDefinedRegions(basePackageClasses = Chat.class)
@EnableContinuousQueries
@EnableChatBot
@SuppressWarnings("unused")
public class ChatClientApplication extends AbstractChatClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatClientApplication.class, args);
  }

  @Bean
  ApplicationRunner runner(ChatService chatService) {

    return args ->
      Optional.ofNullable(chatService)
        .ifPresent(it -> it.register(chatEvent ->
          chatEvent.getChat()
            .filter(chat -> chat instanceof Chat)
            .map(chat -> (Chat) chat)
            .ifPresent(this::log)));
  }
}
