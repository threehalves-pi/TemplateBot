package utils;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

/**
 * This class contains {@link Color} constants used throughout the bot, such as in a {@link MessageEmbed}. By default,
 * it includes the colors from Discord's official color palette.
 * <p><b><a href="https://discord.com/branding">Current Branding</a></b>
 * <p><b><a href="https://web.archive.org/web/20210409112419/discord.com/branding">Old Branding</a></b>
 * <p>
 * <i>Note that some constants here, namely {@link #RED} and {@link #WHITE}, are used by the TemplateBot core library.
 * They should not be removed. See {@link Utils#error(String)}.</i>
 */
public class Colors {
    // The official Discord palette according to https://discord.com/branding
    public static final Color BLURPLE = new Color(0x5865F2);
    public static final Color GREEN = new Color(0x57F287);
    public static final Color YELLOW = new Color(0xFEE75C);
    public static final Color FUSCHIA = new Color(0xEB459E);
    public static final Color RED = new Color(0xED4245);
    public static final Color WHITE = new Color(0xFFFFFF);
    public static final Color BLACK = new Color(0x000000);

    // The old Discord color palette, retrieved from https://web.archive.org/web/20210409112419/discord.com/branding
    public static final Color BLURPLE_OLD = new Color(0x7289DA);
    public static final Color GREYPLE = new Color(0x99AAB5);
    public static final Color DARK_NOT_BLACK = new Color(0x2C2F33);
    public static final Color NOT_QUITE_BLACK = new Color(0x23272A);
}
