package EducationPlus.Commands.Lesson;

import EducationPlus.Classes.Lesson;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.lang.Math;
public class LessonCreate {
    public static final String[] subjects = {
        "Mathematics",
        "Foreign Language",
        "Social Studies",
        "Science",
        "Other"
    };
    private static final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static SlashCommandData slashCommandData () {
        final SlashCommandData slashCommandData = Commands.slash ("lesson-create", "Allows you to create a unique lesson.");
        slashCommandData.addOption (OptionType.STRING, "name", "What is the name of your lesson?", true);
        final OptionData subjectOptionData = new OptionData (OptionType.STRING, "subject", "What is the subject of your lesson?", true);
        for (final String subject : subjects) {
            subjectOptionData.addChoice (subject, subject);
        }
        slashCommandData.addOptions (subjectOptionData);
        slashCommandData.addOption (OptionType.STRING, "description", "What does your lesson teach?", true);
        return slashCommandData;
    }

    public static final SlashCommandData slashCommandData = slashCommandData ();

    public static void execute (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final OptionMapping lessonNameOptionMapping = slashCommandInteractionEvent.getOption ("name");
        final String lessonName = lessonNameOptionMapping.getAsString ();
        final OptionMapping lessonSubjectOptionMapping = slashCommandInteractionEvent.getOption ("subject");
        final String lessonSubject = lessonSubjectOptionMapping.getAsString ();
        String lessonID = generateID (slashCommandInteractionEvent);
        final OptionMapping lessonDescriptionOptionMapping = slashCommandInteractionEvent.getOption ("description");
        final String lessonDescription = lessonDescriptionOptionMapping.getAsString ();
        new Lesson (lessonID, lessonName, lessonSubject, lessonDescription);
        final EmbedBuilder embedBuilder = new EmbedBuilder ();
        embedBuilder.setColor (DiscordUtility.blurple);
        embedBuilder.setDescription ("Your Lesson has been created with ID:```" + lessonID + "```");
        final MessageEmbed messageEmbed = embedBuilder.build ();
        final InteractionHook interactionHook = slashCommandInteractionEvent.getHook ();
        MessageCreateBuilder messageBuilder = new MessageCreateBuilder ();
        messageBuilder.setEmbeds (messageEmbed);
        final MessageCreateData message = messageBuilder.build ();
        WebhookMessageCreateAction<Message> messageCreateAction = interactionHook.sendMessage (message);
        messageCreateAction.queue ();
    }

    public static String generateID (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final String randomString = randomString (5);
        final User user = slashCommandInteractionEvent.getUser ();
        String lessonID = user.getId ();
        lessonID = randomString + lessonID;
        return lessonID;
    }

    private static char randomChar () {
        final int expandedRandomDoubleSize = characters.length ();
        double randomDouble = Math.random () * expandedRandomDoubleSize;
        final int expandedRandomIndex = (int) (randomDouble);
        final char randomCharacter = characters.charAt (expandedRandomIndex);
        return randomCharacter;
    }

    private static String randomString (final int length) {
        String randomString = "";
        for (int loopIndex = 0; loopIndex < length; loopIndex += 1) {
            randomString += randomChar ();
        }
        return randomString;
    }
}
