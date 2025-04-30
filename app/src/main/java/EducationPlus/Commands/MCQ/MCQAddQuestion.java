package EducationPlus.Commands.MCQ;

import EducationPlus.Classes.Helpers.Test;
import EducationPlus.Listeners.Ready;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.AttachmentProxy;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class MCQAddQuestion {
    public static SlashCommandData slashCommandData () {
        final SlashCommandData slashCommandData = Commands.slash ("mcq-add-question", "Adds a question to your Multiple-Choice Question Test.");
        slashCommandData.addOption (OptionType.STRING, "mcq-id", "What is the ID of your MCQ Test?", true);
        slashCommandData.addOption (OptionType.STRING, "question", "What should your question ask?", true);
        slashCommandData.addOption (OptionType.ATTACHMENT, "image", "What image should your question display?");
        slashCommandData.addOption (OptionType.STRING, "correct", "What is the correct answer to your question?", true);
        for (short index = 1; index <= 3; index += 1) {
            slashCommandData.addOption (OptionType.STRING, "wrong-" + index, "What is a wrong answer to your question", index == 1);
        }
        return slashCommandData;
    }

    public static void execute (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final OptionMapping mcqIDOptionMapping = slashCommandInteractionEvent.getOption ("mcq-id");
        final String mcqID = mcqIDOptionMapping.getAsString ();
        final Test.MCQ mcq = Test.MCQ.find (mcqID);
        if (mcq == null) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("I could not find a MCQ Test with ID:```" + mcqID + "```");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
            return;
        }
        final User user = slashCommandInteractionEvent.getUser ();
        final String userID = user.getId ();
        final boolean owner = mcqID.startsWith (userID);
        if (owner == false) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("You are not the owner of the MCQ Test " + mcq.title + " with ID:```" + mcq.id + "```");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
            return;
        }
        if (mcq.questions.length >= 4) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("You cannot have more than 4 questions on a MCQ Test.");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
            return;
        }
        final MessageCreateBuilder questionMessageCreateBuilder = new MessageCreateBuilder ();
        {
            final OptionMapping mcqQuestionOptionMapping = slashCommandInteractionEvent.getOption ("question");
            final String mcqQuestion = mcqQuestionOptionMapping.getAsString ();
            final OptionMapping mcqCorrectAnswerOptionMapping = slashCommandInteractionEvent.getOption ("correct");
            final String mcqCorrectAnswer = mcqCorrectAnswerOptionMapping.getAsString ();
            String content = mcqQuestion + "\n" + mcqCorrectAnswer;
            for (int wrongAnswer = 1; wrongAnswer <= 3; wrongAnswer += 1) {
                final OptionMapping mcqWrongAnswerOptionMapping = slashCommandInteractionEvent.getOption ("wrong-" + wrongAnswer);
                if (mcqWrongAnswerOptionMapping != null) {
                    content += "\n" + mcqWrongAnswerOptionMapping.getAsString ();
                }
            }
            questionMessageCreateBuilder.setContent (content);
        }
        final OptionMapping questionImageOptionMapping = slashCommandInteractionEvent.getOption ("image");
        if (questionImageOptionMapping != null) {
            final Message.Attachment questionPageAttachment = questionImageOptionMapping.getAsAttachment ();
            final AttachmentProxy attachmentProxy = questionPageAttachment.getProxy ();
                final CompletableFuture<InputStream> inputStreamCompletableFuture = attachmentProxy.download ();
                final InputStream inputStream = inputStreamCompletableFuture.join ();
                final String questionPageAttachmentFileName = questionPageAttachment.getFileName ();
                final FileUpload mcqQuestionImageFileUpload = FileUpload.fromData (inputStream, questionPageAttachmentFileName);
            questionMessageCreateBuilder.addFiles (mcqQuestionImageFileUpload);
        }
        final JDA jda = slashCommandInteractionEvent.getJDA ();
        final TextChannel lessonPagesTextChannel = jda.getTextChannelById (Ready.lessonPagesTextChannelID);
        final MessageCreateData questionMessageCreateData = questionMessageCreateBuilder.build ();
        final MessageCreateAction questionMessageCreateAction = lessonPagesTextChannel.sendMessage (questionMessageCreateData);
        questionMessageCreateAction.queue (questionMessage -> {
            final Test.MCQ.Question newQuestion = Test.MCQ.Question.fromMessage (questionMessage);
            final Test.MCQ.Question[] newMCQQuestions = new Test.MCQ.Question[mcq.questions.length + 1];
            for (short index = 0; index < mcq.questions.length; index += 1) {
                newMCQQuestions[index] = mcq.questions[index];
            }
            newMCQQuestions[mcq.questions.length] = newQuestion;
            mcq.questions = newMCQQuestions;
        });
    }
}
