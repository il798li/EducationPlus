package EducationPlus.Listeners;

import EducationPlus.Commands.*;
import EducationPlus.Commands.Administrator.Settings;
import EducationPlus.Commands.Basic.Ping;
import EducationPlus.Commands.Basic.Statistics;
import EducationPlus.Commands.Lesson.*;
import EducationPlus.Commands.Restricted.Save;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class SlashCommandInteraction extends ListenerAdapter {

    public void onSlashCommandInteraction (SlashCommandInteractionEvent slashCommandInteractionEvent) {
        ReplyCallbackAction replyCallbackAction = slashCommandInteractionEvent.deferReply ();
        replyCallbackAction.queue ();
        final String name = slashCommandInteractionEvent.getName ();
        switch (name) {
            case "ping": {
                Ping.execute (slashCommandInteractionEvent);
                break;
            }
            case "statistics": {
                Statistics.execute (slashCommandInteractionEvent);
                break;
            }
            case "settings": {
                Settings.execute (slashCommandInteractionEvent);
                break;
            }
            case "save": {
                Save.execute (slashCommandInteractionEvent);
                break;
            }
            case "lesson-create": {
                LessonCreate.execute (slashCommandInteractionEvent);
                break;
            }
            case "lesson-add-page": {
                LessonAddPage.execute (slashCommandInteractionEvent);
                break;
            }
            case "lesson-learn": {
                LessonLearn.execute (slashCommandInteractionEvent);
                break;
            }
            case "lesson-edit-page": {
                LessonEditPage.execute (slashCommandInteractionEvent);
                break;
            }
            case "lesson-search": {
                LessonSearch.execute (slashCommandInteractionEvent);
                break;
            }
            case "gui": {
                GUI.execute (slashCommandInteractionEvent);
                break;
            }
        }
    }
}
