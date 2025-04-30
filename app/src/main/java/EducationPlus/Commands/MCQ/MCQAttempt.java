package EducationPlus.Commands.MCQ;

import EducationPlus.Classes.Helpers.Test;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
public class MCQAttempt {
    public static SlashCommandData slashCommandData () {
        final SlashCommandData slashCommandData = Commands.slash ("mcq-attempt", "Attempts an MCQ Test to see your skills.");
        slashCommandData.addOption (OptionType.STRING, "mcq-id", "The ID of the MCQ Test you want to attempt.", true);
        return slashCommandData;
    }

    final SlashCommandData slashCommandData = slashCommandData ();

    public static void execute (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final OptionMapping mcqIDOptionMapping = slashCommandInteractionEvent.getOption ("mcq-id");
        final String mcqID = mcqIDOptionMapping.getAsString ();
        final Test.MCQ mcq = Test.MCQ.find (mcqID);
        if (mcq == null) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("I could not find a MCQ Test with ID:```" + mcqID + "```");
            DiscordUtility.respond(slashCommandInteractionEvent, embedBuilder);
            return;
        }
        if (mcq.questions.length <= 0) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("The MCQ Test " + mcq.title + " has no questions.");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
            return;
        }
        final Test.MCQ.Question question = mcq.questions[0];
        final MessageCreateBuilder messageCreateBuilder = mcq.messageCreateBuilder (0);
        final MessageCreateData messageCreateData = messageCreateBuilder.build ();
        final InteractionHook interactionHook = slashCommandInteractionEvent.getHook ();
        final WebhookMessageCreateAction<Message> messageWebhookMessageCreateAction = interactionHook.sendMessage (messageCreateData);
        messageWebhookMessageCreateAction.queue ();
    }
}
