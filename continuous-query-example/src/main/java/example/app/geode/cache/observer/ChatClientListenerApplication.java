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

package example.app.geode.cache.observer;

import java.util.Optional;

import org.apache.geode.cache.query.CqEvent;
import org.cp.elements.lang.SystemUtils;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication.Locator;
import org.springframework.data.gemfire.config.annotation.EnableContinuousQueries;
import org.springframework.data.gemfire.listener.annotation.ContinuousQuery;

import example.app.geode.cache.client.AbstractChatClientApplication;
import example.app.chat.model.Chat;

/**
 * The ChatClientListenerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@ClientCacheApplication(name = "ChatClientListener", locators = @Locator, subscriptionEnabled = true)
@EnableContinuousQueries
@SuppressWarnings("unused")
public class ChatClientListenerApplication extends AbstractChatClientApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(ChatClientListenerApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(args);

    SystemUtils.promptPressEnterToExit();
  }

  @ContinuousQuery(name = "ChatListener", query = "SELECT * FROM /Chat")
  public void receiveChat(CqEvent event) {

    Optional.ofNullable(event)
      .map(CqEvent::getNewValue)
      .filter(it -> it instanceof Chat)
      .map(it -> (Chat) it)
      .ifPresent(this::log);
  }
}
