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

package example.app.geode.cache.client.bots.provider;

import static java.util.Arrays.asList;
import static java.util.stream.StreamSupport.stream;
import static org.cp.elements.lang.RuntimeExceptionsFactory.newIllegalStateException;
import static org.cp.elements.util.CollectionUtils.asSet;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import example.app.geode.cache.client.bots.ChatBot;
import example.app.geode.cache.client.model.Chat;
import example.app.model.Person;

/**
 * The FamousQuotesChatBot class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Service("FamousQuotesChatBot")
@Qualifier("FamousQuotes")
@SuppressWarnings("unused")
public class FamousQuotesChatBot implements ChatBot {

  private static final Iterable<Person> people = asSet(
    Person.newPerson("Aristotle", "(Greek)"),
    Person.newPerson("Benjamin", "Franklin"),
    Person.newPerson("Arthur", "Ashe"),
    Person.newPerson("Bertrand", "Russel"),
    Person.newPerson("Bill", "Waterson"),
    Person.newPerson("Bruce", "Lee"),
    Person.newPerson("Charles", "Dudley Warner"),
    Person.newPerson("Confucius", "(Chinese)"),
    Person.newPerson("David", "Lee Roth"),
    Person.newPerson("Don", "Marquis"),
    Person.newPerson("Eleanor", "Roosevelt"),
    Person.newPerson("George", "Moore"),
    Person.newPerson("Groucho", "Marx"),
    Person.newPerson("H. G.", "Wells"),
    Person.newPerson("Henry", "Youngman"),
    Person.newPerson("Isaac", "Asimov"),
    Person.newPerson("Jack", "Welch"),
    Person.newPerson("Jean-Luc", "Godard"),
    Person.newPerson("Jim", "Rohn"),
    Person.newPerson("Johann", "Wolfgang von Goethe"),
    Person.newPerson("John", "Burroughs"),
    Person.newPerson("Jules", "Renard"),
    Person.newPerson("Katherine", "Hepburn"),
    Person.newPerson("Ken", "Kesey"),
    Person.newPerson("Lana", "Turner"),
    Person.newPerson("Mark", "Twain"),
    Person.newPerson("Martin", "Luther King Jr."),
    Person.newPerson("Maya", "Angelou"),
    Person.newPerson("Milton", "Berle"),
    Person.newPerson("Mitch", "Hedberg"),
    Person.newPerson("Nelson", "Mandela"),
    Person.newPerson("Nikos", "Kazantzakis"),
    Person.newPerson("Oliver", "Herford"),
    Person.newPerson("Reba", "McEntire"),
    Person.newPerson("Reinhold", "Niebuhr"),
    Person.newPerson("Robert", "Benchley"),
    Person.newPerson("Robin", "Williams"),
    Person.newPerson("Samuel", "Beckett"),
    Person.newPerson("Stephan", "Hawking"),
    Person.newPerson("Theodore", "Roosevelt"),
    Person.newPerson("Thomas", "Edison"),
    Person.newPerson("Unknown", "Unknown"),
    Person.newPerson("Virat", "Kohli"),
    Person.newPerson("Walt", "Disney"),
    Person.newPerson("W. C.", "Fields"),
    Person.newPerson("W. H.", "Auden"),
    Person.newPerson("Will", "Rogers"),
    Person.newPerson("William", "Lyon Phelps"),
    Person.newPerson("William", "Shakespear"),
    Person.newPerson("Winston", "Churchill")
  );

  @SuppressWarnings("unchecked")
  private static final List<Chat> chats = asList(
    Chat.newChat(findPerson("Aristotle (Greek)"), "Quality is not an act, it is a habit."),
    Chat.newChat(findPerson("Arthur Ashe"), "Start where you are. Use what you have. Do what you can."),
    Chat.newChat(findPerson("Benjamin Franklin"), "Well done is better than well said."),
    Chat.newChat(findPerson("Bertrand Russel"), "I would never die for my beliefs because I might be wrong."),
    Chat.newChat(findPerson("Bill Waterson"), "Reality continues to ruin my life."),
    Chat.newChat(findPerson("Bruce Lee"), "Mistakes are always forgivable, if one has the courage to admit them."),
    Chat.newChat(findPerson("Charles Dudley Warner"), "Everybody talks about the weather, but nobody does anything about it."),
    Chat.newChat(findPerson("Confucius (Chinese)"), "It does not matter how slowly you go as long as you do not stop."),
    Chat.newChat(findPerson("David Lee Roth"), "I used to jog but the ice cubes kept falling out of my glass."),
    Chat.newChat(findPerson("Don Marquis"), "Procrastination is the art of keeping up with yesterday."),
    Chat.newChat(findPerson("Eleanor Roosevelt"), "It is better to light a candle than curse the darkness."),
    Chat.newChat(findPerson("H. G. Wells"), "If you fell down yesterday, stand up today."),
    Chat.newChat(findPerson("Henry Youngman"), "If you're going to do something tonight that you'll be sorry for tomorrow morning, sleep late."),
    Chat.newChat(findPerson("Isaac Asimov"), "People who think they know everything are a great annoyance to those of us who do."),
    Chat.newChat(findPerson("George Moore"), "A man travels the world over in search of what he needs and returns home to find it."),
    Chat.newChat(findPerson("Groucho Marx"), "Anyone who says he can see through women is missing a lot."),
    Chat.newChat(findPerson("Groucho Marx"), "I refuse to join any club that would have me as a member."),
    Chat.newChat(findPerson("Jack Welch"), "Change before you have to."),
    Chat.newChat(findPerson("Jean-Luc Godard"), "To be or not to be. That's not really a question."),
    Chat.newChat(findPerson("Jim Rohn"), "Either you run the day or the day runs you."),
    Chat.newChat(findPerson("Johann Wolfgang von Goethe"), "Knowing is not enough; we must apply. Willing is not enough; we must do."),
    Chat.newChat(findPerson("John Burroughs"), "The smallest deed is better than the greatest intention."),
    Chat.newChat(findPerson("Jules Renard"), "Laziness is nothing more than the habit of resting before you get tired."),
    Chat.newChat(findPerson("Katherine Hepburn"), "Life is hard. After all, it kills you."),
    Chat.newChat(findPerson("Ken Kesey"), "You can't really be strong until you see a funny side to things."),
    Chat.newChat(findPerson("Lana Turner"), "A successful man is one who makes more money than his wife can spend. A successful woman is one who can find such a man."),
    Chat.newChat(findPerson("Maya Angelou"), "We may encounter many defeats but we must not be defeated."),
    Chat.newChat(findPerson("Mark Twain"), "All generalizations are false, including this one."),
    Chat.newChat(findPerson("Mark Twain"), "Don't let schooling interfere with your education."),
    Chat.newChat(findPerson("Mark Twain"), "Go to Heaven for the climate, Hell for the company."),
    Chat.newChat(findPerson("Martin Luther King Jr."), "Love is the only force capable of transforming an enemy into a friend."),
    Chat.newChat(findPerson("Milton Berle"), "A committee is a group that keeps minutes and loses hours."),
    Chat.newChat(findPerson("Mitch Hedberg"), "My fake plants died because I did not pretend to water them."),
    Chat.newChat(findPerson("Nelson Mandela"), "It always seems impossible until it's done."),
    Chat.newChat(findPerson("Nikos Kazantzakis"), "In order to succeed, we must first believe that we can."),
    Chat.newChat(findPerson("Oliver Herford"), "A woman's mind is cleaner than a man's; She changes it more often."),
    Chat.newChat(findPerson("Reinhold Niebuhr"), "God grant me the serenity to accept the things I cannot change, the courage to change the things I can, and the wisdom to know the difference."),
    Chat.newChat(findPerson("Reba McEntire"), "To succeed in life, you need three things: a wishbone, a backbone and a funny bone."),
    Chat.newChat(findPerson("Robert Benchley"), "Drawing on my fine command of the English language, I said nothing."),
    Chat.newChat(findPerson("Robin Williams"), "I'm sorry, if you were right, I'd agree with you."),
    Chat.newChat(findPerson("Samuel Beckett"), "Ever tried. Ever failed. No matter. Try Again. Fail again. Fail better."),
    Chat.newChat(findPerson("Samuel Beckett"), "We are all born mad. Some remain so."),
    Chat.newChat(findPerson("Stephan Hawking"), "Life would be tragic if it weren't funny."),
    Chat.newChat(findPerson("Theodore Roosevelt"), "Keep your eyes on the stars, and your feet on the ground."),
    Chat.newChat(findPerson("Thomas Edison"), "The chief function of the body is to carry the brain around."),
    Chat.newChat(findPerson("Unknown Unknown"), "A leader is one who knows the way, goes the way, and shows the way."),
    Chat.newChat(findPerson("Unknown Unknown"), "A lie gets halfway around the world before the truth has time to get its pants on."),
    Chat.newChat(findPerson("Unknown Unknown"), "Be happy for this moment. This moment is your life."),
    Chat.newChat(findPerson("Unknown Unknown"), "Give me a lever long enough and a fulcrum on which to place it and I shall move the world."),
    Chat.newChat(findPerson("Unknown Unknown"), "The greater danger for most of us lies not in setting our aim too high and falling short, but in setting our aim too low and achieving our mark."),
    Chat.newChat(findPerson("Unknown Unknown"), "The only thing worse than being blind is having sight but no vision."),
    Chat.newChat(findPerson("Virat Kohli"), "Self-belief and hard work will always earn you success."),
    Chat.newChat(findPerson("Walt Disney"), "The way to get started is to quit talking and begin doing."),
    Chat.newChat(findPerson("W. C. Fields"), "I cook with wine, sometimes I even add it to the food."),
    Chat.newChat(findPerson("W. H. Auden"), "We are all here on earth to help others; what on earth the others are here for I don't know."),
    Chat.newChat(findPerson("Will Rogers"), "Be thankful we're not getting all the government we're paying for."),
    Chat.newChat(findPerson("Will Rogers"), "Everything is funny, as long as it's happening to somebody else."),
    Chat.newChat(findPerson("William Lyon Phelps"), "If at first you don't succeed, find out if the loser gets anything."),
    Chat.newChat(findPerson("William Shakespear"), "We know what we are, but know not what we may be."),
    Chat.newChat(findPerson("Winston Churchill"), "I may be drunk, Miss, but in the morning I will be sober and you will still be ugly.")
  );

  protected static Person findPerson(String name) {

    return stream(people.spliterator(), false).filter(person -> person.getName().equals(name)).findFirst()
      .orElseThrow(() -> newIllegalStateException("Unable to find Person with name [%s]", name));
  }

  public Iterable<Chat> findAll(Person person) {
    return chats.stream().filter(chat -> chat.getPerson().equals(person)).collect(Collectors.toSet());
  }

  public Chat findOne(Person person) {
    return stream(findAll(person).spliterator(), false).findFirst().orElse(null);
  }

  @SuppressWarnings("unused")
  @Value("${example.app.chat.bot.id:ChatBot 0}")
  private Object chatBotId;

  private Random randomIndexGenerator = new Random(System.currentTimeMillis());

  @Override
  public Chat chat() {
    return chats.get(this.randomIndexGenerator.nextInt(chats.size())).using(this.chatBotId);
  }
}
