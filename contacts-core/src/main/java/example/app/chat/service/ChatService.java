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

package example.app.chat.service;

import example.app.chat.event.ChatEvent;
import example.app.chat.event.ChatListener;
import example.app.chat.model.Chat;
import example.app.model.Person;

/**
 * The {@link ChatService} interface defines a contract for implementors to send {@link String chats}
 * made by a {@link Person}.
 *
 * @author John Blum
 * @see example.app.model.Person
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public interface ChatService {

	/**
	 * Returns all {@link Chat chats} stored by the chat application.
	 *
	 * @return a {@link Iterable collection} of all {@link Chat chats} stored by the chat application.
	 * @see example.app.chat.model.Chat
	 * @see java.lang.Iterable
	 */
	Iterable<Chat> findAll();

	/**
	 * Searches for all {@link Chat chats} sent by a given {@link Person person}.
	 *
	 * @param person {@link Person} who is the subject of the search.
	 * @return a {@link Iterable collection} of all {@link Chat chats} sent by the given {@link Person person}.
	 * @see example.app.chat.model.Chat
	 * @see example.app.model.Person
	 * @see java.lang.Iterable
	 */
	Iterable<Chat> findBy(Person person);

	/**
	 * Records a tally of the number of {@link Chat chats} received by this chat client application.
	 *
	 * @return a tally of the number of {@link Chat chats} received by this chat client application.
	 */
	long receiveCount();

	/**
	 * Registers the given {@link ChatListener} for receiving {@link ChatEvent chat events}.
	 *
	 * @param chatListener {@link ChatListener} to register.
	 * @return a boolean value indicating whether the given {@link ChatListener} was successfully registered.
	 * @see example.app.chat.event.ChatListener
	 */
	boolean register(ChatListener<?> chatListener);

	/**
	 * Sends the given {@link String message} from the {@link Person} in a chat.
	 *
	 * @param person {@link Person} who is sending the chat.
	 * @param message {@link String} containing the contents of the chat.
	 * @see example.app.model.Person
	 * @see java.lang.String
	 * @see #send(Chat)
	 */
	default void send(Person person, String message) {
		send(Chat.newChat(person, message));
	}

	/**
	 * Sends the given {@link Chat}.
	 *
	 * @param chat {@link Chat} to send.
	 * @see example.app.chat.model.Chat
	 */
	void send(Chat chat);

	/**
	 * Returns a tally of the number of {@link Chat chats} send from this chat client application.
	 *
	 * @return a tally of the number of {@link Chat chats} send from this chat client application.
	 */
	long sendCount();

	/**
	 * Unregisters the given {@link ChatListener} for receiving {@link ChatEvent chat events}.
	 *
	 * @param chatListener {@link ChatListener} to unregister.
	 * @return a boolean value indicating whether the given {@link ChatListener} was successfully unregistered.
	 * @see example.app.chat.event.ChatListener
	 */
	boolean unregister(ChatListener<?> chatListener);

}
