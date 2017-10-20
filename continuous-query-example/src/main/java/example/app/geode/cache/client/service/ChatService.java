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

  default Chat send(Person person, String message) {
    return send(Chat.newChat(person, message));
  }

  Chat send(Chat chat);

  void receive(Consumer<Chat> chatConsumer);

  Iterable<Chat> findAll();

  Iterable<Chat> findAll(Person person);

}
