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

package example.app.chat.bot.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import example.app.chat.bot.ChatBot;
import example.app.chat.bot.provider.DespairDotComChatBot;
import example.app.chat.bot.provider.FamousQuotesChatBot;
import example.app.chat.repo.ChatRepository;
import example.app.chat.service.ChatService;
import example.app.chat.service.provider.SimpleChatService;

/**
 * The {@link ChatBotConfiguration} class is a Spring {@link Configuration @Configuration} class
 * used to configure, register and enable a {@link ChatBot} in the Spring {@link ApplicationContext}.
 *
 * @author John Blum
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.annotation.Bean
 * @see org.springframework.context.annotation.Configuration
 * @see org.springframework.data.gemfire.repository.config.EnableGemfireRepositories
 * @see org.springframework.scheduling.annotation.EnableScheduling
 * @see example.app.chat.bot.ChatBot
 * @see example.app.chat.service.ChatService
 * @since 1.0.0
 */
@Configuration
@EnableGemfireRepositories(basePackageClasses = ChatRepository.class)
@EnableScheduling
@SuppressWarnings("unused")
public class ChatBotConfiguration {

	@Bean
	public ChatService chatService(ChatRepository chatRepository) {
		return new SimpleChatService(chatRepository);
	}

	@Bean
	public DespairDotComChatBot despairDotComChatBot(ChatService chatService) {
		return new DespairDotComChatBot(chatService);
	}

	@Bean
	public FamousQuotesChatBot famousQuotesChatBot(ChatService chatService) {
		return new FamousQuotesChatBot(chatService);
	}
}
