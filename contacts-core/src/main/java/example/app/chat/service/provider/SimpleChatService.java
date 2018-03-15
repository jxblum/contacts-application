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

package example.app.chat.service.provider;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.geode.cache.query.CqEvent;
import org.cp.elements.lang.Assert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.gemfire.listener.annotation.ContinuousQuery;
import org.springframework.stereotype.Service;

import example.app.chat.event.ChatEvent;
import example.app.chat.event.ChatEventPublisher;
import example.app.chat.model.Chat;
import example.app.chat.repo.ChatRepository;
import example.app.chat.service.ChatService;
import example.app.model.Person;

/**
 * The {@link SimpleChatService} class is a Spring {@link Service} class implementing a chat service to send chats.
 *
 * @author John Blum
 * @see CqEvent
 * @see org.cp.elements.lang.IdentifierSequence
 * @see org.springframework.data.gemfire.listener.annotation.ContinuousQuery
 * @see org.springframework.stereotype.Service
 * @see example.chat.client.model.Chat
 * @see example.chat.client.repo.ChatRepository
 * @see example.chat.event.ChatEvent
 * @see example.chat.event.ChatEventPublisher
 * @see example.chat.service.ChatService
 * @since 1.0.0
 */
@Service
@SuppressWarnings("unused")
public class SimpleChatService extends ChatEventPublisher implements ChatService {

	private final AtomicLong receiveCount = new AtomicLong(0L);
	private final AtomicLong sendCount = new AtomicLong(0L);

	private final ChatRepository chatRepository;

	@Value("${example.app.chat.client.process.id:ChatClient}")
	private Object processId;

	public SimpleChatService(ChatRepository chatRepository) {

		Assert.notNull(chatRepository, "ChatRepository is required");

		this.chatRepository = chatRepository;
	}

	protected ChatRepository getChatRepository() {
		return this.chatRepository;
	}

	@Override
	public Iterable<Chat> findAll() {
		return getChatRepository().findAll();
	}

	@Override
	public Iterable<Chat> findBy(Person person) {
		return getChatRepository().findByPerson(person);
	}

	public void send(Chat chat) {
		this.sendCount.incrementAndGet();
		getChatRepository().save(chat.with(this.processId));
	}

	@Override
	public long sendCount() {
		return this.sendCount.get();
	}

	@ContinuousQuery(name = "ChatReceiver", query = "SELECT * FROM /Chats")
	@SuppressWarnings("unchecked")
	public void receive(CqEvent cqEvent) {

		Optional.ofNullable(cqEvent)
			.map(CqEvent::getNewValue)
			.map(chat -> ChatEvent.newChatEvent(this).with(chat))
			.ifPresent(chatEvent -> {
				this.receiveCount.incrementAndGet();
				fire(chatEvent);
			});
	}

	@Override
	public long receiveCount() {
		return this.receiveCount.get();
	}
}
