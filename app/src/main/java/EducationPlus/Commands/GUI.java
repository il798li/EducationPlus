package EducationPlus.Commands;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.json.JSONObject;

public class GUI {
    public static final SlashCommandData slashCommandData = Commands.slash("gui", "Gives you a GUI to use Education+ rather than Slash Commands.");

    public static void execute (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final JSONObject jsonObject = new JSONObject ();
        jsonObject.put ("command", "gui");
        jsonObject.put ("function", "search");
        final String lessonSearchButtonID = jsonObject.toString ();
        Button lessonSearchButton = Button.of (ButtonStyle.PRIMARY, lessonSearchButtonID, "🔎");
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder ();
        messageCreateBuilder.addActionRow (lessonSearchButton);
        final MessageCreateData messageCreateData = messageCreateBuilder.build ();
        final InteractionHook interactionHook = slashCommandInteractionEvent.getHook ();
        WebhookMessageCreateAction<Message> messageWebhookMessageCreateAction = interactionHook.sendMessage (messageCreateData);
        messageWebhookMessageCreateAction.queue ();
    }

    public static void respond (final ButtonInteractionEvent buttonInteractionEvent) {
        final String componentID = buttonInteractionEvent.getComponentId ();
        final JSONObject jsonObject = new JSONObject (componentID);
        final String function = jsonObject.getString ("function");
        switch (function) {
            case "search": {
                break;
            }
        }
    }

    public static void modal (final ModalInteractionEvent modalInteractionEvent) {

    }

    private static Button[] guiButtons () {
        Button searchButton = null;
        final Button[] buttons = {searchButton};
        {
            final JSONObject jsonObject = new JSONObject ();
            jsonObject.put("command", "gui");
            jsonObject.put("function", "search");
            final String searchButtonID = jsonObject.toString ();
            searchButton = Button.of (ButtonStyle.PRIMARY, searchButtonID, "🔎");
        }
        return buttons;
    }
}
