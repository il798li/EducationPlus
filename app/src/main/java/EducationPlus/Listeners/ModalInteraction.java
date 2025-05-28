package EducationPlus.Listeners;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
public class ModalInteraction extends ListenerAdapter {
    public void onModalInteraction (final ModalInteractionEvent modalInteractionEvent) {
        modalInteractionEvent.reply("test modal response").queue ();
    }

}
