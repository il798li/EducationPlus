package EducationPlus.Commands;

import EducationPlus.Classes.Helpers.Test;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
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
    }
}
