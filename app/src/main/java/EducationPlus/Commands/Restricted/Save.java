package EducationPlus.Commands.Restricted;

import EducationPlus.Classes.Helpers.Display;
import EducationPlus.Classes.Helpers.Test;
import EducationPlus.Classes.Lesson;
import EducationPlus.Listeners.Ready;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class Save {
    public static final SlashCommandData slashCommandData = slashCommandData ();

    public static SlashCommandData slashCommandData () {
        return Commands.slash ("save", "[Restricted] Saves all current data to JSON.");
    }

    public static void execute (SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final User user = slashCommandInteractionEvent.getUser ();
        final boolean owner = Ready.owner (user);
        if (!owner) {
            final Display errorDisplay = new Display ("Data Saving Error", "This command is only accessible by authorized users.");
            final Display[] displays = {
                errorDisplay
            };
            DiscordUtility.deletable (slashCommandInteractionEvent, displays, false);
            return;
        }
        save ();
        final Display successDisplay = new Display ("Data Save Success", "All data regarding Discord server settings was successfully saved!");
        final Display[] displays = {
            successDisplay
        };
        DiscordUtility.deletable (slashCommandInteractionEvent, displays, false);
    }

    public static void save () {
        Lesson.save ();
        Test.save ();
    }
}
