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

package example.app.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import example.app.model.Contact;

/**
 * Spring Data {@link JpaRepository} interface for performing basic data access, CRUD and query operations on
 * {@link Contact} objects stored and managed in a relational database (e.g. an RDBMS such as MySQL).
 *
 * @author John Blum
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see org.springframework.stereotype.Repository
 * @see example.app.model.Contact
 * @since 1.0.0
 */
@SuppressWarnings("unused")
@Repository("jpaContactRepository")
public interface ContactRepository extends JpaRepository<Contact, Long> {

}
