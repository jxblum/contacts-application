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

package example.tests.spring.data.gemfire;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.client.ClientRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.data.gemfire.repository.Query;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests to assert the behavior of Spring Data (GemFire} Repository query method return values.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.apache.geode.cache.Region
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.query.QueryService
 * @see org.springframework.data.gemfire.GemfireTemplate
 * @see org.springframework.data.gemfire.config.annotation.ClientCacheApplication
 * @see org.springframework.data.repository.CrudRepository
 * @see org.springframework.test.context.junit4.SpringRunner
 * @see <a href="https://stackoverflow.com/questions/54703984/how-to-use-nvl-to-for-getting-data-from-gemfire-when-query-result-is-null">How to use NVL to get data from GemFire when query result is null</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("all")
public class NullValueReturningRepositoryQueryMethodIntegrationTests {

  private static final String GEMFIRE_LOG_LEVEL = "error";

  private static final String NULL_SAFE_PERSON_AGE_QUERY =
    "SELECT NVL(person.age, 0) FROM /People person WHERE person.name = $1";

  private static final String PERSON_AGE_QUERY =
    "SELECT person.age FROM /People person WHERE person.name = $1";

  private static final String TARGET_PERSON_AGE_QUERY = NULL_SAFE_PERSON_AGE_QUERY;

  @Autowired
  private GemfireTemplate peopleTemplate;

  @Autowired
  private PersonRepository personRepository;

  @Resource(name = "People")
  private org.apache.geode.cache.Region<String, Person> peopleRegion;

  @Test
  public void queryPersonByIdAndAge() throws Exception {

    Person jonDoe = Person.newPerson("Jon Doe").age(36);
    Person janeDoe = Person.newPerson("Jane Doe");

    this.personRepository.save(jonDoe);
    this.personRepository.save(janeDoe);

    Optional<Person> optionalJonDoe = this.personRepository.findById(jonDoe.getName());

    assertThat(optionalJonDoe.orElse(null)).isEqualTo(jonDoe);
    assertThat(this.personRepository.getAge(jonDoe.getName())).isEqualTo(jonDoe.getAge());

    Optional<Person> optionalJaneDoe = this.personRepository.findById(janeDoe.getName());

    assertThat(optionalJaneDoe.orElse(null)).isEqualTo(janeDoe);
    assertThat(this.personRepository.getAge(janeDoe.getName())).isEqualTo(0);

    // Query Age of Non-Existing Person (i.e. Bob Doe)
    Person bobDoe = Person.newPerson("Bob Doe");

    assertThat(this.personRepository.findById(bobDoe.getName()).orElse(null)).isNull();

    assertThat(queryAgeOfPersonUsingQueryService(bobDoe)).isNull();

    // Ambiguous
    //assertThat(queryAgeOfPersonUsingRepository(bobDoe)).is???

    assertThat(queryAgeOfPersonUsingTemplate(bobDoe)).isNull();
  }

  private Integer queryAgeOfPersonUsingQueryService(Person person) throws Exception {

    QueryService queryService = ((ClientCache) this.peopleRegion.getRegionService()).getLocalQueryService();

    org.apache.geode.cache.query.Query query = queryService.newQuery(TARGET_PERSON_AGE_QUERY);

    Object results = query.execute(person.getAge());

    assertThat(results).isInstanceOf(SelectResults.class);

    SelectResults<Integer> selectResults = (SelectResults<Integer>) results;

    // TODO - Is null really the correct return value here?  What does 'null' mean.
    return selectResults.isEmpty() ? null : selectResults.asList().get(0);
  }

  private Integer queryAgeOfPersonUsingRepository(Person person) {
    return this.personRepository.getAge(person.getName());
  }

  private Integer queryAgeOfPersonUsingTemplate(Person person) {

    SelectResults<Integer> results = this.peopleTemplate.find(TARGET_PERSON_AGE_QUERY, person.getName());

    assertThat(results).isNotNull();

    // TODO - Is null really the correct return value here?  What does 'null' mean?
    return results.isEmpty() ? null : results.asList().get(0);
  }

  @ClientCacheApplication(logLevel = GEMFIRE_LOG_LEVEL)
  static class GemFireConfiguration {

    @Bean("People")
    public ClientRegionFactoryBean<Object, Object> peopleRegion(ClientCache gemfireCache) {

      ClientRegionFactoryBean<Object, Object> peopleRegion = new ClientRegionFactoryBean<>();

      peopleRegion.setCache(gemfireCache);
      peopleRegion.setClose(false);
      peopleRegion.setShortcut(ClientRegionShortcut.LOCAL);

      return peopleRegion;
    }

    @Bean
    public GemfireTemplate peopleTemplate(ClientCache gemfireCache) {
      return new GemfireTemplate(gemfireCache.getRegion("/People"));
    }

    @Bean
    GemfireRepositoryFactoryBean<PersonRepository, Person, String> personRepository() {

      GemfireRepositoryFactoryBean personRepositoryFactoyBean =
        new GemfireRepositoryFactoryBean<>(PersonRepository.class);

      personRepositoryFactoyBean.setGemfireMappingContext(new GemfireMappingContext());

      return personRepositoryFactoyBean;
    }
  }

  @Data
  @Region("People")
  @ToString(of = "name")
  @EqualsAndHashCode(of = "name")
  @RequiredArgsConstructor(staticName = "newPerson")
  static class Person {

    private Integer age;

    @Id @NonNull
    private String name;

    public Person age(int age) {
      setAge(age);
      return this;
    }
  }

  interface PersonRepository extends CrudRepository<Person, String> {

    @Query(TARGET_PERSON_AGE_QUERY)
    Integer getAge(String personName);

  }
}
