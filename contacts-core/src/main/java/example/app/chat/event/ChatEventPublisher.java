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

package example.app.chat.event;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;

import org.cp.elements.lang.annotation.NullSafe;

/**
 * {@link ChatEventPublisher} is an abstract base class defining a contract for implementors
 * allowing {@link ChatListener ChatListeners} to be registered and unregistered as well as to be notified
 * of {@link ChatEvent ChatEvents} when {@link ChatEvent ChatEvents} are fired.
 *
 * @author John Blum
 * @see example.app.chat.event.ChatEvent
 * @see example.app.chat.event.ChatListener
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public abstract class ChatEventPublisher {

	private final CopyOnWriteArraySet<ChatListener<?>> chatListeners = new CopyOnWriteArraySet<>();

	@SuppressWarnings("unchecked")
	protected void fire(ChatEvent chatEvent) {
		this.chatListeners.forEach(chatListener -> chatListener.handle(chatEvent));
	}

	@NullSafe
	public boolean register(ChatListener<?> chatListener) {
		return Optional.ofNullable(chatListener).map(this.chatListeners::add).orElse(false);
	}

	public boolean unregister(ChatListener<?> chatListener) {
		return this.chatListeners.remove(chatListener);
	}
}
