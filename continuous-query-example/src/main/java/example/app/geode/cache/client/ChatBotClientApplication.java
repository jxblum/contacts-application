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

import org.cp.elements.lang.Renderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication.Locator;
import org.springframework.data.gemfire.config.annotation.EnableContinuousQueries;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import example.app.geode.cache.client.bots.ChatBot;
import example.app.geode.cache.client.bots.provider.FamousQuotesChatBot;
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
@ClientCacheApplication(name = "ChatBotClient", locators = @Locator, subscriptionEnabled = true)
@EnableContinuousQueries(poolName = "DEFAULT")
@EnableEntityDefinedRegions(basePackageClasses = Chat.class)
@EnableGemfireRepositories(basePackageClasses = ChatRepository.class)
@EnableScheduling
@SuppressWarnings("unused")
public class ChatBotClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(ChatBotClientApplication.class, args);
  }

  @Value("${example.app.chat.bot.id:Client 0}")
  private Object clientId;

  @Bean
  public ChatBot chatBot() {
    return new FamousQuotesChatBot();
  }

  @Bean
  public ChatService chatService(ChatBot chatBot, ChatRepository chatRepository) {

    ChatBotService chatService = new ChatBotService(chatBot, chatRepository);

    chatService.receive(this::log);

    return chatService;
  }

  private void log(Chat chat) {

    Renderer<Chat> chatRender = it -> String.format("%1$s: %2$s", it.getPerson(), it.getMessage());

    log("%1$s - %2$s", this.clientId, chat.render(chatRender));
  }

  private void log(String message, Object... args) {
    System.out.printf("%s%n", String.format(message, args));
    System.out.flush();
  }
}
