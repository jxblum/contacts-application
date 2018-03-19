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

package example.app.chat.repo;

import org.springframework.data.repository.CrudRepository;

import example.app.chat.model.Chat;
import example.app.model.Person;

/**
 * The {@link ChatRepository} interface is a Data Access Object (DAO) and Spring Data {@link CrudRepository}
 * used to perform basic CRUD and simple querying data access operations on {@link Chat Chats}.
 *
 * @author John Blum
 * @see java.lang.Long
 * @see org.springframework.data.repository.CrudRepository
 * @see example.app.chat.model.Chat
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public interface ChatRepository extends CrudRepository<Chat, String> {

  /**
   * Finds a {@link Iterable collection} of {@link Chat Chats} for given {@link Person}.
   *
   * @param person {@link Person} who's {@link Chat Chats} are searched.
   * @return a {@link Iterable collection} of {@link Chat Chats} for given {@link Person}.
   * @see example.app.chat.model.Chat
   * @see example.app.model.Person
   * @see java.lang.Iterable
   */
  Iterable<Chat> findByPerson(Person person);

}
