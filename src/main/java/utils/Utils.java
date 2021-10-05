package utils;

import main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Objects;

public class Utils {
    /**
     * This logger is used exclusively for errors thrown by utility methods defined in this class.
     */
    public static final Logger LOG = JDALogger.getLog(Utils.class);

    /**
     * Get a {@link Class#getResource(String) resource} with the given file name. Note that this is based on the {@link
     * Main} class, not the calling class, so the resource will be retrieved from the <code>main</code> module.
     * <p>
     * Including a "<code>/</code>" before the name of a resource file is proper practice, but file names without a
     * slash will be automatically modified to comply.
     *
     * @param fileName the name of the desired resource
     *
     * @return the resource {@link URL}, if a resource with the given name is found. Otherwise <code>null</code>.
     * @see #getResourceFile(String) getResourceFile()
     * @see #getResourceStream(String) getResourceStream()
     */
    @Nullable
    public static URL getResource(String fileName) {
        return Main.class.getResource(fileName.startsWith("/") ? fileName : "/" + fileName);
    }

    /**
     * Load a resource file with the given name as an {@link InputStream}. The "<code>/</code>" before the name is
     * optional, and will be set automatically if not given. Note that the resource is retrieved using the {@link Main}
     * class, not the calling class.
     *
     * @param fileName the name of the resource file
     *
     * @return an {@link InputStream} with the resource contents
     * @see #getResource(String)
     * @see #getResourceFile(String)
     */
    @Nonnull
    public static InputStream getResourceStream(@Nonnull String fileName) {
        try {
            return Objects.requireNonNull(
                    Main.class.getResourceAsStream(fileName.startsWith("/") ? fileName : "/" + fileName)
            );
        } catch (NullPointerException e) {
            LOG.error("Failed to create an input stream from the resource '" + fileName + "'.");
            throw e;
        }
    }

    /**
     * Load a resource {@link File} with the given name. If the resource file is not found, a new {@link File} instance
     * with an empty path name is returned instead, and an {@link Logger#error(String) error} is {@link #LOG logged} to
     * the console.
     * <p>
     * Note that this resource file is based off the {@link Main} class, rather than the calling class.
     * <p>The "<code>/</code>" before the name is optional, and will be set automatically if not given.
     *
     * @param fileName the name of the file to retrieve
     *
     * @return the retrieved file, or a file with no path if a file with the given name was not found
     */
    @Nonnull
    public static File getResourceFile(String fileName) {
        try {
            return new File(Objects.requireNonNull(getResource(fileName)).toURI());
        } catch (NullPointerException e) {
            LOG.error("Failed to find a resource file called '" + fileName + "'.");
        } catch (URISyntaxException e) {
            LOG.error("Failed to obtain a URI and create a File from the resource '" + fileName + "'.");
        }
        return new File("");
    }

    /**
     * This is a convenience method for creating {@link MessageEmbed.Field} instances, which are used to create {@link
     * EmbedBuilder} instances.
     *
     * @param title  the field title
     * @param value  the contents of the field
     * @param inline whether the field should display inline
     *
     * @return the newly created {@link MessageEmbed.Field}
     */
    @Nonnull
    public static MessageEmbed.Field makeField(@Nonnull String title, @Nonnull String value, boolean inline) {
        return new MessageEmbed.Field(title, value, inline);
    }

    /**
     * This is an overloaded method for {@link #makeField(String, String, boolean)} that sets inline to false by
     * default.
     *
     * @param title the field title
     * @param value the contents of the field
     *
     * @return the newly created {@link MessageEmbed.Field}
     */
    @Nonnull
    public static MessageEmbed.Field makeField(@Nonnull String title, @Nonnull String value) {
        return makeField(title, value, false);
    }

    /**
     * This is an overloaded method for {@link #makeEmbed(String, String, Color, MessageEmbed.Field...)} that creates an
     * {@link EmbedBuilder} with an additional footer parameter. Additional parameters can be added to the returned
     * embed.
     *
     * @param title       the title
     * @param description the description
     * @param color       the color
     * @param footer      the footer
     * @param fields      one or more fields (optionally created with {@link #makeField(String, String, boolean)}
     *
     * @return the {@link EmbedBuilder}
     */
    @Nonnull
    public static EmbedBuilder makeEmbed(
            @Nonnull String title,
            @Nonnull String description,
            @Nonnull Color color,
            @Nonnull String footer,
            MessageEmbed.Field... fields) {
        return makeEmbed(title, description, color, fields).setFooter(footer);
    }

    /**
     * This is an overloaded method for {@link #makeEmbed(String, String, Color)} that creates an {@link EmbedBuilder}
     * with one or more embed fields. Additional parameters can be set to the returned embed.
     *
     * @param title       the title
     * @param description the description
     * @param color       the color
     * @param fields      one or more fields (optionally created with {@link #makeField(String, String, boolean)}
     *
     * @return the {@link EmbedBuilder}
     */
    @Nonnull
    public static EmbedBuilder makeEmbed(@Nullable String title, @Nonnull String description, @Nonnull Color color,
                                         MessageEmbed.Field... fields) {
        EmbedBuilder e = makeEmbed(title, description, color);
        for (MessageEmbed.Field field : fields)
            e.addField(field);
        return e;
    }

    /**
     * This is an overloaded method for {@link #makeEmbed(String, String, Color)} that creates an {@link EmbedBuilder}
     * with a footer. Additional parameters can be set to the returned embed.
     *
     * @param title       the title
     * @param description the description
     * @param color       the color
     * @param footer      the footer
     *
     * @return the {@link EmbedBuilder}
     */
    @Nonnull
    public static EmbedBuilder makeEmbed(@Nonnull String title, @Nonnull String description, @Nonnull Color color,
                                         @Nonnull String footer) {
        return makeEmbed(title, description, color).setFooter(footer);
    }

    /**
     * This creates an {@link EmbedBuilder} with the most basic set of parameters: a title, description, and color.
     * Additional parameters can be added to the returned embed.
     *
     * @param title       the title
     * @param description the description
     * @param color       the color
     *
     * @return the {@link EmbedBuilder}
     */
    @Nonnull
    public static EmbedBuilder makeEmbed(@Nullable String title, @Nonnull String description, @Nonnull Color color) {
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(color);
    }

    /**
     * Create a fancy {@link EmbedBuilder} titled "Error" with the provided message as the description.
     *
     * @param message the error message
     *
     * @return an error embed
     * @see #error(Throwable)
     */
    public static EmbedBuilder error(@Nonnull String message) {
        return makeEmbed("Error", message, Colors.RED).setTimestamp(Instant.now());
    }

    /**
     * Create a fancy {@link EmbedBuilder} titled "Error" with the exception class name and message as the description.
     *
     * @param exception the exception that was thrown
     *
     * @return an error embed
     * @see #error(String)
     */
    public static EmbedBuilder error(@Nonnull Throwable exception) {
        return error(String.format("**%s**: %s", exception.getClass().getName(), exception.getMessage()));
    }

    /**
     * Send an {@link #error(String) error} to the given {@link MessageChannel channel}.
     *
     * @param channel the channel
     * @param message the error message
     *
     * @see #error(String)
     * @see #error(MessageChannel, Throwable)
     * @see #error(Message, String)
     */
    public static void error(@Nonnull MessageChannel channel, @Nonnull String message) {
        channel.sendMessageEmbeds(error(message).build()).queue();
    }

    /**
     * Send an {@link #error(String) error} as a reply to a given {@link Message}.
     *
     * @param message the channel
     * @param error   the error message
     *
     * @see #error(String)
     * @see #error(Message, Throwable)
     * @see #error(MessageChannel, String)
     */
    public static void error(@Nonnull Message message, @Nonnull String error) {
        message.replyEmbeds(error(error).build()).queue();
    }

    /**
     * Send an {@link #error(String) error} as an {@link ReplyAction#setEphemeral(boolean) ephemeral} reply to a given
     * {@link GenericInteractionCreateEvent interaction}.
     *
     * @param interaction the interaction
     * @param message     the error message
     *
     * @see #error(String)
     * @see #error(GenericInteractionCreateEvent, Throwable)
     * @see #error(GenericInteractionCreateEvent, String, boolean)
     * @see #error(InteractionHook, String)
     */
    public static void error(@Nonnull GenericInteractionCreateEvent interaction, @Nonnull String message) {
        interaction.replyEmbeds(error(message).build()).setEphemeral(true).queue();
    }

    /**
     * Send an {@link #error(String) error} as reply to a given {@link GenericInteractionCreateEvent interaction}.
     * Allows control over whether the message is {@link ReplyAction#setEphemeral(boolean) ephemeral}.
     *
     * @param interaction the interaction
     * @param message     the error message
     * @param ephemeral   whether the message should be ephemeral; passed directly to {@link
     *                    ReplyAction#setEphemeral(boolean) setEphemeral(boolean)}.
     *
     * @see #error(String)
     * @see #error(GenericInteractionCreateEvent, Throwable, boolean)
     * @see #error(GenericInteractionCreateEvent, String)
     * @see #error(GenericInteractionCreateEvent, String, boolean)
     */
    public static void error(@Nonnull GenericInteractionCreateEvent interaction, @Nonnull String message,
                             boolean ephemeral) {
        interaction.replyEmbeds(error(message).build()).setEphemeral(ephemeral).queue();
    }

    /**
     * Send an {@link #error(String) error} by editing a given {@link InteractionHook interaction} response that was
     * initially {@link GenericInteractionCreateEvent#deferReply() defered}.
     *
     * @param interaction the interaction
     * @param message     the error message
     *
     * @see #error(String)
     * @see #error(InteractionHook, Throwable)
     * @see #error(GenericInteractionCreateEvent, String)
     * @see #error(GenericInteractionCreateEvent, String, boolean)
     */
    public static void error(@Nonnull InteractionHook interaction, @Nonnull String message) {
        interaction.editOriginalEmbeds(error(message).build()).queue();
    }

    /**
     * Send an {@link #error(Throwable) error} to the given {@link MessageChannel channel}.
     *
     * @param channel   the channel
     * @param exception the exception that was thrown
     *
     * @see #error(Throwable)
     * @see #error(MessageChannel, String)
     * @see #error(Message, Throwable)
     */
    public static void error(@Nonnull MessageChannel channel, @Nonnull Throwable exception) {
        channel.sendMessageEmbeds(error(exception).build()).queue();
    }

    /**
     * Send an {@link #error(Throwable) error} as a reply to a given {@link Message}.
     *
     * @param message   the channel
     * @param exception the exception that was thrown
     *
     * @see #error(Throwable)
     * @see #error(Message, String)
     * @see #error(MessageChannel, Throwable)
     */
    public static void error(@Nonnull Message message, @Nonnull Throwable exception) {
        message.replyEmbeds(error(exception).build()).queue();
    }

    /**
     * Send an {@link #error(Throwable) error} as an {@link ReplyAction#setEphemeral(boolean) ephemeral} reply to a
     * given {@link GenericInteractionCreateEvent interaction}.
     *
     * @param interaction the interaction
     * @param exception   the exception that was thrown
     *
     * @see #error(Throwable)
     * @see #error(GenericInteractionCreateEvent, String)
     * @see #error(GenericInteractionCreateEvent, Throwable, boolean)
     * @see #error(MessageChannel, Throwable)
     */
    public static void error(@Nonnull GenericInteractionCreateEvent interaction, @Nonnull Throwable exception) {
        interaction.replyEmbeds(error(exception).build()).setEphemeral(true).queue();
    }

    /**
     * Send an {@link #error(Throwable) error} as reply to a given {@link GenericInteractionCreateEvent interaction}.
     * Allows control over whether the message is {@link ReplyAction#setEphemeral(boolean) ephemeral}.
     *
     * @param interaction the interaction
     * @param exception   the exception that was thrown
     * @param ephemeral   whether the message should be ephemeral; passed directly to {@link
     *                    ReplyAction#setEphemeral(boolean) setEphemeral(boolean)}.
     *
     * @see #error(Throwable)
     * @see #error(GenericInteractionCreateEvent, String, boolean)
     * @see #error(GenericInteractionCreateEvent, Throwable)
     * @see #error(InteractionHook, Throwable)
     */
    public static void error(@Nonnull GenericInteractionCreateEvent interaction, @Nonnull Throwable exception,
                             boolean ephemeral) {
        interaction.replyEmbeds(error(exception).build()).setEphemeral(ephemeral).queue();
    }

    /**
     * Send an {@link #error(Throwable) error} by editing a given {@link InteractionHook interaction} response that was
     * initially {@link GenericInteractionCreateEvent#deferReply() defered}.
     *
     * @param interaction the interaction
     * @param exception   the exception that was thrown
     *
     * @see #error(Throwable)
     * @see #error(InteractionHook, String)
     * @see #error(GenericInteractionCreateEvent, Throwable)
     * @see #error(GenericInteractionCreateEvent, Throwable, boolean)
     */
    public static void error(@Nonnull InteractionHook interaction, @Nonnull Throwable exception) {
        interaction.editOriginalEmbeds(error(exception).build()).queue();
    }

    /**
     * Get a {@link Guild} with the matching snowflake id.
     * <p>
     * If no connected guild is found, <code>null</code> is returned, and an error message is {@link #LOG logged} to the
     * console.
     *
     * @param id the snowflake id of the desired guild.
     *
     * @return the guild (or <code>null</code> if not found)
     */
    public static Guild getGuild(long id) {
        Guild guild = Main.JDA.getGuildById(id);
        if (guild == null)
            LOG.error("Failed to retrieve a guild with the id " + id + ".");
        return guild;
    }

    /**
     * Get a string for mentioning {@link User users} based on their Discord {@link ISnowflake#getIdLong() id}.
     *
     * @param id the id of the user to mention
     *
     * @return a string that mentions the user
     */
    @Nonnull
    public static String mentionUser(long id) {
        return String.format("<@%d>", id);
    }

    /**
     * Get a string for mentioning channels based on their Discord {@link ISnowflake#getIdLong() id}.
     *
     * @param id the id of the channel to mention
     *
     * @return a string that mentions that channel
     */
    @Nonnull
    public static String mentionChannel(long id) {
        return String.format("<#%d>", id);
    }

    /**
     * This formats and returns a custom hyperlink for {@link EmbedBuilder embed} messages. The link <code>text</code>
     * and <code>url</code> are formatted as follows:
     * <p>
     * <code>[link text](url)</code>
     * <p>
     * This means that the link text must <i>not</i> contain brackets.
     *
     * @param text the link text
     * @param url  the destination url when the link is clicked
     *
     * @return the properly formatted link
     */
    @Nonnull
    public static String link(@NotNull String text, @NotNull String url) {
        return String.format("[%s](%s)", text, url);
    }

    /**
     * Get an {@link EmbedBuilder} that contains a nicely formatted profile display for user.
     *
     * @param user the user to display
     *
     * @return an info panel for the user
     */
    public static EmbedBuilder getUserPanel(User user) {
        EmbedBuilder embed = Utils.makeEmbed(
                null,
                "",
                Colors.NOT_QUITE_BLACK,
                Utils.makeField("Tag",
                        String.format("Name: %s%nDiscriminator: %s",
                                user.getName(), user.getDiscriminator())),
                Utils.makeField("Account",
                        String.format("Created: %s%nID: `%d`",
                                TimeFormat.DATE_TIME_LONG.format(user.getTimeCreated()), user.getIdLong()))
        );

        EnumSet<User.UserFlag> flags = user.getFlags();
        if (flags.size() > 0) {
            StringBuilder list = new StringBuilder();
            flags.forEach(flag -> list.append(System.lineSeparator()).append(flag.getName()));
            embed.addField(Utils.makeField("Profile Flags", list.substring(1)));
        }

        if (user.isBot())
            embed.addField(Utils.makeField("Account Type", "Bot"));

        embed.setTimestamp(Instant.now());
        embed.setThumbnail(user.getEffectiveAvatarUrl());
        embed.setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl());

        return embed;
    }
}
