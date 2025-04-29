package EducationPlus.Listeners;

import EducationPlus.Commands.Lesson.LessonSearch;
import EducationPlus.Main;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
public class CommandAutoCompleteInteraction extends ListenerAdapter {
    public void onCommandAutoCompleteInteraction (final CommandAutoCompleteInteractionEvent commandAutoCompleteInteractionEvent) {
        final String commandName = commandAutoCompleteInteractionEvent.getName ();
        Main.debug (commandName);
        switch (commandName) {
            case "lesson-search": {
                LessonSearch.autoComplete (commandAutoCompleteInteractionEvent);
                break;
            }
        }
    }
}
