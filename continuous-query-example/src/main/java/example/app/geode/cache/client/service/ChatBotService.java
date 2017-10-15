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

package example.app.geode.cache.client.service;

import java.util.Optional;
import java.util.Random;

import org.apache.geode.cache.query.CqEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.listener.annotation.ContinuousQuery;
import org.springframework.stereotype.Service;

/**
 * The ChatBotService class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Service
@SuppressWarnings("all")
public class ChatBotService {

  @Autowired
  private GemfireTemplate chatRegionTemplate;

  private Random randomIndexGenerator = new Random(System.currentTimeMillis());

  private static final String[] DEFAULT_CHAT_MESSAGES = {
    "Nikos Kazantzakis: In order to succeed, we must first believe that we can.",
    "Confucious: It does not matter how slowly you go as long as you do not stop.",
    "Nelson Mandela: It always seems impossible until it's done.",
    "Arthur Ashe: Start where you are. Use what you have. Do what you can.",
    "Theodore Roosevelt: Keep your eyes on the stars, and your feet on the ground.",
    "Aristotle: Quality is not an act, it is a habit.",
    "H. G. Wells: If you fell down yesterday, stand up today.",
    "Benjamin Franklin: Well done is better than well said.",
    "Maya Angelou: We may encounter many defeats but we must not be defeated.",
    "Samuel Beckett: Ever tried. Ever failed. No matter. Try Again. Fail again. Fail better.",
    "Jim Rohn: Either you run the day or the day runs you.",
    "Unknown: Be happy for this moment. This moment is your life.",
    "Johann Wolfgang von Goethe: Knowing is not enough; we must apply. Willing is not enough; we must do.",
    "Walt Disney: The way to get started is to quit talking and begin doing.",
    "George A. Moore: A man travels the world over in search of what he needs and returns home to find it.",
    "John Burroughs: The smallest deed is better than the greatest intention.",
    "Jack Welch: Change before you have to.",
    "Unknown: A lie gets halfway around the world before the truth has time to get its pants on.",
    "Martin Luther King Jr.: Love is the only force capable of transforming an enemy into a friend.",
    "Mark Twain: Don't let schooling interfere with your education.",
    "Reinhold Niebuhr: God grant me the serenity to accept the things I cannot change, the courage to change the things I can, and the wisdom to know the difference.",
    "Unknown: The greater danger for most of us lies not in setting our aim too high and falling short, but in setting our aim too low and achieving our mark.",
    "Unknown: Give me a lever long enough and a fulcrum on which to place it and I shall move the world.",
    "Virat Kohli: Self-belief and hard work will always earn you success.",
    "Eleanor Roosevelt: It is better to light a candle than curse the darkness.",
    "Bruce Lee: Mistakes are always forgivable, if one has the courage to admit them.",
    "Unknown: A leader is one who knows the way, goes the way, and shows the way.",
    "Unknown: The only thing worse than being blind is having sight but no vision.",
    "William Shakespear: We know what we are, but know not what we may be.",
    "Reba McEntire: To succeed in life, you need three things: a wishbone, a backbone and a funny bone.",
    "Winston Churchill: I may be drunk, Miss, but in the morning I will be sober and you will still be ugly.",
    "Isaac Asimov: People who think they know everything are a great annoyance to those of us who do.",
    "Mitch Hedberg: My fake plants died because I did not pretend to water them.",
    "Oliver Herford: A woman's mind is cleaner than a man's; She changes it more often.",
    "W.H. Auden: We are all here on earth to help others; what on earth the others are here for I don't know.",
    "Henry Youngman: If you're going to do something tonight that you'll be sorry for tomorrow morning, sleep late.",
    "Will Rogers: Everything is funny, as long as it's happening to somebody else.",
    "Robin Williams: I'm sorry, if you were right, I'd agree with you.",
    "Don Marquis: Procrastination is the art of keeping up with yesterday.",
    "Mark Twain: Go to Heaven for the climate, Hell for the company.",
    "Katherine Hepburn: Life is hard. After all, it kills you.",
    "Jules Renard: Laziness is nothing more than the habit of resting before you get tired.",
    "Lana Turner: A successful man is one who makes more money than his wife can spend. A successful woman is one who can find such a man.",
    "W. C. Fields: I cook with wine, sometimes I even add it to the food.",
    "Stephan Hawking: Life would be tragic if it weren't funny.",
    "Ken Kesey: You can't really be strong until you see a funny side to things.",
    "William Lyon Phelps: If at first you don't succeed, find out if the loser gets anything.",
    "Thomas A. Edison: The chief function of the body is to carry the brain around.",
    "Groucho Marx: I refuse to join any club that would have me as a member.",
    "Robert Benchley: Drawing on my fine command of the English language, I said nothing.",
    "Mark Twain: All generalizations are false, including this one.",
    "Groucho Marx: Anyone who says he can see through women is missing a lot.",
    "Samuel Beckett: We are all born mad. Some remain so.",
    "Milton Berle: A committee is a group that keeps minutes and loses hours.",
    "Charles Dudley Warner: Everybody talks about the weather, but nobody does anything about it.",
    "David Lee Roth: I used to jog but the ice cubes kept falling out of my glass.",
    "Will Rogers: Be thankful we're not getting all the government we're paying for.",
    "Bill Waterson: Reality continues to ruin my life.",
    "Jean-Luc Godard: To be or not to be. That's not really a question.",
    "Bertrand Russel: I would never die for my beliefs because I might be wrong."
  };

  public String sendChat(Object clientId) {
    return sendChat(clientId, this.randomIndexGenerator.nextInt(DEFAULT_CHAT_MESSAGES.length));
  }

  public String sendChat(Object clientId, int index) {

    String chatMessage = String.format("%1$s - %2$s", clientId, DEFAULT_CHAT_MESSAGES[index]);

    this.chatRegionTemplate.put(index, chatMessage);

    return chatMessage;
  }

  @ContinuousQuery(name = "ChatMessages", query = "SELECT * FROM /Chats")
  public void receiveChat(CqEvent event) {
    Optional.ofNullable(event)
      .map(it -> it.getNewValue())
      .map(String::valueOf)
      .ifPresent(newValue -> log(newValue));
  }

  private void log(String message, Object... args) {
    System.out.printf("%s%n", String.format(message, args));
    System.out.flush();
  }
}
