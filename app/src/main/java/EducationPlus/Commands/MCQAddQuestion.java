package EducationPlus.Commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
public class MCQAddQuestion {
    public static SlashCommandData slashCommandData () {
        final SlashCommandData slashCommandData = Commands.slash ("mcq-add-question", "Adds a question to your Multiple-Choice Question Test.");
        slashCommandData.addOption (OptionType.STRING, "mcq-id", "What is the ID of your MCQ Test?");
        slashCommandData.addOption (OptionType.STRING, "question", "What should your question ask?");
        slashCommandData.addOption (OptionType.ATTACHMENT, "image", "What image should your question display?");
        slashCommandData.addOption (OptionType.STRING, "correct", "What is the correct answer to your question?");
        for (short index = 1; index <= 3; index += 1) {
            slashCommandData.addOption (OptionType.STRING, "wrong-" + index, "What is a wrong answer to your question", index == 1);
        }
        return slashCommandData;
    }
}
