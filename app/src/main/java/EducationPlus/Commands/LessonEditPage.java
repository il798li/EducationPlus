package EducationPlus.Commands;

import EducationPlus.Classes.Lesson;
import EducationPlus.Listeners.Ready;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.AttachmentProxy;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
public class LessonEditPage {
    public static SlashCommandData slashCommandData () {
        final SlashCommandData slashCommandData = Commands.slash ("lesson-edit-page", "Allows you to edit a page of your existing lesson.");
        slashCommandData.addOption (OptionType.STRING, "lesson-id", "What lesson's are you trying to edit?", true);
        slashCommandData.addOption (OptionType.INTEGER, "page-index", "What Page of your Lesson do you want to edit?", true);
        slashCommandData.addOption (OptionType.STRING, "content", "What should the content of your page say?", true);
        slashCommandData.addOption (OptionType.ATTACHMENT, "image", "Should your Page have an image? If you leave this blank, any existing images will be deleted.");
        return slashCommandData;
    }

    public static final SlashCommandData slashCommandData = slashCommandData ();

    public static void execute (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        final OptionMapping lessonIDOptionMapping = slashCommandInteractionEvent.getOption ("lesson-id");
        final String lessonID = lessonIDOptionMapping.getAsString ();
        final Lesson lesson = Lesson.findLesson (lessonID);
        if (lesson == null) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("I could not find a Lesson with ID:```" + lessonID + "```");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
        }
        final User user = slashCommandInteractionEvent.getUser ();
        final String userID = user.getId ();
        final boolean userMatches = lesson.id.contains (userID);
        if (userMatches == false) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("You are not the owner of Lesson " + lesson.name + " with ID:```" + lesson.id + "```");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
            return;
        }
        final OptionMapping pageIndexOptionMapping = slashCommandInteractionEvent.getOption ("page-index");
        final int pageIndex = pageIndexOptionMapping.getAsInt ();
        if (pageIndex > lesson.pages.length || pageIndex < 1) {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setTitle ("Error");
            embedBuilder.setDescription ("Your Page index, `" + pageIndex + "`, is not valid.");
            DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
            return;
        }
        Message.Attachment attachment;
        final MessageCreateBuilder pageMessageCreateBuilder = new MessageCreateBuilder ();
        final OptionMapping pageImageOptionMapping = slashCommandInteractionEvent.getOption ("image");
        final EmbedBuilder embedBuilder = new EmbedBuilder ();
        if (pageImageOptionMapping != null) {
            attachment = pageImageOptionMapping.getAsAttachment ();
            final boolean isImage = attachment.isImage ();
            if (isImage == false) {
                final EmbedBuilder errorEmbedBuilder = new EmbedBuilder ();
                errorEmbedBuilder.setTitle ("Error");
                errorEmbedBuilder.setDescription ("Your attachment must be an image.");
                DiscordUtility.respond (slashCommandInteractionEvent, errorEmbedBuilder);
                return;
            }
            final String attachmentName = attachment.getFileName ();
            final AttachmentProxy attachmentProxy = attachment.getProxy ();
            final CompletableFuture<InputStream> inputStreamCompletableFuture = attachmentProxy.download ();
            final InputStream inputStream = inputStreamCompletableFuture.join ();
            final FileUpload fileUpload = FileUpload.fromStreamSupplier (attachmentName, () -> inputStream);
            pageMessageCreateBuilder.addFiles (fileUpload);
            final String attachmentURL = attachmentProxy.getUrl ();
            embedBuilder.setImage (attachmentURL);
        }
        final OptionMapping pageContentOptionMapping = slashCommandInteractionEvent.getOption ("content");
        final String pageContent = pageContentOptionMapping.getAsString ();
        pageMessageCreateBuilder.setContent (pageContent);
        final JDA jda = slashCommandInteractionEvent.getJDA ();
        final TextChannel lessonPagesTextChannel = jda.getTextChannelById (Ready.lessonPagesTextChannelID);
        final MessageCreateData messageCreateData = pageMessageCreateBuilder.build ();
        final MessageCreateAction messageCreateAction = lessonPagesTextChannel.sendMessage (messageCreateData);
        messageCreateAction.queue (message -> {
            final long messageID = message.getIdLong ();
            final Lesson.Page page = new Lesson.Page (messageID, jda);
            lesson.pages[pageIndex - 1] = page;
        });
        embedBuilder.setTitle ("Success");
        embedBuilder.setDescription ("Your page was successfully added to Lesson " + lesson.name + " with ID:```" + lesson.id + "```");
        embedBuilder.addField ("Content", pageContent, false);
        DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
    }
}
