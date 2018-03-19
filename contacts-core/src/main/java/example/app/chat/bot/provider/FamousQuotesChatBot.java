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

package example.app.chat.bot.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.cp.elements.lang.Assert;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import example.app.chat.bot.ChatBot;
import example.app.chat.model.Chat;
import example.app.chat.service.ChatService;
import example.app.model.Person;

/**
 * The {@link FamousQuotesChatBot} class is a {@link ChatBot} implementation that chats {@link String famous quotes}
 * from various {@link Person people}.
 *
 * @author John Blum
 * @see org.springframework.stereotype.Service
 * @see example.app.chat.bot.ChatBot
 * @see example.app.chat.service.ChatService
 * @see example.app.model.Person
 * @since 1.0.0
 */
@Service
@Qualifier("FamousQuotes")
@SuppressWarnings("unused")
public class FamousQuotesChatBot implements ChatBot {

	@SuppressWarnings("unchecked")
	private static final List<Chat> chats = Arrays.asList(
		Chat.newChat(Person.newCachedPerson("Aristotle", "(Greek)"), "Quality is not an act, it is a habit."),
		Chat.newChat(Person.newCachedPerson("Arthur", "Ashe"), "Start where you are. Use what you have. Do what you can."),
		Chat.newChat(Person.newCachedPerson("Benjamin", "Franklin"), "Well done is better than well said."),
		Chat.newChat(Person.newCachedPerson("Bertrand", "Russel"), "I would never die for my beliefs because I might be wrong."),
		Chat.newChat(Person.newCachedPerson("Bill", "Waterson"), "Reality continues to ruin my life."),
		Chat.newChat(Person.newCachedPerson("Bruce", "Lee"), "Mistakes are always forgivable, if one has the courage to admit them."),
		Chat.newChat(Person.newCachedPerson("Charles", "Dudley Warner"), "Everybody talks about the weather, but nobody does anything about it."),
		Chat.newChat(Person.newCachedPerson("Confucius", "(Chinese)"), "It does not matter how slowly you go as long as you do not stop."),
		Chat.newChat(Person.newCachedPerson("David", "Lee Roth"), "I used to jog but the ice cubes kept falling out of my glass."),
		Chat.newChat(Person.newCachedPerson("Don", "Marquis"), "Procrastination is the art of keeping up with yesterday."),
		Chat.newChat(Person.newCachedPerson("Eleanor", "Roosevelt"), "It is better to light a candle than curse the darkness."),
		Chat.newChat(Person.newCachedPerson("George", "Moore"), "A man travels the world over in search of what he needs and returns home to find it."),
		Chat.newChat(Person.newCachedPerson("Groucho", "Marx"), "Anyone who says he can see through women is missing a lot."),
		Chat.newChat(Person.newCachedPerson("Groucho", "Marx"), "I refuse to join any club that would have me as a member."),
		Chat.newChat(Person.newCachedPerson("H. G.", "Wells"), "If you fell down yesterday, stand up today."),
		Chat.newChat(Person.newCachedPerson("Henry", "Youngman"), "If you're going to do something tonight that you'll be sorry for tomorrow morning, sleep late."),
		Chat.newChat(Person.newCachedPerson("Isaac", "Asimov"), "People who think they know everything are a great annoyance to those of us who do."),
		Chat.newChat(Person.newCachedPerson("Jack", "Welch"), "Change before you have to."),
		Chat.newChat(Person.newCachedPerson("Jean-Luc", "Godard"), "To be or not to be. That's not really a question."),
		Chat.newChat(Person.newCachedPerson("Jim", "Rohn"), "Either you run the day or the day runs you."),
		Chat.newChat(Person.newCachedPerson("Johann", "Wolfgang von Goethe"), "Knowing is not enough; we must apply. Willing is not enough; we must do."),
		Chat.newChat(Person.newCachedPerson("John", "Burroughs"), "The smallest deed is better than the greatest intention."),
		Chat.newChat(Person.newCachedPerson("Jules", "Renard"), "Laziness is nothing more than the habit of resting before you get tired."),
		Chat.newChat(Person.newCachedPerson("Katherine", "Hepburn"), "Life is hard. After all, it kills you."),
		Chat.newChat(Person.newCachedPerson("Ken", "Kesey"), "You can't really be strong until you see a funny side to things."),
		Chat.newChat(Person.newCachedPerson("Lana", "Turner"), "A successful man is one who makes more money than his wife can spend. A successful woman is one who can find such a man."),
		Chat.newChat(Person.newCachedPerson("Mark", "Twain"), "All generalizations are false, including this one."),
		Chat.newChat(Person.newCachedPerson("Mark", "Twain"), "Don't let schooling interfere with your education."),
		Chat.newChat(Person.newCachedPerson("Mark", "Twain"), "Go to Heaven for the climate, Hell for the company."),
		Chat.newChat(Person.newCachedPerson("Martin", "Luther King Jr."), "Love is the only force capable of transforming an enemy into a friend."),
		Chat.newChat(Person.newCachedPerson("Maya", "Angelou"), "We may encounter many defeats but we must not be defeated."),
		Chat.newChat(Person.newCachedPerson("Milton", "Berle"), "A committee is a group that keeps minutes and loses hours."),
		Chat.newChat(Person.newCachedPerson("Mitch", "Hedberg"), "My fake plants died because I did not pretend to water them."),
		Chat.newChat(Person.newCachedPerson("Nelson", "Mandela"), "It always seems impossible until it's done."),
		Chat.newChat(Person.newCachedPerson("Nikos", "Kazantzakis"), "In order to succeed, we must first believe that we can."),
		Chat.newChat(Person.newCachedPerson("Oliver", "Herford"), "A woman's mind is cleaner than a man's; She changes it more often."),
		Chat.newChat(Person.newCachedPerson("Reba", "McEntire"), "To succeed in life, you need three things: a wishbone, a backbone and a funny bone."),
		Chat.newChat(Person.newCachedPerson("Reinhold", "Niebuhr"), "God grant me the serenity to accept the things I cannot change, the courage to change the things I can, and the wisdom to know the difference."),
		Chat.newChat(Person.newCachedPerson("Robert", "Benchley"), "Drawing on my fine command of the English language, I said nothing."),
		Chat.newChat(Person.newCachedPerson("Robin", "Williams"), "I'm sorry, if you were right, I'd agree with you."),
		Chat.newChat(Person.newCachedPerson("Samuel", "Beckett"), "Ever tried. Ever failed. No matter. Try Again. Fail again. Fail better."),
		Chat.newChat(Person.newCachedPerson("Samuel", "Beckett"), "We are all born mad. Some remain so."),
		Chat.newChat(Person.newCachedPerson("Stephan", "Hawking"), "Life would be tragic if it weren't funny."),
		Chat.newChat(Person.newCachedPerson("Theodore", "Roosevelt"), "Keep your eyes on the stars, and your feet on the ground."),
		Chat.newChat(Person.newCachedPerson("Thomas", "Edison"), "The chief function of the body is to carry the brain around."),
		Chat.newChat(Person.newCachedPerson("Unknown", "Unknown"), "A leader is one who knows the way, goes the way, and shows the way."),
		Chat.newChat(Person.newCachedPerson("Unknown", "Unknown"), "A lie gets halfway around the world before the truth has time to get its pants on."),
		Chat.newChat(Person.newCachedPerson("Unknown", "Unknown"), "Be happy for this moment. This moment is your life."),
		Chat.newChat(Person.newCachedPerson("Unknown", "Unknown"), "Give me a lever long enough and a fulcrum on which to place it and I shall move the world."),
		Chat.newChat(Person.newCachedPerson("Unknown", "Unknown"), "The greater danger for most of us lies not in setting our aim too high and falling short, but in setting our aim too low and achieving our mark."),
		Chat.newChat(Person.newCachedPerson("Unknown", "Unknown"), "The only thing worse than being blind is having sight but no vision."),
		Chat.newChat(Person.newCachedPerson("Virat", "Kohli"), "Self-belief and hard work will always earn you success."),
		Chat.newChat(Person.newCachedPerson("Walt", "Disney"), "The way to get started is to quit talking and begin doing."),
		Chat.newChat(Person.newCachedPerson("W. C.", "Fields"), "I cook with wine, sometimes I even add it to the food."),
		Chat.newChat(Person.newCachedPerson("W. H.", "Auden"), "We are all here on earth to help others; what on earth the others are here for I don't know."),
		Chat.newChat(Person.newCachedPerson("Will", "Rogers"), "Be thankful we're not getting all the government we're paying for."),
		Chat.newChat(Person.newCachedPerson("Will", "Rogers"), "Everything is funny, as long as it's happening to somebody else."),
		Chat.newChat(Person.newCachedPerson("William", "Lyon Phelps"), "If at first you don't succeed, find out if the loser gets anything."),
		Chat.newChat(Person.newCachedPerson("William", "Shakespear"), "We know what we are, but know not what we may be."),
		Chat.newChat(Person.newCachedPerson("Winston", "Churchill"), "I may be drunk, Miss, but in the morning I will be sober and you will still be ugly.")
	);

	private static List<String> findChatsBy(Person person) {

		return chats.stream()
			.filter(chat -> chat.getPerson().equals(person))
			.map(Chat::getMessage)
			.collect(Collectors.toList());
	}

	private final ChatService chatService;

	public FamousQuotesChatBot(ChatService chatService) {

		Assert.notNull(chatService, "ChatService is required");

		this.chatService = chatService;
	}

	protected ChatService getChatService() {
		return this.chatService;
	}

	@Override
	public String chat(Person person) {

		List<String> personChats = findChatsBy(person);

		Collections.shuffle(personChats);

		return personChats.isEmpty() ? "What?" : personChats.iterator().next();
	}


	private Person random() {

		List<Person> people = chats.stream()
			.map(Chat::getPerson)
			.collect(Collectors.toList());

		Collections.shuffle(people);

		return people.iterator().next();
	}

	@Scheduled(initialDelay = 5000L, fixedRateString = "${example.app.chat.bot.schedule.rate:5000}")
	public void sendChat() {

		Person person = random();

		getChatService().send(person, chat(person));
	}
}
