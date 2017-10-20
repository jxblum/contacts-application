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

package example.app.geode.cache.client.service;

import java.util.function.Consumer;

import example.app.geode.cache.client.model.Chat;
import example.app.model.Person;

/**
 * The {@link ChatService} interface defines a contract for implementing classes to send and receive
 * {@link Chat} messages.
 *
 * @author John Blum
 * @see java.util.function.Consumer
 * @see java.util.function.Supplier
 * @see java.util.stream.Stream
 * @see example.app.geode.cache.client.model.Chat
 * @see example.app.model.Person
 * @since 1.0.0
 */
public interface ChatService {

  /**
   * Sends a {@link Chat} from the given {@link Person} with the provided {@link String message}.
   *
   * @param person {@link Person} who sent the {@link Chat}.
   * @param message {@link String} containing the contents of the {@link Chat}.
   * @return a new instance of {@link Chat} from the given {@link Person}
   * containing the provided {@link String message}.
   * @see example.app.geode.cache.client.model.Chat
   * @see example.app.model.Person
   * @see java.lang.String
   */
  default Chat send(Person person, String message) {
    return send(Chat.newChat(person, message));
  }

  /**
   * Sends the given {@link Chat}.
   *
   * @param chat {@link Chat} to send.
   * @return the given {@link Chat}.
   * @see example.app.geode.cache.client.model.Chat
   */
  Chat send(Chat chat);

  long sendCount();

  /**
   * Registers the given {@link Consumer} to receive and consumer {@link Chat} messages from other
   * chat client applications and {@link Person users}.
   *
   * @param chatConsumer {@link Consumer} object used to process and handle the incoming {@link Chat} message.
   * @see example.app.geode.cache.client.model.Chat
   * @see java.util.function.Consumer
   */
  void receive(Consumer<Chat> chatConsumer);

  long receiveCount();

  /**
   * Finds all {@link Chat Chats} stored in the chat application.
   *
   * @return all {@link Chat Chats} stored in the chat application.
   * @see example.app.geode.cache.client.model.Chat
   * @see java.lang.Iterable
   */
  Iterable<Chat> findAll();

  /**
   * Finds all {@link Chat Chats} for the given {@link Person}.
   *
   * @param person {@link Person} to evaluate.
   * @return all {@link Chat Chats} for the given {@link Person}.
   * @see example.app.geode.cache.client.model.Chat
   * @see example.app.model.Person
   * @see java.lang.Iterable
   */
  Iterable<Chat> findAll(Person person);

}
