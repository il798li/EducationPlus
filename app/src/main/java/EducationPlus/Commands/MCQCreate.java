package EducationPlus.Commands;

import EducationPlus.Classes.Helpers.Test;
import EducationPlus.Utility.DiscordUtility;
import EducationPlus.Utility.JSONUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.json.JSONObject;

public class MCQCreate {
    public static SlashCommandData slashCommandData () {
        final SlashCommandData slashCommandData = Commands.slash ("mcq-create", "Creates a test with multiple-choice questions that other learners can take.");
        slashCommandData.addOption (OptionType.STRING, "name", "What should your test be named?", true);
        slashCommandData.addOption (OptionType.STRING, "description", "What are you testing learners on?", true);
        {
            OptionData optionData = new OptionData (OptionType.STRING, "subject", "What subject are you testing learners on?", true);
            for (final String subject : LessonCreate.subjects) {
                optionData.addChoice (subject, subject);
            }
            slashCommandData.addOptions (optionData);
        }
        return slashCommandData;
    }

    public static final SlashCommandData slashCommandData = slashCommandData ();

    public static void execute (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final String mcqID = LessonCreate.generateID (slashCommandInteractionEvent);
        final OptionMapping nameOptionMapping = slashCommandInteractionEvent.getOption ("name");
        final String mcqName = nameOptionMapping.getAsString ();
        final OptionMapping descriptionOptionMapping = slashCommandInteractionEvent.getOption ("description");
        final String mcqDescription = descriptionOptionMapping.getAsString ();
        final OptionMapping subjectOptionMapping = slashCommandInteractionEvent.getOption ("subject");
        final String mcqSubject = subjectOptionMapping.getAsString ();
        final Test.MCQ.Question[] questions = new Test.MCQ.Question[0];
        final Test.MCQ mcq = new Test.MCQ (mcqName, mcqDescription, questions, mcqSubject, mcqID);
        final EmbedBuilder embedBuilder = new EmbedBuilder ();
        {
            embedBuilder.setDescription ("Your Multiple-Choice Question Test has been created with ID:```" + mcqID + "```");
        }
        DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
    }
}
