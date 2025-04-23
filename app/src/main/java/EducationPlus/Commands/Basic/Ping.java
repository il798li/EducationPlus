package EducationPlus.Commands.Basic;

import EducationPlus.Classes.Helpers.Display;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Ping {
    public static final SlashCommandData slashCommandData = Commands.slash ("ping", "Check my response time in milliseconds.");

    public static void execute (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final JDA jda = slashCommandInteractionEvent.getJDA ();
        final long pingMilli = ping (jda);
        final Display[] displays = {
            new Display ("Latency", "I am currently responding to commands within approximately " + pingMilli + " milliseconds.")
        };
        DiscordUtility.deletable (slashCommandInteractionEvent, displays, false);
    }

    public static long ping (JDA jda) {
        return jda.getGatewayPing ();
    }
}
