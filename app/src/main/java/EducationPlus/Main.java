package EducationPlus;

import EducationPlus.Data.Setting;
import EducationPlus.Listeners.*;
import EducationPlus.Utility.FileUtility;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;

public class Main {
    public static JDA jda;
    public static void main (final String[] args) {
        for (int loop = 0; loop < 100; loop++) {
            debug ();
        }
        debug ("Education+ is loading...");
        String token = "UNKNOWN_TOKEN";
        try {
            token = token ();
            final EnumSet <GatewayIntent> intents = GatewayIntent.getIntents (GatewayIntent.ALL_INTENTS);
            final JDABuilder jdaBuilder = JDABuilder.create (token, intents);
            jda = jdaBuilder.build ();
        } catch (final InvalidTokenException invalidTokenException) {
            debug ("Could not login using this token: \"" + token + "\"");
            return;
        } catch (final Exception exception) {
            debug ("An unknown error occurred.");
            exception.printStackTrace ();
        }
        debug ("Finished logging in!");
        {
            Setting.load ();
        }
        {
            {
                final Ready ready = new Ready ();
                jda.addEventListener (ready);
            }
            {
                final SlashCommandInteraction slashCommandInteraction = new SlashCommandInteraction ();
                jda.addEventListener (slashCommandInteraction);
            }
            {
                final ButtonInteraction buttonInteractionEvent = new ButtonInteraction ();
                jda.addEventListener (buttonInteractionEvent);
            }
            {
                final CommandAutoCompleteInteraction commandAutoCompleteInteraction = new CommandAutoCompleteInteraction ();
                jda.addEventListener (commandAutoCompleteInteraction);
            }
            {
                final ModalInteraction modalInteraction = new ModalInteraction ();
                jda.addEventListener (modalInteraction);
            }
        }
    }

    public static void debug () {
        System.out.println ("\n");
    }

    public static void debug (final String debug) {
        final String startingString = "\t[Debug] ";
        final StringBuilder formattedDebug = new StringBuilder ("\u001B[38;2;88;101;242m");
        formattedDebug.append (startingString);
        final int length = debug.length ();
        for (int index = 0; index < length; index++) {
            String character = debug.charAt (index) + "";
            final boolean newLine = character.equals ("\n");
            if (newLine) {
                character += startingString;
            }
            formattedDebug.append (character);
        }
        formattedDebug.append ("\u001B[0m");
        System.out.println (formattedDebug);
    }

    public static String token () {
        final String token = FileUtility.readFile ("Token.txt");
        return token.trim ();
    }

    public static void debug (final int debug) {
        debug ("" + debug);
    }

    public static void debug (final long debug) {
        debug ("" + debug);
    }

    public static void debug (final boolean debug) {
        debug ("" + debug);
    }
}
