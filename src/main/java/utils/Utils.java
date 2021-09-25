package utils;

import main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

public class Utils {
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
    public static MessageEmbed.Field makeEmbedField(String title, String value, boolean inline) {
        return new MessageEmbed.Field(title, value, inline);
    }

    /**
     * This is an overloaded method for {@link #makeEmbedField(String, String, boolean)} that sets inline to false by
     * default.
     *
     * @param title the field title
     * @param value the contents of the field
     *
     * @return the newly created {@link MessageEmbed.Field}
     */
    public static MessageEmbed.Field makeEmbedField(String title, String value) {
        return makeEmbedField(title, value, false);
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
     * @param fields      one or more fields (optionally created with {@link #makeEmbedField(String, String, boolean)}
     *
     * @return the {@link EmbedBuilder}
     */
    public static EmbedBuilder makeEmbed(
            String title,
            String description,
            Color color,
            String footer,
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
     * @param fields      one or more fields (optionally created with {@link #makeEmbedField(String, String, boolean)}
     *
     * @return the {@link EmbedBuilder}
     */
    public static EmbedBuilder makeEmbed(String title, String description, Color color, MessageEmbed.Field... fields) {
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
    public static EmbedBuilder makeEmbed(String title, String description, Color color, String footer) {
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
    public static EmbedBuilder makeEmbed(String title, String description, Color color) {
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(color);
    }

    /**
     * Load a resource file based on its name. The "<code>/</code>" before the name is optional, and will be set
     * automatically if not given. Note that the resource is retrieved using the {@link Main} class.
     *
     * @param fileName the name of the resource file
     *
     * @return an {@link InputStream} with the resource contents
     */
    public static @Nonnull
    InputStream loadResource(@Nonnull String fileName) {
        try {
            return Objects.requireNonNull(
                    Main.class.getResourceAsStream(fileName.startsWith("/") ? fileName : "/" + fileName)
            );
        } catch (NullPointerException e) {
            Main.LOG.error("Failed to create an input stream from the resource '" + fileName + "'.");
            throw e;
        }
    }
}
