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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package example.app.model.support;

/**
 * The Identifiable interface defines a contract for an application domain model object
 * that can be uniquely identified with other objects of the same type.
 *
 * @author John Blum
 * @param <T> class type of the object's identifier.
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public interface Identifiable<T> {

	T getId();

	void setId(T id);

	@SuppressWarnings("unchecked")
	default <S extends Identifiable<T>> S identifiedBy(T id) {
		setId(id);
		return (S) this;
	}

	default boolean isNew() {
		return (getId() == null);
	}

	default boolean isNotNew() {
		return !isNew();
	}
}
