package EducationPlus.Commands.Lesson;

import EducationPlus.Classes.Helpers.Display;
import EducationPlus.Classes.Lesson;
import EducationPlus.Listeners.Ready;
import EducationPlus.Utility.DiscordUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.AttachmentProxy;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
public class LessonAddPage {
    private static SlashCommandData slashCommandData () {
        final SlashCommandData lessonAddPageSlashCommandData = Commands.slash ("lesson-add-page", "Adds a page to an existing lesson.");
        lessonAddPageSlashCommandData.addOption (OptionType.STRING, "lesson-id", "What is your lesson's ID?", true);
        lessonAddPageSlashCommandData.addOption (OptionType.STRING, "content", "What text content should this page display?", true);
        lessonAddPageSlashCommandData.addOption (OptionType.ATTACHMENT, "image", "Do you want to add an image to your lesson?", false);
        lessonAddPageSlashCommandData.addOption (OptionType.INTEGER, "page-index", "Do you want to insert this page somewhere in the middle of your lesson?", false);
        return lessonAddPageSlashCommandData;
    }

    public static final SlashCommandData slashCommandData = slashCommandData ();

    public static void execute (final SlashCommandInteractionEvent slashCommandInteractionEvent) {
        Lesson lesson = null;
        final OptionMapping lessonIDOptionMapping = slashCommandInteractionEvent.getOption ("lesson-id");
        final String lessonID = lessonIDOptionMapping.getAsString ();
        for (Lesson potentialLesson : Lesson.lessons) {
            final boolean matchingID = potentialLesson.id.equals (lessonID);
            if (matchingID) {
                lesson = potentialLesson;
                break;
            }
        }
        if (lesson == null) {
            final Display display = new Display ("Error", "I could not find a Lesson with ID:```" + lessonID + "```");
            Display[] displays = new Display[1];
            displays[0] = display;
            DiscordUtility.deletable (slashCommandInteractionEvent, displays, false);
            return;
        }
        final User user = slashCommandInteractionEvent.getUser ();
        final String userID = user.getId ();
        final boolean validUser = lessonID.endsWith (userID);
        if (validUser == false) {
            final Display display = new Display ("Error", "You are not the owner of Lesson " + lesson.name + " with ID:```" + lessonID + "```");
            DiscordUtility.deletable (slashCommandInteractionEvent, display);
            return;
        }
        final OptionMapping lessonPageContentOptionMapping = slashCommandInteractionEvent.getOption ("content");
        final String lessonPageContent = lessonPageContentOptionMapping.getAsString ();
        final OptionMapping lessonPageImageOptionMapping = slashCommandInteractionEvent.getOption ("image");
        Message.Attachment lessonPageImageAttachment = null;
        if (lessonPageImageOptionMapping != null) {
            lessonPageImageAttachment = lessonPageImageOptionMapping.getAsAttachment ();
            final boolean lessonPageAttachmentIsImage = lessonPageImageAttachment.isImage ();
            if (lessonPageAttachmentIsImage == false) {
                final Display display = new Display ("Error", "Your attachment must be an image.");
                DiscordUtility.deletable (slashCommandInteractionEvent, display);
                return;
            }
        }
        final MessageCreateBuilder lessonPageMessage = new MessageCreateBuilder ();
        lessonPageMessage.setContent (lessonPageContent);
        if (lessonPageImageAttachment != null) {
            final AttachmentProxy lessonPageImageAttachmentProxy = lessonPageImageAttachment.getProxy ();
            final String lessonPageImageExtension = lessonPageImageAttachment.getFileExtension ();
            final FileUpload lessonPageImageFileUpload = FileUpload.fromStreamSupplier (lessonPageImageAttachment.getFileName (), () -> lessonPageImageAttachmentProxy.download ().join ());
            lessonPageMessage.addFiles (lessonPageImageFileUpload);
        }
        final JDA jda = slashCommandInteractionEvent.getJDA ();
        final TextChannel lessonPagesTextChannel = jda.getTextChannelById (Ready.lessonPagesTextChannelID);
        final MessageCreateData messageCreateData = lessonPageMessage.build ();
        final MessageCreateAction messageCreateAction = lessonPagesTextChannel.sendMessage (messageCreateData);
        final Lesson finalLesson = lesson;
        messageCreateAction.queue (message -> {
            long lessonPageMessageID = message.getIdLong ();
            int lessonNewPageIndex = finalLesson.pages.length + 1;
            final Lesson.Page newLessonPage = new Lesson.Page (lessonPageMessageID, jda);
            final OptionMapping lessonPageIndexOptionMapping = slashCommandInteractionEvent.getOption ("page-index");
            if (lessonPageIndexOptionMapping != null) {
                lessonNewPageIndex = lessonPageIndexOptionMapping.getAsInt ();
            }
            if (lessonNewPageIndex < 1 || lessonNewPageIndex > finalLesson.pages.length + 1) {
                final EmbedBuilder embedBuilder = new EmbedBuilder ();
                embedBuilder.setTitle ("Error");
                embedBuilder.setDescription ("Your Page index, `" + lessonNewPageIndex + "` is not valid.");
                DiscordUtility.respond (slashCommandInteractionEvent, embedBuilder);
                return;
            }
            int newPageCount = finalLesson.pages.length + 1;
            final Lesson.Page[] newLessonPages = new Lesson.Page[newPageCount];
            {
                for (int pageIndex = 0; pageIndex < lessonNewPageIndex - 1; pageIndex += 1) {
                    newLessonPages[pageIndex] = finalLesson.pages[pageIndex];
                }
                newLessonPages[lessonNewPageIndex - 1] = newLessonPage;
                for (int pageIndex = lessonNewPageIndex - 1; pageIndex < finalLesson.pages.length; pageIndex += 1) {
                    newLessonPages[pageIndex + 1] = finalLesson.pages[pageIndex];
                }
            }
            finalLesson.pages = newLessonPages;
        });
        final EmbedBuilder successEmbedBuilder = new EmbedBuilder ();
        successEmbedBuilder.setColor (DiscordUtility.blurple);
        successEmbedBuilder.setTitle ("Success");
        successEmbedBuilder.setDescription ("Your page was successfully added to Lesson " + lesson.name + " with ID:```" + lesson.id + "```");
        successEmbedBuilder.addField ("Content", lessonPageContent, false);
        if (lessonPageImageOptionMapping != null) {
            successEmbedBuilder.setImage (lessonPageImageAttachment.getUrl ());
        }
        final InteractionHook slashCommandInteractionHook = slashCommandInteractionEvent.getHook ();
        final MessageEmbed successMessageEmbed = successEmbedBuilder.build ();
        final MessageCreateBuilder successMessageCreateBuilder = new MessageCreateBuilder ();
        successMessageCreateBuilder.addEmbeds (successMessageEmbed);
        final MessageCreateData successMessageCreateData = successMessageCreateBuilder.build ();
        WebhookMessageCreateAction <Message> successMessageCreateAction = slashCommandInteractionHook.sendMessage (successMessageCreateData);
        successMessageCreateAction.queue ();
    }
}
