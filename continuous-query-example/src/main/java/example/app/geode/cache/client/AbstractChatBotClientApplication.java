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

import example.app.geode.cache.client.model.Chat;

/**
 * The {@link AbstractChatBotClientApplication} class...
 *
 * @author John Blum
 * @since 1.0.0
 */
public abstract class AbstractChatBotClientApplication {

  protected void log(Chat chat) {

    Renderer<Chat> chatRender = it -> String.format("[%1$s] %2$s: %3$s",
      it.getProcessId(), it.getPerson(), it.getMessage());

    log(chatRender.render(chat));
  }

  protected void log(String message, Object... args) {
    System.out.printf("%s%n", String.format(message, args));
    System.out.flush();
  }
}
