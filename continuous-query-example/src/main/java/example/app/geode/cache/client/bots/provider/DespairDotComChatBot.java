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

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import example.app.geode.cache.client.bots.ChatBot;
import example.app.geode.cache.client.model.Chat;
import example.app.model.Person;

/**
 * The DespairDotComChatBot class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Service("DespairDotComChatBot")
@Qualifier("DespairDotCom")
@SuppressWarnings("unused")
public class DespairDotComChatBot implements ChatBot {

  private static final Person despairInc = Person.newPerson("Despair", "Inc.");

  private static final List<Chat> chats = asList(
    Chat.newChat(despairInc, "Believe in Yourself - Because the rest of us think you're an idiot."),
    Chat.newChat(despairInc, "Consistency - It is only a virtue if you are not a screw up."),
    Chat.newChat(despairInc, "Consulting - If you are not part of the solution, there's good money to be made in prolonging the problem."),
    Chat.newChat(despairInc, "Distinction - Looking sharp is easy when you haven't done any work."),
    Chat.newChat(despairInc, "Foresight - Those who say it cannot be done just not interrupt those busy proving them right."),
    Chat.newChat(despairInc, "Get To Work - You aren't being paid to believe in the power of your dreams."),
    Chat.newChat(despairInc, "Government - If you think the problems we create are bad, just wait until you see our solutions."),
    Chat.newChat(despairInc, "Hope - May not be warranted at this point."),
    Chat.newChat(despairInc, "Idiocy - Never underestimate the power of stupid people in large groups."),
    Chat.newChat(despairInc, "Incompetence - When you earnestly believe you can compensate for a lack of skill by doubling your efforts, there is no end to what you can't do."),
    Chat.newChat(despairInc, "Interns - The experience we are giving you is invaluable. That's why we're not paying you anything."),
    Chat.newChat(despairInc, "Limitations - Until you spread your wings, you'll have no idea how far you can walk."),
    Chat.newChat(despairInc, "Meetings - None of us is as dumb as all of us."),
    Chat.newChat(despairInc, "Motivation - If a pretty poster and a cute saying are all it takes to motivate you, you probably have a very easy job. The kind robots will be doing soon."),
    Chat.newChat(despairInc, "Multitasking - The art of doing twice as much as you should half as well as you could."),
    Chat.newChat(despairInc, "Perseverance - The courage to ignore the obvious wisdom of turning back."),
    Chat.newChat(despairInc, "Potential - Not everyone gets to be an astronaut when they grow up."),
    Chat.newChat(despairInc, "Procrastination - Hard work often pays off after time, but laziness always pays off now."),
    Chat.newChat(despairInc, "Retirement - Because you've given so much of yourself to the company that you don't have anything left we can use."),
    Chat.newChat(despairInc, "Teams - Together we can do the work of one."),
    Chat.newChat(despairInc, "Teamwork - A few harmless flakes working together can unleash an avalanche of destruction."),
    Chat.newChat(despairInc, "Tradition - Just because you've always done it that doesn't mean it's not incredibly stupid."),
    Chat.newChat(despairInc, "Wishes - When you wish upon a star, your dreams can come true. Unless it's really a meteorite hurtling to the Earth which will destroy all life. Then you're pretty much hosed not matter what you wish for. Unless it is death by meteor.")
  );

  @SuppressWarnings("unused")
  @Value("${example.app.chat.bot.id:ChatBotClient-0}")
  private Object chatBotId;

  private Random randomIndexGenerator = new Random(System.currentTimeMillis());

  @Override
  public Chat chat() {
    return chats.get(this.randomIndexGenerator.nextInt(chats.size())).using(this.chatBotId);
  }

  @Override
  public Iterable<Chat> findAll(Person person) {
    return chats;
  }

  @Override
  public Chat findOne(Person person) {
    return chat();
  }
}
