package EducationPlus.Listeners;

import EducationPlus.Commands.GUI;
import EducationPlus.Commands.LessonLearn;
import EducationPlus.Commands.LessonSearch;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;

public class ButtonInteraction extends ListenerAdapter {

    public void onButtonInteraction (final ButtonInteractionEvent buttonInteractionEvent) {
        final String customID = buttonInteractionEvent.getComponentId ();
        final JSONObject identifierJSONObject = new JSONObject (customID);
        final String command = identifierJSONObject.getString ("command");
        switch (command) {
            case "lesson-learn": {
                LessonLearn.respond (buttonInteractionEvent);
                break;
            }
            case "lesson-search": {
                LessonSearch.respond (buttonInteractionEvent);
                break;
            }
            case "gui": {
                GUI.respond (buttonInteractionEvent);
                break;
            }
        }
    }
}
