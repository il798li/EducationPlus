package EducationPlus.Listeners;

import EducationPlus.Commands.MCQ.MCQAttempt;
import EducationPlus.Main;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.json.JSONObject;
public class StringSelectInteraction extends ListenerAdapter {
    public void onStringSelectInteraction (final StringSelectInteractionEvent stringSelectInteractionEvent) {
        final String customID = stringSelectInteractionEvent.getComponentId ();
        final JSONObject jsonObject = new JSONObject (customID);
        final String command = jsonObject.getString ("command");
        switch (command) {
            case "mcq-attempt": {
                MCQAttempt.stringSelectInteraction (stringSelectInteractionEvent);
                Main.debug ("mcq attempt something selected");
                break;
            }
            default: {
                final Message message = stringSelectInteractionEvent.getMessage ();
                final MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromMessage (message);
                final MessageEditData messageEditData = messageEditBuilder.build ();
                final MessageEditAction messageEditAction = message.editMessage (messageEditData);
                messageEditAction.queue ();
            }
        }
    }
}
