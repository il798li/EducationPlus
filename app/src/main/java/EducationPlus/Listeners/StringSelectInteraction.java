package EducationPlus.Listeners;

import EducationPlus.Commands.MCQ.MCQAttempt;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;
public class StringSelectInteraction extends ListenerAdapter {
    public void onStringSelectInteraction (final StringSelectInteractionEvent stringSelectInteractionEvent) {
        final String customID = stringSelectInteractionEvent.getComponentId ();
        final JSONObject jsonObject = new JSONObject (customID);
        final String command = jsonObject.getString ("command");
        switch (command) {
            case "mcq-attempt": {
                MCQAttempt.stringSelectInteraction (stringSelectInteractionEvent);
                break;
            }
        }
    }
}
