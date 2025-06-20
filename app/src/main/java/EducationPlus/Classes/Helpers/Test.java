package EducationPlus.Classes.Helpers;

import EducationPlus.Listeners.Ready;
import EducationPlus.Utility.DiscordUtility;
import EducationPlus.Utility.JSONUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
public class Test {
    public MessageCreateBuilder messageCreateBuilder (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        return null;
    }

    public static final ArrayList <Test> tests = new ArrayList <Test> ();

    public static void load (final JDA jda) {
        final JSONObject testsJSONObject = JSONUtility.load (JSONUtility.JSONFile.Tests);
        final JSONArray testsJSONArray = testsJSONObject.getJSONArray ("mcqs");
        final int length = testsJSONArray.length ();
        for (int index = 0; index < length; index += 1) {
            final JSONObject testJSONObject = testsJSONArray.getJSONObject (index);
            final MCQ mcq = MCQ.fromJSON (testJSONObject, jda);
            tests.add (mcq);
        }
    }

    public static void save () {
        final JSONObject testsJSONObject = new JSONObject ();
        final JSONArray mcqsJSONArray = new JSONArray ();
        for (final MCQ mcq : MCQ.mcqs) {
            final JSONObject mcqJSONObject = mcq.toJSON ();
            mcqsJSONArray.put (mcqJSONObject);
        }
        testsJSONObject.put ("mcqs", mcqsJSONArray);
        JSONUtility.save (testsJSONObject, JSONUtility.JSONFile.Tests);
    }

    public static class MCQ extends Test {
        public static final ArrayList <MCQ> mcqs = new ArrayList <MCQ> ();
        public Question[] questions;
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
            public List <String> answers;
            public Message.Attachment image;
            public long messageID;

            public Question (final String question, final String correct, final String... wrong) {
                this.question = question;
                this.correct = correct;
                this.wrong = wrong;
                this.answers = new ArrayList <String> ();
                answers.add (correct);
                for (final String wrongAnswer : wrong) {
                    answers.add (wrongAnswer);
                }
                Collections.shuffle (this.answers);
            }

            public static Question fromMessageID (final long messageID, final JDA jda) {
                final TextChannel lessonPagesTextChannel = jda.getTextChannelById (Ready.lessonPagesTextChannelID);
                final RestAction <Message> messageRestAction = lessonPagesTextChannel.retrieveMessageById (messageID);
                final Question[] question = {null};
                messageRestAction.queue (message -> {
                    question[0] = fromMessage (message);
                });
                return question[0];
            }

            public static Question fromMessage (final Message message) {
                final String content = message.getContentRaw ();
                final String[] parts = content.split ("\n");
                final String questionText = parts[0];
                final String correct = parts[1];
                final String[] wrong = new String[parts.length - 2];
                for (int index = 2; index < parts.length; index += 1) {
                    wrong[index - 2] = parts[index];
                }
                final List <Message.Attachment> attachmentsList = message.getAttachments ();
                final int attachmentsListLength = attachmentsList.size ();
                final Question question = new Question (questionText, correct, wrong);
                if (attachmentsListLength >= 1) {
                    question.image = attachmentsList.get (0);
                }
                question.messageID = message.getIdLong ();
                return question;
            }
        }

        public static class Attempt {
            private final long userID;
            private final String mcqID;
            public final MCQ mcq;
            public final short[] answers;
            private final long messageID;
            public int questionIndex;

            public static Set <Attempt> attempts = new HashSet <Attempt> ();

            public Attempt (final long userID, final String mcqID, final short[] answers, final long messageID) {
                this.userID = userID;
                this.mcqID = mcqID;
                this.answers = answers;
                this.messageID = messageID;
                this.questionIndex = 1;
                this.mcq = MCQ.find (mcqID);
                attempts.add (this);
            }

            public static Attempt find (final long messageID) {
                for (final Attempt attempt : attempts) {
                    if (attempt.messageID == messageID) {
                        return attempt;
                    }
                }
                return null;
            }
        }

        public static MCQ fromJSON (final JSONObject jsonObject, final JDA jda) {
            final String mcqID = jsonObject.getString ("id");
            final String title = jsonObject.getString ("title");
            final String description = jsonObject.getString ("description");
            final String subject = jsonObject.getString ("subject");
            final JSONArray questionMessageIDsJSONArray = jsonObject.getJSONArray ("questions");
            final int size = questionMessageIDsJSONArray.length ();
            final Question[] mcqQuestions = new Question[size];
            for (int index = 0; index < size; index += 1) {
                final long questionMessageID = questionMessageIDsJSONArray.getLong (index);
                final Question question = Question.fromMessageID (questionMessageID, jda);
                mcqQuestions[index] = question;
            }
            return new MCQ (title, description, mcqQuestions, subject, mcqID);
        }

        public JSONObject toJSON () {
            final JSONObject jsonObject = new JSONObject ();
            jsonObject.put ("id", this.id);
            jsonObject.put ("title", this.title);
            jsonObject.put ("description", this.description);
            jsonObject.put ("subject", this.subject);
            final JSONArray questionsJSONArray = new JSONArray ();
            for (final Question question : this.questions) {
                questionsJSONArray.put (question.messageID);
            }
            jsonObject.put ("questions", questionsJSONArray);
            jsonObject.put ("mcq", true);
            return jsonObject;
        }

        public static MCQ find (final String mcqID) {
            for (final MCQ mcq : mcqs) {
                final boolean matchingID = mcq.id.equals (mcqID);
                if (matchingID) {
                    return mcq;
                }
            }
            return null;
        }

        public MessageCreateBuilder messageCreateBuilder (final int questionIndex) {
            final Question question = this.questions[questionIndex];
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setDescription (question.question);
            if (this.image != null) {
                final String attachmentURL = this.image.getUrl ();
                embedBuilder.setImage (attachmentURL);
            }
            embedBuilder.setColor (DiscordUtility.blurple);
            final MessageEmbed messageEmbed = embedBuilder.build ();
            final MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder ();
            messageCreateBuilder.setEmbeds (messageEmbed);
            return messageCreateBuilder;
        }

        public String data (final int questionIndex) {
            final JSONObject jsonObject = new JSONObject ();
            final Question question = this.questions[questionIndex];
            jsonObject.put ("mcq", this.id);
            jsonObject.put ("question", questionIndex);
            return jsonObject.toString ();
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
