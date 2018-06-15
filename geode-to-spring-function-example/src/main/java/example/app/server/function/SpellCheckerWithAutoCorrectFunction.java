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

package example.app.server.function;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;
import org.apache.geode.cache.execute.ResultSender;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryException;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.English;
import org.languagetool.rules.RuleMatch;
import org.springframework.data.gemfire.function.annotation.GemfireFunction;
import org.springframework.util.Assert;

import example.app.chat.model.Chat;

/**
 * The {@link SpellCheckerWithAutoCorrectFunction} class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SpellCheckerWithAutoCorrectFunction {

  protected static final String QUERY_STATEMENT = "SELECT * FROM /Chats c WHERE c.getTimestamp().isAfter($1)";

  private static final Object[] EMPTY_ARRAY = {};

  private final AtomicReference<LocalDateTime> lastRun = new AtomicReference<>(LocalDateTime.now());

  private Language language;

  @GemfireFunction(id = "spellCheckWithAutoCorrect", hasResult = true, optimizeForWrite = true)
  public FunctionResult spellCheckWithAutoCorrect(FunctionContext functionContext) {

    Assert.isInstanceOf(RegionFunctionContext.class, functionContext,
      "FunctionContext must be an instance of RegionFunctionContext");

    RegionFunctionContext regionFunctionContext = (RegionFunctionContext) functionContext;

    Region<String, Chat> chats = regionFunctionContext.getDataSet();

    ResultSender resultSender = regionFunctionContext.getResultSender();

    try {
      findChats(regionFunctionContext).forEach(chat -> {

        String message = chat.getMessage();
        String spellCheckedMessage = spellCheckAndAutoCorrect(message);

        Chat newChat = Chat.newChat(chat.getPerson(), spellCheckedMessage)
          .at(chat.getTimestamp())
          .identifiedBy(chat.getId());

        chats.put(newChat.getId(), newChat);
      });

      return FunctionResult.SUCCESS;
    }
    catch (QueryException cause) {
      throw new RuntimeException("Failed to execute Spell Check with Auto-Correct Function", cause);
    }
  }

  protected <T> T doOperationSafely(ExceptionThrowingOperation<T> operation) {

    try {
      return operation.doExceptionalOperation();
    }
    catch (Throwable cause) {
      throw new RuntimeException(cause);
    }
  }

  @SuppressWarnings("unchecked")
  protected List<Chat> findChats(RegionFunctionContext regionFunctionContext) throws QueryException {

    Query query = QueryBuilder.from(regionFunctionContext).build(QUERY_STATEMENT);

    Object[] queryArguments = resolveArguments(resolveTimestamp());

    SelectResults<Chat> chats = (SelectResults<Chat>) query.execute(regionFunctionContext, queryArguments);

    return chats.asList();
  }

  private Object[] resolveArguments(Object... arguments) {
    return arguments != null ? arguments : EMPTY_ARRAY;
  }

  private LocalDateTime resolveTimestamp() {
    return this.lastRun.getAndSet(LocalDateTime.now());
  }

  protected String spellCheckAndAutoCorrect(String text) {
    return autoCorrect(text, spellCheck(text));
  }

  protected List<RuleMatch> spellCheck(String text) {
    return doOperationSafely(() -> newLanguageTool().check(text));
  }

  protected String autoCorrect(String text, List<RuleMatch> ruleMatches) {

    String[] correctedText = { text };

    ruleMatches.forEach(ruleMatch -> {

      List<String> suggestedReplacements = ruleMatch.getSuggestedReplacements();

      int fromPosition = ruleMatch.getFromPos();
      int toPosition = ruleMatch.getToPos();

      if (!suggestedReplacements.isEmpty()) {

        String badWord = text.substring(fromPosition, toPosition);
        String newWord = findReplacement(badWord, suggestedReplacements);

        if (!newWord.equals(badWord)) {
          correctedText[0] = correctedText[0].replace(badWord, newWord);
        }
      }
    });

    return correctedText[0];
  }

  protected String findReplacement(String badWord, List<String> suggestedReplacements) {

    return Optional.ofNullable(suggestedReplacements)
      .filter(list -> !list.isEmpty())
      .map(list -> list.get(0))
      .orElse(badWord);
  }

  protected JLanguageTool newLanguageTool() {
    return configure(new JLanguageTool(resolveLanguage()));
  }

  protected JLanguageTool configure(JLanguageTool languageTool) {

    languageTool.getAllRules().stream()
      .filter(rule -> !rule.isDictionaryBasedSpellingRule())
      .forEach(nonDictionaryBasedSpellingRule ->
        languageTool.disableRule(nonDictionaryBasedSpellingRule.getId()));

    return languageTool;
  }

  protected synchronized Language resolveLanguage() {

    if (this.language == null) {
      this.language = newEnglish();
    }

    return this.language;
  }

  protected English newEnglish() {

    return Optional.of(Locale.getDefault())
      .filter(Locale.US::equals)
      .<English>map(it -> new AmericanEnglish())
      .orElseGet(BritishEnglish::new);
  }

  protected static class QueryBuilder {

    protected static QueryBuilder from(FunctionContext functionContext) {

      Assert.notNull(functionContext, "FunctionContext must not be null");

      return from(functionContext.getCache());
    }

    protected static QueryBuilder from(GemFireCache cache) {
      return new QueryBuilder(cache);
    }

    private final GemFireCache cache;

    private QueryBuilder(GemFireCache cache) {

      Assert.notNull(cache, "Cache must not be null");

      this.cache = cache;
    }

    protected GemFireCache getCache() {
      return this.cache;
    }

    protected QueryService resolveQueryService() {
      return getCache().getQueryService();
    }

    public Query build(String statement) {
      return resolveQueryService().newQuery(statement);
    }
  }

  public enum FunctionResult {

    SUCCESS,
    FAILURE,

  }

  @FunctionalInterface
  public interface ExceptionThrowingOperation<T> {

    T doExceptionalOperation() throws Throwable;

  }
}
