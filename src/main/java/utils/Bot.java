package utils;

import events.OnStartup;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.awt.Color;

/**
 * This class is designed to work with the <code>bot.properties</code> resource file associated with this Discord bot.
 * All the properties contained in that file have a matching instance variable here that is set with
 * <code>loadProperties()</code> in {@link OnStartup}.
 * <p>
 * To create a new property, add a line to <code>bot.properties</code> in the following format:
 * <p>
 * <code>resource_name=value</code>
 * <p>
 * Then create a field in {@link Bot} with the <i>same name</i> as the property. Ensure that the {@link Bot} field is
 * all uppercase; the property name is case-insensitive in <code>bot.properties</code>. Make sure that the {@link Bot}
 * field is <code>public</code> and <code>static</code>, but <i>not</i> <code>final</code>.
 * <p>
 * The property will automatically be loaded from <code>bot.properties</code> to the {@link Bot} field by the private
 * <code>loadProperties()</code> method in {@link OnStartup}. If you use a non-standard field type, you will need to
 * modify the <code>cast()</code> method in {@link OnStartup} to support that type. By default, it supports:
 * <ul>
 *     <li>{@link String}</li>
 *     <li>{@link Character}</li>
 *     <li>{@link Byte}</li>
 *     <li>{@link Boolean}</li>
 *     <li>{@link Integer}</li>
 *     <li>{@link Long}</li>
 *     <li>{@link Float}</li>
 *     <li>{@link Short}</li>
 *     <li>{@link OnlineStatus}</li>
 *     <li>{@link Color}</li>
 *     <li>And all respective primitive types.</li>
 * </ul>
 */
public class Bot {
    // Generic bot config
    public static String PREFIX;
    public static boolean ENABLE_STARTUP_MESSAGE;
    public static String NAME;
    public static String BOT_DESCRIPTION;
    public static String VERSION;

    // Slash commands
    public static boolean LOAD_GLOBAL_COMMANDS;
    public static boolean LOAD_LOCAL_COMMANDS;

    // Status
    public static OnlineStatus STATUS;
    public static String ACTIVITY_TYPE;
    public static String ACTIVITY_TEXT;
    public static String ACTIVITY_URL;
    public static Activity ACTIVITY;

    // Discord Ids
    public static long ID;
    public static long DEVELOPMENT_SERVER;
    public static long LOG_CHANNEL;
}
