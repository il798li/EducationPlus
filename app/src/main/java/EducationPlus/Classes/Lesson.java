package EducationPlus.Classes;

import EducationPlus.Listeners.Ready;
import EducationPlus.Utility.DiscordUtility;
import EducationPlus.Utility.JSONUtility;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
public class Lesson {

    public enum Subject {
        Mathematics,
        Foreign_Language,
        Social_Studies,
        Science,
        Other
    }

    public static final ArrayList <Lesson> lessons = new ArrayList <Lesson> ();

    public static Lesson findLesson (final String id) {
        for (final Lesson lesson : lessons) {
            final boolean lessonMatches = lesson.id.equals (id);
            if (lessonMatches) {
                return lesson;
            }
        }
        return null;
    }

    public final String name;
    public final String description;
    public final String subject;
    public Page[] pages;
    public final String id;

    public static void load (final JDA jda) {
        final JSONObject lessonsJSONObject = JSONUtility.load (JSONUtility.JSONFile.Lessons);
        final JSONArray lessonsJSONList = lessonsJSONObject.getJSONArray ("lessons");
        final int lessonsJSONListLength = lessonsJSONList.length ();
        for (int index = 0; index < lessonsJSONListLength; index += 1) {
            final JSONObject lessonJSONObject = lessonsJSONList.getJSONObject (index);
            new Lesson (lessonJSONObject, jda);
        }
    }

    public JSONObject toJSON () {
        final JSONObject lessonJSONObject = new JSONObject ();
        lessonJSONObject.put ("id", this.id);
        lessonJSONObject.put ("name", this.name);
        lessonJSONObject.put ("subject", this.subject);
        lessonJSONObject.put ("description", this.description);
        final JSONArray lessonPagesJSONArray = new JSONArray ();
        for (final Page lessonPage : this.pages) {
            lessonPagesJSONArray.put (lessonPage.messageID);
        }
        lessonJSONObject.put ("pages", lessonPagesJSONArray);
        return lessonJSONObject;
    }

    public static void save () {
        final JSONObject lessonsJSONObject = new JSONObject ();
        final JSONArray lessonsJSONArray = new JSONArray ();
        for (final Lesson lesson : lessons) {
            final JSONObject lessonJSONObject = lesson.toJSON ();
            lessonsJSONArray.put (lessonJSONObject);
        }
        lessonsJSONObject.put ("lessons", lessonsJSONArray);
        JSONUtility.save (lessonsJSONObject, JSONUtility.JSONFile.Lessons);
    }

    public Lesson (final JSONObject lessonJSONObject, final JDA jda) {
        this.id = lessonJSONObject.getString ("id");
        this.name = lessonJSONObject.getString ("name");
        this.subject = lessonJSONObject.getString ("subject");
        this.description = lessonJSONObject.getString ("description");
        final JSONArray lessonPagesJSONArray = lessonJSONObject.getJSONArray ("pages");
        final int lessonPagesJSONArrayLength = lessonPagesJSONArray.length ();
        this.pages = new Page[lessonPagesJSONArrayLength];
        for (int lessonPagesJSONArrayIndex = 0; lessonPagesJSONArrayIndex < lessonPagesJSONArrayLength; lessonPagesJSONArrayIndex += 1) {
            final long lessonPageMessageID = lessonPagesJSONArray.getLong (lessonPagesJSONArrayIndex);
            final Page lessonPage = new Page (lessonPageMessageID, jda);
            this.pages[lessonPagesJSONArrayIndex] = lessonPage;
        }
        lessons.add (this);
    }

    public EmbedBuilder embedBuilder (int pageIndex, final JDA jda) {
        final Page page = this.pages[pageIndex];
        final EmbedBuilder embedBuilder = page.embedBuilder ();
        embedBuilder.setTitle (this.name);
        final String userID = this.id.substring (5);
        final User user = jda.getUserById (userID);
        final String userName = user.getName ();
        final String userAvatarURL = user.getAvatarUrl ();
        embedBuilder.setAuthor (userName, null, userAvatarURL);
        pageIndex += 1;
        embedBuilder.setFooter ("Page " + pageIndex + "/" + this.pages.length);
        return embedBuilder;
    }

    public Lesson (final String id, final String name, final String subject, final String description) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.description = description;
        this.pages = new Page[0];
        lessons.add (this);
    }

    public static class Page {
        private String content;
        private Message.Attachment image;
        private long messageID;

        public Page (final String content, Message.Attachment image) {
            this.content = content;
            this.image = image;
        }

        public Page (final long messageID, final JDA jda) {
            final TextChannel pagesTextChannel = jda.getTextChannelById (Ready.lessonPagesTextChannelID);
            RestAction <Message> pageMessageRetrievalRestAction = pagesTextChannel.retrieveMessageById (messageID);
            pageMessageRetrievalRestAction.queue (message -> {
                this.content = message.getContentRaw ();
                final List <Message.Attachment> messageAttachments = message.getAttachments ();
                final int messageAttachmentsLength = messageAttachments.size ();
                if (messageAttachmentsLength >= 1) {
                    this.image = messageAttachments.get (0);
                } else {
                    this.image = null;
                }
                this.messageID = message.getIdLong ();
            });
        }

        public EmbedBuilder embedBuilder () {
            final EmbedBuilder embedBuilder = new EmbedBuilder ();
            embedBuilder.setDescription (this.content);
            if (this.image != null) {
                final String imageURL = this.image.getUrl ();
                embedBuilder.setImage (imageURL);
            }
            embedBuilder.setColor (DiscordUtility.blurple);
            return embedBuilder;
        }
    }
}
