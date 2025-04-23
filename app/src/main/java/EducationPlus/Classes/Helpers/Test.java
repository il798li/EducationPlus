package EducationPlus.Classes.Helpers;

import EducationPlus.Listeners.Ready;
import EducationPlus.Utility.JSONUtility;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
public class Test {
    public MessageCreateBuilder messageCreateBuilder (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        return null;
    }

    public static final ArrayList <Test> tests = new ArrayList <Test> ();

    public static void load (final JDA jda) {
        final JSONObject testsJSONObject = JSONUtility.load (JSONUtility.JSONFile.Tests);
        final JSONArray testsJSONArray = testsJSONObject.getJSONArray ("tests");
        final int length = testsJSONArray.length ();
        for (int index = 0; index < length; index += 1) {
            final JSONObject testJSONObject = testsJSONArray.getJSONObject (index);
            final boolean testType = testJSONObject.getBoolean ("mcq");
            if (testType) {
                final MCQ mcq = MCQ.fromJSON (testJSONObject, jda);
                tests.add (mcq);
            } else {
                final FRQ frq = FRQ.fromJSON (testJSONObject, jda);
                tests.add (frq);
            }
        }
    }

    public static class MCQ extends Test {
        public static final ArrayList <MCQ> mcqs = new ArrayList <MCQ> ();
        public final Question[] questions;
        public final String title;
        public final String description;
        public String id;
        public String subject;
        public Message.Attachment image;

        public MCQ (final String title, final String description, final Question[] questions, final String subject, final String id) {
            this.title = title;
            this.description = description;
            this.questions = questions;
            this.subject = subject;
            this.id = id;
            mcqs.add (this);
        }

        public static class Question {
            public String correct;
            public String[] wrong;
            public String question;

            public Question (final String question, final String correct, final String... wrong) {
                this.question = question;
                this.correct = correct;
                this.wrong = wrong;
            }

            public Question (final long messageID, final JDA jda) {
                final TextChannel lessonPagesTextChannel = jda.getTextChannelById (Ready.lessonPagesTextChannelID);
                final RestAction <Message> messageRestAction = lessonPagesTextChannel.retrieveMessageById (messageID);
                messageRestAction.queue (message -> {
                    final String content = message.getContentRaw ();
                    final String[] parts = content.split ("\n");
                    this.question = parts[0];
                    this.correct = parts[1];
                    this.wrong = new String[parts.length - 2];
                    for (int index = 2; index < parts.length; index += 1) {
                        this.wrong[index - 2] = parts[index];
                    }
                });
            }
        }

        public MessageCreateBuilder messageCreateBuilder (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
            return null;
        }

        public static MCQ fromJSON (final JSONObject jsonObject, final JDA jda) {
            return null;
        }
    }

    public static class FRQ extends Test {

        public MessageCreateBuilder messageCreateBuilder (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
            return null;
        }

        public static FRQ fromJSON (final JSONObject jsonObject, final JDA jda) {
            return null;
        }
    }
}
