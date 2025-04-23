package EducationPlus.Commands;

import EducationPlus.Classes.Lesson;
import EducationPlus.Utility.BasicUtility;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.AutoCompleteCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
public class LessonSearch {
    public static SlashCommandData slashCommandData () {
        final SlashCommandData slashCommandData = Commands.slash ("lesson-search", "Allows you to search for a Lesson.");
        slashCommandData.addOption (OptionType.STRING, "query", "What type of lesson are you searching for?", true);
        {
            final OptionData subjectOptionData = new OptionData (OptionType.STRING, "subject", "What subject are you trying to learn?", true);
            for (final String subject : LessonCreate.subjects) {
                subjectOptionData.addChoice (subject, subject);
            }
            slashCommandData.addOptions (subjectOptionData);
        }
        slashCommandData.addOption (OptionType.STRING, "author", "Do you want to filter Lessons by a specific author? Input their username here.", false, true);
        return slashCommandData;
    }

    public static final SlashCommandData slashCommandData = slashCommandData ();

    public static final void execute (@NonNull final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final JDA jda = slashCommandInteractionEvent.getJDA ();
        final OptionMapping subjectOptionMapping = slashCommandInteractionEvent.getOption ("subject");
        final String subject = subjectOptionMapping.getAsString ();
        final OptionMapping authorOptionMapping = slashCommandInteractionEvent.getOption ("author");
        User authorUser;
        String authorUsername;
        String authorID = null;
        ArrayList <Lesson> lessons = filter (subject, authorID);
        int size = lessons.size ();
        if (authorOptionMapping != null) {
            authorUsername = authorOptionMapping.getAsString ();
            authorUser = DiscordUtility.getUser (jda, authorUsername);
            if (authorUser == null) {
                final EmbedBuilder embedBuilder = new EmbedBuilder ();
                embedBuilder.setDescription ("I could not find a user with name:```" + authorUsername + "```");
                embedBuilder.setTitle ("Error");
                DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
                return;
            }
            authorID = authorUser.getId ();
            lessons = filter (subject, authorID);
            size = lessons.size ();
            if (size == 0) {
                final EmbedBuilder embedBuilder = new EmbedBuilder ();
                embedBuilder.setTitle ("Error");
                embedBuilder.setDescription (authorUsername + " has not created any Lessons.");
                DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
                return;
            }
        }
        final OptionMapping queryOptionMapping = slashCommandInteractionEvent.getOption ("query");
        final String query = queryOptionMapping.getAsString ();
        lessons = sort (query, lessons);
        final EmbedBuilder embedBuilder = lessonSearch (query, lessons, 0);
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder ();
        final MessageEmbed messageEmbed = embedBuilder.build ();
        messageCreateBuilder.addEmbeds (messageEmbed);
        final Button[] buttons = buttons (query, subject, authorID, 0, size);
        messageCreateBuilder.addActionRow (buttons);
        final MessageCreateData messageCreateData = messageCreateBuilder.build ();
        final InteractionHook interactionHook = slashCommandInteractionEvent.getHook ();
        final WebhookMessageCreateAction <Message> messageWebhookMessageCreateAction = interactionHook.sendMessage (messageCreateData);
        messageWebhookMessageCreateAction.queue ();
    }

    public static final void autoComplete (@NonNull final CommandAutoCompleteInteractionEvent commandAutoCompleteInteractionEvent) {
        final AutoCompleteQuery autoCompleteQuery = commandAutoCompleteInteractionEvent.getFocusedOption ();
        String value = autoCompleteQuery.getValue ();
        value = value.toLowerCase ();
        final JDA jda = commandAutoCompleteInteractionEvent.getJDA ();
        final List <User> users = jda.getUsers ();
        final List <String> usernames = new ArrayList <String> ();
        final int usersSize = users.size ();
        for (int userIndex = 0; userIndex < usersSize; userIndex += 1) {
            final User user = users.get (userIndex);
            final String username = user.getName ();
            final boolean validUser = username.contains (value);
            if (validUser) {
                usernames.add (username);
            }
        }
        final AutoCompleteCallbackAction autoCompleteCallbackAction = commandAutoCompleteInteractionEvent.replyChoiceStrings (usernames);
        autoCompleteCallbackAction.queue ();
    }

    public static ArrayList <Lesson> sort (@NonNull final String query, @Nullable ArrayList <Lesson> lessons) {
        if (lessons == null) {
            lessons = Lesson.lessons;
        }
        final int size = lessons.size ();
        for (int index = 1; index < size; index += 1) {
            final Lesson thisLesson = lessons.get (index);
            final Lesson previousLesson = lessons.get (index - 1);
            final int thisLessonSimilarity = BasicUtility.similarity (thisLesson.name, query);
            final int previousLessonSimilarity = BasicUtility.similarity (previousLesson.name, query);
            if (thisLessonSimilarity > previousLessonSimilarity) {
                lessons.set (index - 1, thisLesson);
                lessons.set (index, previousLesson);
                index = 0;
            }
        }
        return lessons;
    }

    public static ArrayList <Lesson> filter (@NonNull final String subject, @Nullable final String authorID) {
        final ArrayList <Lesson> filteredLessons = new ArrayList <> (Lesson.lessons);
        int size = filteredLessons.size ();
        for (int index = 0; index < size; index += 1) {
            final Lesson lesson = filteredLessons.get (index);
            final boolean matchingSubject = lesson.subject.equals (subject);
            boolean matchingAuthor = true;
            if (authorID != null) {
                final String lessonAuthorID = lesson.id.substring (5);
                matchingAuthor = lessonAuthorID.equals (authorID);
            }
            if (matchingSubject == false || matchingAuthor == false) {
                filteredLessons.remove (index);
                index -= 1;
            }
        }
        return filteredLessons;
    }

    public static EmbedBuilder lessonSearch (@NonNull final String query, ArrayList <Lesson> lessons, @NonNull int page) {
        final EmbedBuilder embedBuilder = new EmbedBuilder ();
        embedBuilder.setTitle ("Query: " + query);
        final int initialIndex = page * 5;
        final int finalIndex = initialIndex + 5;
        final int size = lessons.size ();
        for (int index = initialIndex; index < finalIndex && index < size; index += 1) {
            final Lesson lesson = lessons.get (index);
            final String description = lesson.description + "```" + lesson.id + "```";
            embedBuilder.addField (lesson.name, description, false);
        }
        final int pagesInteger = pages (size);
        page += 1;
        embedBuilder.setFooter ("Page " + page + "/" + pagesInteger);
        embedBuilder.setColor (DiscordUtility.blurple);
        return embedBuilder;
    }

    private static int pages (final int size) {
        final double pagesDouble = size / 5.0;
        final double pages = Math.ceil (pagesDouble);
        return (int) pages;
    }

    public static Button[] buttons (@Nonnull final String query, @NonNull final String subject, @Nullable final String authorID, @Nonnull final int page, @NonNull final int size) {
        final JSONObject jsonObject = new JSONObject ();
        jsonObject.put ("query", query);
        jsonObject.put ("subject", subject);
        jsonObject.put ("author-id", authorID);
        jsonObject.put ("command", "lesson-search");
        jsonObject.put ("page", 0);
        String buttonID = jsonObject.toString ();
        Button previousButton = Button.of (ButtonStyle.PRIMARY, buttonID, "◀️");
        previousButton.withDisabled (true);
        jsonObject.put ("page", 1);
        buttonID = jsonObject.toString ();
        Button nextButton = Button.of (ButtonStyle.PRIMARY, buttonID, "▶️");
        final int pages = pages (size);
        if (page <= 0) {
            previousButton = previousButton.withDisabled (true);
        }
        if (page + 1 >= pages) {
            nextButton = nextButton.withDisabled (true);
        }
        final Button[] buttons = {
            previousButton,
            nextButton
        };
        return buttons;
    }

    public static void respond (final ButtonInteractionEvent buttonInteractionEvent) {
        final String componentID = buttonInteractionEvent.getComponentId ();
        final JSONObject jsonObject = new JSONObject (componentID);
        final String query = jsonObject.getString ("query");
        final String subject = jsonObject.getString ("subject");
        final int page = jsonObject.getInt ("page");
        String authorID;
        try {
            authorID = jsonObject.getString ("author-id");
        } catch (final JSONException jsonException) {
            authorID = null;
        }
        final ArrayList<Lesson> filteredLessons = filter (subject, authorID);
        final EmbedBuilder embedBuilder = lessonSearch (query, filteredLessons, page);
        MessageEditBuilder messageEditBuilder = new MessageEditBuilder ();
        final MessageEmbed messageEmbed = embedBuilder.build ();
        messageEditBuilder.setEmbeds (messageEmbed);
        final int size = filteredLessons.size ();
        final Button[] buttons = buttons(query, subject, authorID, page, size);
        messageEditBuilder.setActionRow (buttons);
        final MessageEditData messageEditData = messageEditBuilder.build ();
        final MessageEditCallbackAction messageEditCallbackAction = buttonInteractionEvent.editMessage (messageEditData);
        messageEditCallbackAction.queue ();
    }
}
