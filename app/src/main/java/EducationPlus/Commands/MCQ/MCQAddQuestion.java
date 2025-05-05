package EducationPlus.Commands.MCQ;

import EducationPlus.Classes.Helpers.Test;
import EducationPlus.Listeners.Ready;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.AttachmentProxy;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class MCQAddQuestion {
    private static final String letters = "abcdefghijklmnopqrstuvwxyz";
    public static final SlashCommandData slashCommandData = slashCommandData ();

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
        String mcqQuestionText;
        {
            final OptionMapping mcqQuestionOptionMapping = slashCommandInteractionEvent.getOption ("question");
            mcqQuestionText = mcqQuestionOptionMapping.getAsString ();
            final OptionMapping mcqCorrectAnswerOptionMapping = slashCommandInteractionEvent.getOption ("correct");
            final String mcqCorrectAnswer = mcqCorrectAnswerOptionMapping.getAsString ();
            String content = mcqQuestionText + "\n" + mcqCorrectAnswer;
            for (int wrongAnswer = 1; wrongAnswer <= 3; wrongAnswer += 1) {
                final OptionMapping mcqWrongAnswerOptionMapping = slashCommandInteractionEvent.getOption ("wrong-" + wrongAnswer);
                if (mcqWrongAnswerOptionMapping != null) {
                    content += "\n" + mcqWrongAnswerOptionMapping.getAsString ();
                }
            }
            questionMessageCreateBuilder.setContent (content);
        }
        final OptionMapping questionImageOptionMapping = slashCommandInteractionEvent.getOption ("image");
        final EmbedBuilder responseEmbedBuilder = new EmbedBuilder ();
        if (questionImageOptionMapping != null) {
            final Message.Attachment questionPageAttachment = questionImageOptionMapping.getAsAttachment ();
            {
                final String url = questionPageAttachment.getUrl ();
                responseEmbedBuilder.setImage (url);
            }
            final AttachmentProxy attachmentProxy = questionPageAttachment.getProxy ();
            final CompletableFuture <InputStream> inputStreamCompletableFuture = attachmentProxy.download ();
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
        responseEmbedBuilder.setTitle ("Success");
        responseEmbedBuilder.setDescription ("Your question was successfully added to MCQ " + mcq.title + " with ID:```" + mcq.id + "```");
        responseEmbedBuilder.addField ("Question", mcqQuestionText, false);
        final MessageEmbed responseEmbed = responseEmbedBuilder.build ();
        final MessageCreateBuilder responseMessageCreateBuilder = new MessageCreateBuilder ();
        responseMessageCreateBuilder.setEmbeds (responseEmbed);
        {
            final JSONObject jsonObject = new JSONObject ();
            jsonObject.put ("command", "mcq-add-question");
            final String customID = jsonObject.toString ();
            final StringSelectMenu.Builder stringSelectMenuBuilder = StringSelectMenu.create (customID);
            final Test.MCQ.Question question = mcq.questions[mcq.questions.length - 1];
            final Emoji whiteCheckMarkEmoji = Emoji.fromUnicode ("✅");
            final Emoji xEmoji = Emoji.fromUnicode ("❌");
            stringSelectMenuBuilder.addOption (question.correct, question.correct, whiteCheckMarkEmoji);
            for (final String wrongAnswer : question.wrong) {
                stringSelectMenuBuilder.addOption (wrongAnswer, wrongAnswer, xEmoji);
            }
            final StringSelectMenu stringSelectMenu = stringSelectMenuBuilder.build ();
            responseMessageCreateBuilder.addActionRow (stringSelectMenu);
        }
        final MessageCreateData messageCreateData = responseMessageCreateBuilder.build ();
        final InteractionHook interactionHook = slashCommandInteractionEvent.getHook ();
        final WebhookMessageCreateAction <Message> messageWebhookMessageCreateAction = interactionHook.sendMessage (messageCreateData);
        messageWebhookMessageCreateAction.queue ();
    }
}
