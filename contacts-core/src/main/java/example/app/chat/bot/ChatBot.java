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

package example.app.chat.bot;

import example.app.model.Person;

/**
 * The {@link ChatBot} interface defines a contract for implementors to generate {@link String messages}
 * or {@link String chats} for a given {@link Person}.
 *
 * The {@link Person} may represent a mock {@link Person}, system or otherwise.
 *
 * @author John Blum
 * @see example.app.model.Person
 * @since 1.0.0
 */
public interface ChatBot {

	/**
	 * Generates a {@link String chat} for the given {@link Person}.
	 *
	 * @param person {@link Person} who chats.
	 * @return a {@link String} containing a generated chat for the given {@link Person}.
	 * @see example.app.model.Person
	 * @see java.lang.String
	 */
	String chat(Person person);

}
