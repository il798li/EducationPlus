package EducationPlus.Commands.MCQ;

import EducationPlus.Classes.Helpers.Test;
import EducationPlus.Main;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
public class MCQAttempt {
    public static SlashCommandData slashCommandData () {
        final SlashCommandData slashCommandData = Commands.slash ("mcq-attempt", "Attempts an MCQ Test to see your skills.");
        slashCommandData.addOption (OptionType.STRING, "mcq", "The ID of the MCQ Test you want to attempt.", true);
        return slashCommandData;
    }

    public static final SlashCommandData slashCommandData = slashCommandData ();

    public static void execute (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final OptionMapping mcqIDOptionMapping = slashCommandInteractionEvent.getOption ("mcq");
        final String mcqID = mcqIDOptionMapping.getAsString ();
        final Test.MCQ mcq = Test.MCQ.find (mcqID);
        if (mcq == null) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("I could not find a MCQ Test with ID:```" + mcqID + "```");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
            return;
        }
        if (mcq.questions.length == 0) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("The MCQ Test " + mcq.title + " has no questions.");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
            return;
        }
        final InteractionHook interactionHook = slashCommandInteractionEvent.getHook ();
        final WebhookMessageCreateAction <Message> loadingWebhookMessageCreateAction = interactionHook.sendMessage ("Loading...");
        loadingWebhookMessageCreateAction.queue (loadingMessage -> {
            {
                final long messageID = loadingMessage.getIdLong ();
                final User user = slashCommandInteractionEvent.getUser ();
                final long userID = user.getIdLong ();
                final short[] answers = new short[mcq.questions.length];
                new Test.MCQ.Attempt (userID, mcqID, answers, messageID);
            }
            final MessageCreateBuilder messageCreateBuilder = mcq.messageCreateBuilder (0);
            {
                final short[] answers = new short[mcq.questions.length];
                final User user = slashCommandInteractionEvent.getUser ();
                final StringSelectMenu questionsStringSelectMenu = questionSelect (mcq, user, 1, answers);
                final StringSelectMenu answerStringSelectMenu = answerSelect (loadingMessage.getIdLong (), user);
                messageCreateBuilder.addActionRow (questionsStringSelectMenu);
                messageCreateBuilder.addActionRow (answerStringSelectMenu);
            }
            final MessageCreateData messageCreateData = messageCreateBuilder.build ();
            final MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromCreateData (messageCreateData);
            final MessageEditData messageEditData = messageEditBuilder.build ();
            loadingMessage.editMessage (messageEditData).queue ();
        });
    }

    private static String customID (final boolean forAnswerSelect, final User user) {
        final JSONObject jsonObject = new JSONObject ();
        final long userID = user.getIdLong ();
        jsonObject.put ("user-id", userID);
        jsonObject.put ("command", "mcq-attempt");
        jsonObject.put ("for-answer-select", forAnswerSelect);
        final String customID = jsonObject.toString ();
        return customID;
    }

    private static StringSelectMenu questionSelect (final Test.MCQ mcq, final User user, final int question, final short[] answers) {
        final String customID = customID (false, user);
        final StringSelectMenu.Builder stringSelectMenuBuilder = StringSelectMenu.create (customID);
        for (short index = 0; index < answers.length; index += 1) {
            String questionOptionLabel = String.valueOf (index + 1);
            final short answer = answers[index];
            if (answer != 0) {
                questionOptionLabel += ") ";
                questionOptionLabel += (char) ('A' + answer - 1);
            }
            stringSelectMenuBuilder.addOption (questionOptionLabel, questionOptionLabel);
            if (index == question - 1) {
                stringSelectMenuBuilder.setDefaultValues (questionOptionLabel);
            }
        }
        stringSelectMenuBuilder.setRequiredRange (1, 1);
        return stringSelectMenuBuilder.build ();
    }

    private static StringSelectMenu answerSelect (final long messageID, final User user) {
        String customID = customID (true, user);
        final Test.MCQ.Attempt attempt = Test.MCQ.Attempt.find (messageID);
        final StringSelectMenu.Builder stringSelectMenuBuilder = StringSelectMenu.create (customID);
        final Test.MCQ.Question question = attempt.mcq.questions[attempt.questionIndex - 1];
        for (final String response : question.answers) {
            stringSelectMenuBuilder.addOption (response, response);
        }
        final short selectedAnswerIndex = attempt.answers[attempt.questionIndex - 1];
        if (selectedAnswerIndex != 0) {
            final String selectedAnswer = question.answers.get (selectedAnswerIndex - 1);
            stringSelectMenuBuilder.setDefaultValues (selectedAnswer);
        }
        stringSelectMenuBuilder.setRequiredRange (1, 1);
        return stringSelectMenuBuilder.build ();
    }

    private static Button submitButton (final Test.MCQ.Attempt attempt) {
        final JSONObject jsonObject = new JSONObject ();
        jsonObject.put ("command", "mcq-attempt");
        final String customID = jsonObject.toString ();
        final Button submitButton = Button.of(ButtonStyle.SUCCESS, customID, "Submit", MCQAddQuestion.whiteCheckMarkEmoji);
        return submitButton;
    }

    public static void stringSelectInteraction (final StringSelectInteractionEvent stringSelectInteractionEvent) {
        final User user = stringSelectInteractionEvent.getUser ();
        {
            final long userID = user.getIdLong ();
            final String customID = stringSelectInteractionEvent.getComponentId ();
            final JSONObject dataJSONObject = new JSONObject (customID);
            final long expectedUserID = dataJSONObject.getLong ("user-id");
            if (userID != expectedUserID) {
                final ReplyCallbackAction deferReplyCallbackAction = stringSelectInteractionEvent.deferReply (true);
                deferReplyCallbackAction.queue ();
                final InteractionHook interactionHook = stringSelectInteractionEvent.getHook ();
                final EmbedBuilder embedBuilder = new EmbedBuilder ();
                embedBuilder.setTitle ("Error");
                embedBuilder.setDescription ("You cannot interact with this MCQ attempt!");
                final MessageEmbed messageEmbed = embedBuilder.build ();
                final MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder ();
                messageCreateBuilder.addEmbeds (messageEmbed);
                final MessageCreateData messageCreateData = messageCreateBuilder.build ();
                final WebhookMessageCreateAction <Message> webhookMessageCreateAction = interactionHook.sendMessage (messageCreateData);
                webhookMessageCreateAction.queue ();
            }
        }
        final long messageID = stringSelectInteractionEvent.getMessageIdLong ();
        final Test.MCQ.Attempt attempt = Test.MCQ.Attempt.find (messageID);
        final String customID = stringSelectInteractionEvent.getComponentId ();
        final JSONObject jsonObject = new JSONObject (customID);
        final Test.MCQ.Question question = attempt.mcq.questions[attempt.questionIndex - 1];
        final boolean forAnswerSelect = jsonObject.getBoolean ("for-answer-select");
        final List <SelectOption> selectOptionList = stringSelectInteractionEvent.getSelectedOptions ();
        if (forAnswerSelect) {
            final int answersSize = question.answers.size ();
            final SelectOption selectedOption = selectOptionList.get (0);
            final String option = selectedOption.getLabel ();
            int answer;
            for (answer = 0; answer < answersSize; answer += 1) {
                final String possibleAnswer = question.answers.get (answer);
                final boolean answerMatches = possibleAnswer.equals (option);
                if (answerMatches) {
                    answer += 1;
                    break;
                }
            }
            final Message message = stringSelectInteractionEvent.getMessage ();
            final MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromMessage (message);
            final List <LayoutComponent> layoutComponentList = messageEditBuilder.getComponents ();
            final LayoutComponent answerStringSelect = layoutComponentList.get (1);
            final List <ActionComponent> actionComponentList = answerStringSelect.getActionComponents ();
            final ActionComponent actionComponent = actionComponentList.get (0);
            StringSelectMenu answerSelect = (StringSelectMenu) actionComponent;
            final StringSelectMenu.Builder answerSelectMenuBuilder = answerSelect.createCopy ();
            answerSelectMenuBuilder.setDefaultValues (answerSelectMenuBuilder.getOptions ().get (answer - 1).getValue ());
            messageEditBuilder.setComponents (message.getComponents ().get (0), ActionRow.of (answerSelectMenuBuilder.build ()), ActionRow.of(submitButton (attempt)));
            stringSelectInteractionEvent.editMessage (messageEditBuilder.build ()).queue ();
        } else {
            final SelectOption selectOption = selectOptionList.get (0);
            final String selectOptionValue = selectOption.getValue ();
            final int questionIndex = Integer.valueOf (selectOptionValue) - 1;
            final MessageCreateBuilder newQuestionMessageCreateBuilder = attempt.mcq.messageCreateBuilder (questionIndex);
            final StringSelectMenu answerStringSelect = answerSelect (messageID, user);
            final StringSelectMenu questionStringSelect = questionSelect (attempt.mcq, user, attempt.questionIndex + 1, attempt.answers);
            final Button submitButton = submitButton (attempt);
            newQuestionMessageCreateBuilder.addActionRow (questionStringSelect);
            newQuestionMessageCreateBuilder.addActionRow (answerStringSelect);
            newQuestionMessageCreateBuilder.addActionRow (submitButton);
            final MessageCreateData newQuestionMessageCreateData = newQuestionMessageCreateBuilder.build ();
            final MessageEditData newQuestionMessageEditData = MessageEditData.fromCreateData (newQuestionMessageCreateData);
            final MessageEditCallbackAction newQuestionMessageEditCallbackAction = stringSelectInteractionEvent.editMessage (newQuestionMessageEditData);
            newQuestionMessageEditCallbackAction.queue ();
        }
    }
}
