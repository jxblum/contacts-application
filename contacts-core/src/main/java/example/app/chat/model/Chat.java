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

package example.app.chat.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.cp.elements.lang.Assert;
import org.cp.elements.lang.IdentifierSequence;
import org.cp.elements.lang.ObjectUtils;
import org.cp.elements.lang.Renderer;
import org.cp.elements.lang.support.IdentifiableAdapter;
import org.cp.elements.lang.support.UUIDIdentifierSequence;
import org.cp.elements.util.ComparatorResultBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;

import example.app.model.Person;

/**
 * The {@link Chat} class is an Abstract Data Type (ADT) modeling a chat in a Instant Messaging (IM) application
 * like Facebook Messenger, Slack, and so on.
 *
 * @author John Blum
 * @see java.io.Serializable
 * @see java.lang.Comparable
 * @see java.time.LocalDateTime
 * @see org.cp.elements.lang.support.IdentifiableAdapter
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.gemfire.mapping.annotation.Region
 * @since 1.0.0
 */
@Region("Chats")
@JsonIgnoreProperties(value = { "new", "notNew" }, ignoreUnknown = true)
@SuppressWarnings("unused")
public class Chat extends IdentifiableAdapter<String> implements Comparable<Chat>, Serializable {

  private static final long serialVersionUID = 6338077599686182454L;

  protected static final IdentifierSequence<String> identifierSequence = new UUIDIdentifierSequence();

  protected static final String CHAT_TO_STRING =
    "{ @type = %1$s, timestamp = %2$s, processId = %3$s, person = %4$s, message = %5$s }";

  /**
   * Factory method used to construct a new instance of {@link Chat} initialized with the given {@link Person}
   * who is sending the {@link String message}.
   *
   * @param person {@link Person} chatting.
   * @param message {@link String} containing the contents of the chat.
   * @return a new {@link Chat} initialized with the given {@link Person} sending the given {@link String message}.
   * @throws IllegalArgumentException if {@link Person} is {@literal null}
   * or {@link String message} is {@literal null} or empty.
   * @see example.app.model.Person
   * @see java.lang.String
   */
  public static Chat newChat(Person person, String message) {
    return new Chat(LocalDateTime.now(), person, message);
  }

  /**
   * Factory method used to construct a new instance of {@link Chat} initialized with the given {@link Person}
   * who is sending the {@link String message} at the given {@link LocalDateTime timestamp}.
   *
   * @param timestamp {@link LocalDateTime} specifying the date/time when the chat was sent.
   * @param person {@link Person} chatting.
   * @param message {@link String} containing the contents of the chat.
   * @return a new {@link Chat} initialized with the given {@link Person} sending the given {@link String message}.
   * @throws IllegalArgumentException if {@link Person} is {@literal null}
   * or {@link String message} is {@literal null} or empty.
   * @see example.app.model.Person
   * @see java.time.LocalDateTime
   * @see java.lang.String
   */
  public static Chat newChat(LocalDateTime timestamp, Person person, String message) {
    return new Chat(timestamp, person, message);
  }

  private LocalDateTime timestamp;

  @Id
  private final String id;

  private Object processId;

  private final Person person;

  private final String message;

  /**
   * Constructs a new instance of {@link Chat} initialized with the given {@link Person} who is sending
   * the {@link String message} at the given {@link LocalDateTime timestamp}.
   *
   * @param timestamp {@link LocalDateTime} specifying the date/time when the chat was sent.
   * @param person {@link Person} chatting.
   * @param message {@link String} containing the contents of the chat.
   * @throws IllegalArgumentException if {@link Person} is {@literal null}
   * or {@link String message} is {@literal null} or empty.
   * @see example.app.model.Person
   * @see java.time.LocalDateTime
   * @see java.lang.String
   */
  protected Chat(LocalDateTime timestamp, Person person, String message) {

    Assert.notNull(person, "Person is required");
    Assert.hasText(message, "Message is required");

    this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    this.person = person;
    this.message = message;
    this.id = identifierSequence.nextId();
  }

  @Override
  public String getId() {
    return this.id;
  }

  /**
   * Returns a {@link String} containing the contents of the chat.
   *
   * @return a {@link String} containing the contents of the chat.
   * @see java.lang.String
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * Returns an {@link Optional} {@link Object process identifier} used by the {@link #getPerson() person}
   * sending this {@link Chat}.
   *
   * The {@link Object process ID} maybe the name of an application or some other meta-data that identifies
   * how the {@link #getPerson() person} sent the chat.
   *
   * @return an {@link Optional} {@link Object process identifier} used by the {@link #getPerson() person}
   * sending this {@link Chat}.
   * @see java.util.Optional
   */
  public Optional<Object> getProcessId() {
    return Optional.ofNullable(this.processId);
  }

  /**
   * Returns the {@link Person} who sent the {@link Chat}.
   *
   * @return the {@link Person} who sent this {@link Chat}.
   * @see example.app.model.Person
   */
  public Person getPerson() {
    return Person.newPerson(this.person);
  }

  /**
   * Returns the {@link LocalDateTime date/time} when this {@link Chat} was sent.
   *
   * @return the {@link LocalDateTime date/time} when this {@link Chat} was sent.
   * @see java.time.LocalDateTime
   */
  public LocalDateTime getTimestamp() {
    return this.timestamp;
  }

  @Override
  @SuppressWarnings("unchecked")
  public int compareTo(Chat chat) {

    return ComparatorResultBuilder.<Comparable>create()
      .doCompare(this.getTimestamp(), chat.getTimestamp())
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

  @Override
  public String toString() {

    return String.format(CHAT_TO_STRING,
      getClass().getName(), toString(getTimestamp()), getProcessId(), getPerson(), getMessage());
  }

  protected String toString(LocalDateTime dateTime) {

    return Optional.ofNullable(dateTime)
      .map(it -> it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")))
      .orElse(null);
  }

  public String render(Renderer<Chat> renderer) {
    return renderer.render(this);
  }

  public Chat at(LocalDateTime timestamp) {
    this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    return this;
  }

  public Chat with(Object processId) {
    this.processId = processId;
    return this;
  }
}
