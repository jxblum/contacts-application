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

package example.app.geode.cache.client.service.provider;

import static org.cp.elements.lang.RuntimeExceptionsFactory.newIllegalArgumentException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.geode.cache.query.CqEvent;
import org.apache.geode.internal.concurrent.ConcurrentHashSet;
import org.cp.elements.lang.Identifiable;
import org.cp.elements.lang.IdentifierSequence;
import org.cp.elements.lang.support.SimpleIdentifierSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.gemfire.listener.annotation.ContinuousQuery;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import example.app.geode.cache.client.bots.ChatBot;
import example.app.geode.cache.client.model.Chat;
import example.app.geode.cache.client.repo.ChatRepository;
import example.app.geode.cache.client.service.ChatService;
import example.app.model.Person;

/**
 * The ChatBotService class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Service("ChatBotService")
@SuppressWarnings("all")
public class ChatBotService implements ChatService {

  private final AtomicLong receiveCount = new AtomicLong(0L);
  private final AtomicLong sendCount = new AtomicLong(0L);

  private final ChatBot chatBot;

  @Autowired(required = false)
  @Qualifier("DespairDotCom")
  private ChatBot despairChatBot;

  private final ChatRepository chatRepository;

  private final IdentifierSequence<Long> identifierSequence;

  @Value("${example.app.chat.bot.id:ChatBotClient-0}")
  private Object chatBotId;

  private final Set<Consumer<Chat>> chatConsumers = new ConcurrentHashSet<>();

  private final Consumer<Chat> compositeChatConsumer = chat ->
    this.chatConsumers.stream().forEach(chatConsumer -> chatConsumer.accept(chat));

  public ChatBotService(@Qualifier("FamousQuotes") ChatBot chatBot, ChatRepository chatRepository) {

    this.chatBot = Optional.ofNullable(chatBot).filter(Objects::nonNull)
      .orElseThrow(() -> newIllegalArgumentException("ChatBot is required"));

    this.chatRepository = Optional.ofNullable(chatRepository).filter(Objects::nonNull)
      .orElseThrow(() -> newIllegalArgumentException("ChatRepository is required"));

    this.identifierSequence = newIdentifiderSequence();
  }

  /**
   * Returns a reference to the {@link ChatBot} used by this service to generate {@link Chat chats}.
   *
   * @return a reference to the {@link ChatBot} used by this service to generate {@link Chat chats}.
   * @see example.app.geode.cache.client.bots.ChatBot
   */
  protected ChatBot getChatBot() {
    return this.chatBot;
  }

  /**
   * Returns a reference to the {@link ChatRepository} used by this service to perform basic CRUD and query
   * data access operations on {@link Chat chats}.
   *
   * @return a reference to the {@link ChatRepository} used by this service.
   * @see ChatRepository
   */
  protected ChatRepository getChatRepository() {
    return this.chatRepository;
  }

  /**
   * Returns a reference to the {@link IdentifierSequence} used by this service to generate unique identifiers (ID)
   * for the {@link Chat chats}.
   *
   * @return a reference to the {@link IdentifierSequence} used by this service to generate unique identifiers (ID)
   * for the {@link Chat chats}.
   * @see org.cp.elements.lang.IdentifierSequence
   * @see java.lang.Long
   */
  protected IdentifierSequence<Long> getIdentifierSequence() {
    return this.identifierSequence;
  }

  /**
   * Sets the identifier (ID) of the given {@link Identifiable} object using the configured
   * {@link #getIdentifierSequence() IdentifierSequence}.
   *
   * @param <T> {@link Class} type of the ID.
   * @param identifiable {@link Identifiable} object to evaluate.
   * @return the given {@link Identifiable} object.
   * @see org.cp.elements.lang.Identifiable
   * @see #getIdentifierSequence()
   */
  protected <T extends Identifiable<Long>> T identify(T identifiable) {

    return Optional.ofNullable(identifiable)
      .filter(Identifiable::isNew)
      .map(it -> it.<T>identifiedBy(getIdentifierSequence().nextId()))
      .orElse(identifiable);
  }

  /**
   * Constructs a new instance of the {@link IdentifierSequence}.
   *
   * @return a new instance of the {@link IdentifierSequence}.
   * @see org.cp.elements.lang.IdentifierSequence
   */
  protected IdentifierSequence<Long> newIdentifiderSequence() {
    return new SimpleIdentifierSequence();
  }

  @Override
  public Chat send(Chat chat) {
    return incrementSendCount(getChatRepository().save(this.<Chat>identify(chat)));
  }

  @Override
  @CachePut(cacheNames = "Chat", key = "#result.id")
  public Chat send(Person person, String message) {
    return incrementSendCount(identify(Chat.newChat(person, message).using(String.format("Web%s", this.chatBotId))));
  }

  @Scheduled(initialDelay = 2000L, fixedRateString = "${example.app.chat.bot.schedule.rate:5000}")
  public void sendDespairQuote() {
    Optional.ofNullable(this.despairChatBot).ifPresent(chatBot -> send(chatBot.chat()));
  }

  @Scheduled(fixedRateString = "${example.app.chat.bot.schedule.rate:5000}")
  public void sendFamousQuote() {
    send(getChatBot().chat());
  }

  public long sendCount() {
    return this.sendCount.get();
  }

  private Chat incrementSendCount(Chat chat) {
    this.sendCount.incrementAndGet();
    return chat;
  }

  @Override
  public void receive(Consumer<Chat> chatConsumer) {
    Optional.ofNullable(chatConsumer).ifPresent(this.chatConsumers::add);
  }

  @ContinuousQuery(name = "ChatReceiver", query = "SELECT * FROM /Chat")
  public void receiveChat(CqEvent event) {

    this.receiveCount.incrementAndGet();

    Optional.ofNullable(event).map(it -> it.getNewValue())
      .ifPresent(chat -> this.compositeChatConsumer.accept((Chat) chat));
  }

  @Override
  public long receiveCount() {
    return this.receiveCount.get();
  }

  @Override
  public Iterable<Chat> findAll() {
    return getChatRepository().findAll();
  }

  @Override
  public Iterable<Chat> findAll(Person person) {
    return getChatRepository().findByPerson(person);
  }
}
