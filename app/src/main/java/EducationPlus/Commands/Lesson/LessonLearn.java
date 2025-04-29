package EducationPlus.Commands.Lesson;

import EducationPlus.Classes.Lesson;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.json.JSONObject;

public class LessonLearn {
    public static SlashCommandData slashCommandData () {
        final SlashCommandData slashCommandData = Commands.slash ("lesson-learn", "Allows you to learn any lesson.");
        slashCommandData.addOption (OptionType.STRING, "lesson-id", "What is the ID of the lesson you want to learn?", true);
        return slashCommandData;
    }

    public static final SlashCommandData slashCommandData = slashCommandData ();

    public static void execute (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final OptionMapping lessonIDOptionMapping = slashCommandInteractionEvent.getOption ("lesson-id");
        final String lessonID = lessonIDOptionMapping.getAsString ();
        final Lesson lesson = Lesson.findLesson (lessonID);
        if (lesson == null) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("I could not find a Lesson with ID:```" + lessonID + "```");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
            return;
        }
        if (lesson.pages.length == 0) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("There are no pages in the Lesson " + lesson.name + " with ID:```" + lesson.id + "```");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
            return;
        }
        final JSONObject jsonObject = new JSONObject ();
        jsonObject.put ("page", 0);
        jsonObject.put ("id", lesson.id);
        jsonObject.put ("command", "lesson-learn");
        final String previousButtonID = jsonObject.toString ();
        Button previousButton = Button.of (ButtonStyle.PRIMARY, previousButtonID, "◀️");
        previousButton = previousButton.withDisabled (true);
        jsonObject.put ("page", 1);
        final User user = slashCommandInteractionEvent.getUser ();
        final long userID = user.getIdLong ();
        jsonObject.put ("user-id", userID);
        final String nextButtonID = jsonObject.toString ();
        Button nextButton = Button.of (ButtonStyle.PRIMARY, nextButtonID, "▶️");
        nextButton = nextButton.withDisabled (lesson.pages.length <= 1);
        final ActionRow actionRow = ActionRow.of (previousButton, nextButton);
        final JDA jda = slashCommandInteractionEvent.getJDA ();
        final EmbedBuilder embedBuilder = lesson.embedBuilder (0, jda);
        final MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder ();
        final MessageEmbed embed = embedBuilder.build ();
        messageCreateBuilder.addEmbeds (embed);
        messageCreateBuilder.addActionRow (previousButton, nextButton);
        final InteractionHook interactionHook = slashCommandInteractionEvent.getHook ();
        final MessageCreateData messageCreateData = messageCreateBuilder.build ();
        final WebhookMessageCreateAction <Message> webhookMessageCreateAction = interactionHook.sendMessage (messageCreateData);
        webhookMessageCreateAction.queue ();
    }

    public static void respond (ButtonInteractionEvent buttonInteractionEvent) {
        final JDA jda = buttonInteractionEvent.getJDA ();
        final String componentId = buttonInteractionEvent.getComponentId ();
        final JSONObject jsonObject = new JSONObject (componentId);
        final int newPageIndex = jsonObject.getInt ("page");
        final String lessonID = jsonObject.getString ("id");
        final Lesson lesson = Lesson.findLesson (lessonID);
        final User user = buttonInteractionEvent.getUser ();
        final long userID = user.getIdLong();
        final long approvedUserID = jsonObject.getLong ("user-id");
        if (userID != approvedUserID) {
            final ReplyCallbackAction replyCallbackAction = buttonInteractionEvent.deferReply (true);
            replyCallbackAction.queue ();
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("You cannot interact with this, because it was requested by <@" + approvedUserID + ">.");
            embedBuilder.setColor (DiscordUtility.blurple);
            final MessageEmbed messageEmbed = embedBuilder.build ();
            final MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder ();
            messageCreateBuilder.addEmbeds (messageEmbed);
            final MessageCreateData messageCreateData = messageCreateBuilder.build ();
            final InteractionHook interactionHook = buttonInteractionEvent.getHook ();
            final WebhookMessageCreateAction<Message> messageWebhookMessageCreateAction = interactionHook.sendMessage (messageCreateData);
            messageWebhookMessageCreateAction.queue ();

        }
        final EmbedBuilder embedBuilder = lesson.embedBuilder (newPageIndex, jda);
        final MessageEmbed embed = embedBuilder.build ();
        final Message message = buttonInteractionEvent.getMessage ();
        buttonInteractionEvent.getUser ();
        jsonObject.put ("page", newPageIndex + 1);
        final String nextButtonID = jsonObject.toString ();
        Button nextButton = Button.of (ButtonStyle.PRIMARY, nextButtonID, "▶️");
        nextButton = nextButton.withDisabled (newPageIndex + 1 >= lesson.pages.length);
        jsonObject.put ("page", newPageIndex - 1);
        final String previousButtonID = jsonObject.toString ();
        Button previousButton = Button.of (ButtonStyle.PRIMARY, previousButtonID, "◀️");
        previousButton = previousButton.withDisabled (newPageIndex <= 0);
        final MessageEditBuilder messageEditBuilder = new MessageEditBuilder ();
        messageEditBuilder.setEmbeds (embed);
        messageEditBuilder.setActionRow (previousButton, nextButton);
        final MessageEditData messageEditData = messageEditBuilder.build ();
        final MessageEditCallbackAction messageEditCallbackAction = buttonInteractionEvent.editMessage (messageEditData);
        messageEditCallbackAction.queue ();
    }
}
