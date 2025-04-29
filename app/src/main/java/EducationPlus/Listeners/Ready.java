package EducationPlus.Listeners;

import EducationPlus.Classes.Lesson;
import EducationPlus.Commands.*;
import EducationPlus.Commands.Administrator.Settings;
import EducationPlus.Commands.Basic.Ping;
import EducationPlus.Commands.Basic.Statistics;
import EducationPlus.Commands.Lesson.*;
import EducationPlus.Commands.MCQ.MCQCreate;
import EducationPlus.Commands.Restricted.Save;
import EducationPlus.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class Ready extends ListenerAdapter {

    public static final long lessonPagesTextChannelID = 1320909068738498685L;

    public static boolean owner (User user) {
        final long[] ownerIDs = {
            655263219459293210L,
            1214057610857160835L
        };
        final long userID = user.getIdLong ();
        for (final long ownerID : ownerIDs) {
            if (userID == ownerID) {
                return true;
            }
        }
        return false;
    }

    public void onReady (ReadyEvent readyEvent) {
        final JDA jda = readyEvent.getJDA ();
        final User mm = jda.getSelfUser ();
        final String name = mm.getName ();
        Main.debug ("Successfully signed in as " + name + "!");
        final Guild epGuild = jda.getGuildById (1320909067035873392L);
        {
            final CommandListUpdateAction commandListUpdateAction = epGuild.updateCommands ();
            commandListUpdateAction.addCommands (Save.slashCommandData);
            commandListUpdateAction.addCommands (LessonCreate.slashCommandData);
            commandListUpdateAction.addCommands (LessonAddPage.slashCommandData);
            commandListUpdateAction.addCommands (LessonLearn.slashCommandData);
            commandListUpdateAction.addCommands (LessonEditPage.slashCommandData);
            commandListUpdateAction.addCommands (LessonSearch.slashCommandData);
            commandListUpdateAction.addCommands (GUI.slashCommandData);
            commandListUpdateAction.addCommands (MCQCreate.slashCommandData);
            commandListUpdateAction.queue ();
        }
        {
            final CommandListUpdateAction publishedCommandListUpdateAction = jda.updateCommands ();
            publishedCommandListUpdateAction.addCommands (Ping.slashCommandData);
            publishedCommandListUpdateAction.addCommands (Settings.slashCommandData);
            publishedCommandListUpdateAction.addCommands (Statistics.slashCommandData);
            publishedCommandListUpdateAction.queue ();
        }
        final long ping = Ping.ping (jda);
        presence (jda);
        Lesson.load (jda);
        Main.debug (name + " is responding to commands with " + ping + " milliseconds of latency...");
    }

    public static void presence (final JDA jda) {
        final Presence jdaPresence = jda.getPresence ();
        final Activity activity = Activity.of (Activity.ActivityType.STREAMING, "Join Education+ to receive the best education for you, tailored by teachers from all over the world!");
        jdaPresence.setPresence (OnlineStatus.ONLINE, activity);
    }
}
