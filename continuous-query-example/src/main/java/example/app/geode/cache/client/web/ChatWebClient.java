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

package example.app.geode.cache.client.web;

import static org.cp.elements.lang.RuntimeExceptionsFactory.newIllegalStateException;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import example.app.geode.cache.client.model.Chat;
import example.app.geode.cache.client.service.ChatService;
import example.app.model.Person;

/**
 * The {@link ChatWebClient} class is a Spring {@link RestController} implement the Web interface
 * to the chat client application.
 *
 * @author John Blum
 * @see org.springframework.web.bind.annotation.RestController
 * @see example.app.geode.cache.client.model.Chat
 * @see example.app.geode.cache.client.service.ChatService
 * @see example.app.model.Person
 * @since 1.0.0
 */
@RestController
@SuppressWarnings("unused")
public class ChatWebClient {

  @Autowired
  private ChatService chatService;

  protected ChatService getChatService() {
    return Optional.ofNullable(this.chatService)
      .orElseThrow(() -> newIllegalStateException("ChatService was not properly configured"));
  }

  @GetMapping("/chats")
  public Iterable<Chat> findAll() {
    return getChatService().findAll();
  }

  @GetMapping("/chats/{name}")
  public Iterable<Chat> findBy(@PathVariable("name") Person person) {
    return getChatService().findAll(person);
  }

  @GetMapping("/ping")
  public String ping() {
    return "PONG";
  }

  @GetMapping("/chats/count/received")
  public Long receiveCount() {
    return getChatService().receiveCount();
  }

  @GetMapping("/chats/count/sent")
  public Long sendCount() {
    return getChatService().sendCount();
  }

  // POST http://localhost:8080/chats?name=John_Blum&message=A_Long_Time_Ago_In_A_Galaxy_Far,_Far_Away...
  // POST http://localhost:8080/chats?name=John_Blum&message=Once_Upon_A_Time_The_End.
  @PostMapping("/chats")
  public void sendChat(@RequestParam("name") Person person, @RequestParam("message") String message) {
    getChatService().send(person, message.replaceAll("[\\s|_]+", " "));
  }
}
