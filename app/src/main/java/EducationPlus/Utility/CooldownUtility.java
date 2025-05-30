package EducationPlus.Utility;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;

public class CooldownUtility {
    public static final ArrayList <CooldownUtility> cooldowns = new ArrayList <CooldownUtility> ();
    public final long userID;
    public final String command;
    public long nextTimestamp;

    public CooldownUtility (long userID, String command, long nextTimestamp) {
        this.userID = userID;
        this.command = command;
        this.nextTimestamp = nextTimestamp;
        cooldowns.add (this);
    }

    public static boolean cooldown (final SlashCommandInteractionEvent slashCommandInteractionEvent, final int cooldown) {
        final User user = slashCommandInteractionEvent.getUser ();
        final long userID = user.getIdLong ();
        final String command = slashCommandInteractionEvent.getName ();
        CooldownUtility cooldownUtility = find (slashCommandInteractionEvent);
        long currentTimestamp = DiscordUtility.timestamp ();
        if (currentTimestamp < cooldownUtility.nextTimestamp) {
            return false;
        }
        cooldownUtility.nextTimestamp += cooldown;
        return true;
    }

    public static CooldownUtility find (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final User user = slashCommandInteractionEvent.getUser ();
        final long userID = user.getIdLong ();
        final String command = slashCommandInteractionEvent.getName ();
        final int size = cooldowns.size ();
        for (final CooldownUtility cooldownUtility : cooldowns) {
            final boolean userIDMatch = cooldownUtility.userID == userID;
            final boolean commandMatch = cooldownUtility.command.equals (command);
            if (userIDMatch && commandMatch) {
                return cooldownUtility;
            }
        }
        final long timestamp = DiscordUtility.timestamp ();
        return new CooldownUtility (userID, command, timestamp);
    }
}
