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

package example.app.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.geode.cache.GemFireCache;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.xml.GemfireConstants;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctionExecutions;
import org.springframework.data.gemfire.function.execution.GemfireOnServerFunctionTemplate;
import org.springframework.data.gemfire.function.support.SpringDefinedFunctionAwareRegistrar;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

import example.app.chat.model.Chat;
import example.app.chat.repo.ChatRepository;
import example.app.chat.util.ChatRenderer;
import example.app.client.function.SpellCheckerWithAutoCorrectFunctionExecution;
import example.app.geode.function.util.FunctionUtils;
import example.app.model.Person;
import example.app.server.function.SpellCheckerWithAutoCorrectFunction;
import example.app.server.function.SpellCheckerWithAutoCorrectFunction.FunctionResult;

/**
 * The {@link GeodeToSpringFunctionExampleApplication} class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@ClientCacheApplication(logLevel = "error")
@EnableEntityDefinedRegions(basePackageClasses = Chat.class)
@EnableGemfireFunctionExecutions(basePackageClasses = SpellCheckerWithAutoCorrectFunctionExecution.class)
@EnableGemfireRepositories(basePackageClasses = ChatRepository.class)
@SuppressWarnings("unused")
public class GeodeToSpringFunctionExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(GeodeToSpringFunctionExampleApplication.class, args);
  }

  @Bean
  ApplicationRunner runner(ChatRepository chatRepository,
      SpellCheckerWithAutoCorrectFunctionExecution spellCheckerWithAutoCorrectFunction) {

    return args -> {

      Person jonDoe = Person.newPerson("Jon", "Doe");
      Person janeDoe = Person.newPerson("Jane", "Doe");

      System.err.println("Saving chats...");

      Chat jonDoeChat = Chat.newChat(jonDoe, "What is the deel wth this xampl anyway?");

      chatRepository.save(jonDoeChat);

      Chat janeDoeChat = Chat.newChat(janeDoe, "I don't knw it's a duhd!");

      chatRepository.save(janeDoeChat);

      // Spell Check & Auto-Correct
      System.err.println("Spell checking & auto-correcting chats...");

      assertThat(FunctionUtils.<FunctionResult>extractSingleFunctionResult(spellCheckerWithAutoCorrectFunction.spellCheckWithAutoCorrect()))
        .isEqualTo(FunctionResult.SUCCESS);

      System.err.println("Asserting chats...");

      Chat correctedJonDoeChat = chatRepository.findById(jonDoeChat.getId()).orElse(null);

      assertThat(correctedJonDoeChat).isNotNull();
      assertThat(correctedJonDoeChat.getMessage()).isEqualTo("What is the deal with this example anyway?");

      System.err.println(correctedJonDoeChat.render(ChatRenderer.INSTANCE));

      Chat correctedJaneDoeChat = chatRepository.findById(janeDoeChat.getId()).orElse(null);

      assertThat(correctedJaneDoeChat).isNotNull();
      assertThat(correctedJaneDoeChat.getMessage()).isEqualTo("I don't know it's a dud!");

      System.err.println(correctedJaneDoeChat.render(ChatRenderer.INSTANCE));
      System.err.println("DONE!");
    };
  }

  @Bean
  @Profile("native")
  @SuppressWarnings("all")
  ApplicationListener<ContextRefreshedEvent> registerSpellCheckWithAutoCorrectFunction() {

    SpringDefinedFunctionAwareRegistrar springDefinedFunctionAwareRegistrar =
      new SpringDefinedFunctionAwareRegistrar();

    String[] functionArguments = {
      SpellCheckerWithAutoCorrectFunction.class.getPackage().getName(),
    };

    return contextRefreshedEvent -> {

      GemFireCache gemfireCache = contextRefreshedEvent.getApplicationContext()
        .getBean(GemfireConstants.DEFAULT_GEMFIRE_CACHE_NAME, GemFireCache.class);

      GemfireOnServerFunctionTemplate functionTemplate =
        new GemfireOnServerFunctionTemplate(gemfireCache);

      SpringDefinedFunctionAwareRegistrar.ResultStatus resultStatus = functionTemplate
        .executeAndExtract(springDefinedFunctionAwareRegistrar, functionArguments);

      assertThat(resultStatus).isEqualTo(SpringDefinedFunctionAwareRegistrar.ResultStatus.SUCCESS);
    };
  }

  @Configuration
  @EnableClusterConfiguration
  @Profile("!native")
  static class ClusterConfiguration { }

}
