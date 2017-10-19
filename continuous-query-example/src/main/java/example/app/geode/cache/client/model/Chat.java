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

package example.app.geode.cache.client.model;

import static org.cp.elements.lang.RuntimeExceptionsFactory.newIllegalArgumentException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.cp.elements.lang.Identifiable;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.Renderer;
import org.cp.elements.lang.StringUtils;
import org.cp.elements.util.ComparatorResultBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;

import example.app.model.Person;

/**
 * The {@link Chat} class is an Abstract Data Type (ADT) modeling a chat in a Instant Messenger (IM) tool
 * like Facebook Messenger or Slack, etc.
 *
 * @author John Blum
 * @param <ID> Class type of the identifier.
 * @see java.io.Serializable
 * @see java.time.LocalDateTime
 * @see org.cp.elements.lang.Identifiable
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.gemfire.mapping.annotation.Region
 * @since 1.0.0
 */
@Region("Chat")
@SuppressWarnings("unused")
public class Chat<ID extends Comparable<ID>> implements Comparable<Chat>, Identifiable<ID>, Serializable {

  private static final long serialVersionUID = -2605298490928082336L;

  private final LocalDateTime timestamp;

  @Id
  private ID id;

  private Object chatBotId;

  private final Person person;

  private final String message;

  public static Chat newChat(Person person, String message) {
    return new Chat(person, null, message);
  }

  public static Chat newChat(LocalDateTime timestamp, Person person, String message) {
    return new Chat(person, timestamp, message);
  }

  public Chat(Person person, LocalDateTime timestamp, String message) {

    this.person = Optional.ofNullable(person)
      .orElseThrow(() -> newIllegalArgumentException("Person is required"));

    this.timestamp = Optional.ofNullable(timestamp).orElse(LocalDateTime.now());

    this.message = Optional.ofNullable(message).filter(StringUtils::hasText)
      .orElseThrow(() -> newIllegalArgumentException("Message is required"));
  }

  public Object getChatBotId() {
    return this.chatBotId;
  }

  @Override
  public ID getId() {
    return this.id;
  }

  @Override
  public void setId(ID id) {
    this.id = id;
  }

  public Person getPerson() {
    return Person.newPerson(this.person);
  }

  public String getMessage() {
    return this.message;
  }

  public LocalDateTime getTimestamp() {
    return this.timestamp;
  }

  @Override
  @SuppressWarnings("unchecked")
  public int compareTo(Chat chat) {

    return ComparatorResultBuilder.<Comparable>create()
      .doCompare(this.getPerson(), chat.getPerson())
      .doCompare(this.getMessage(), chat.getMessage())
      .build();
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Chat)) {
      return false;
    }

    Chat that = (Chat) obj;

    return ObjectUtils.equals(this.getTimestamp(), that.getTimestamp())
      && ObjectUtils.equals(this.getPerson(), that.getPerson())
      && ObjectUtils.equals(this.getMessage(), that.getMessage());
  }

  @Override
  public int hashCode() {

    int hashValue = 17;

    hashValue = 37 * hashValue + ObjectUtils.hashCode(this.getTimestamp());
    hashValue = 37 * hashValue + ObjectUtils.hashCode(this.getPerson());
    hashValue = 37 * hashValue + ObjectUtils.hashCode(this.getMessage());

    return hashValue;
  }

  public String render(Renderer<Chat> renderer) {
    return renderer.render(this);
  }

  @Override
  public String toString() {
    return String.format("{ timestamp = %1$s, person = %2$s, message = %3$s, chatBotId = %4$s }",
      toString(getTimestamp()), getPerson(), getMessage(), getChatBotId());
  }

  protected String toString(LocalDateTime date) {

    return Optional.ofNullable(date)
      .map(it -> it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")))
      .orElse(null);
  }

  public Chat from(Object chatBotId) {
    this.chatBotId = chatBotId;
    return this;
  }
}
