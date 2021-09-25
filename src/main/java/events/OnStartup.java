package events;

import commands.GlobalCommands;
import commands.LocalCommands;
import main.Main;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import utils.Bot;
import utils.Utils;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OnStartup extends ListenerAdapter {
    public static final Logger LOG = JDALogger.getLog(OnStartup.class);

    /**
     * This method is called once when the bot initially starts. It performs basic setup tasks including loading the bot
     * properties and updating the bot's presence. It also lists the basic information provided with the {@link
     * ReadyEvent}, sending the guild counts to the console.
     *
     * @param event the startup event
     */
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println();
        LOG.info("Running startup processes...");

        LOG.info(String.format("ReadyEvent count: %d available guilds; %d unavailable guilds. Total: %d",
                event.getGuildAvailableCount(),
                event.getGuildUnavailableCount(),
                event.getGuildTotalCount()));

        // Load bot.properties
        Map<String, Boolean> propertyImportResults = loadProperties();

        // Set bot status and activity
        Main.JDA.getPresence().setPresence(Bot.STATUS, Bot.ACTIVITY);
        LOG.info("Updated bot presence");

        // Load slash commands, if applicable
        if (Bot.LOAD_GLOBAL_COMMANDS)
            GlobalCommands.registerGlobalSlashCommands(Main.JDA.updateCommands());
        if (Bot.LOAD_LOCAL_COMMANDS)
            LocalCommands.registerLocalSlashCommands(Objects.requireNonNull(
                    Main.JDA.getGuildById(Bot.DEVELOPMENT_SERVER))
                    .updateCommands());

        // If a startup log message was enabled, send it
        if (Bot.ENABLE_STARTUP_MESSAGE)
            sendLogMessage(propertyImportResults);

        // Create break in console now that setup has finished
        LOG.info("Finished startup processes");
        System.out.println();
    }

    /**
     * This sends a message to {@link Bot#LOG_CHANNEL} whenever the bot starts that contains information on the initial
     * bot state. Primarily, it lists all the properties imported from <code>bot.properties</code>, along with whether
     * they were imported correctly.
     * <p>
     * If the list of property import results is null, something went seriously wrong, and a warning startup message is
     * sent.
     */
    private static void sendLogMessage(@Nullable Map<String, Boolean> results) {
        StringBuilder log = new StringBuilder();

        // Build the startup checklist for each of the properties, or assign it to an error message if loading failed
        if (results == null)
            log.append("ERROR: Failed to properly load bot.properties file. Consult runtime console immediately.");
        else
            for (String property : results.keySet())
                log
                        .append("\n")
                        .append(booleanEmoji(results.get(property)))
                        .append(" ")
                        .append(property);

        // Send the message
        try {
            Objects.requireNonNull(Objects.requireNonNull(Main.JDA
                    .getGuildById(Bot.DEVELOPMENT_SERVER))
                    .getTextChannelById(Bot.LOG_CHANNEL))
                    .sendMessageEmbeds(
                            Utils.makeEmbed(
                                    Main.JDA.getSelfUser().getName() + " Startup Log",
                                    "Bot started on " +
                                    DateTimeFormatter.ofPattern("MM/dd 'at' HH:mm:ss").format(LocalDateTime.now()),
                                    results == null ? Color.RED : Color.WHITE,
                                    Utils.makeEmbedField(
                                            "Bot.properties",
                                            log.substring(1)),
                                    Utils.makeEmbedField(
                                            "Slash Commands",
                                            booleanEmoji(Bot.LOAD_GLOBAL_COMMANDS) + " Loaded global commands\n" +
                                            booleanEmoji(Bot.LOAD_LOCAL_COMMANDS) + " Loaded local commands")
                            ).build())
                    .queue();
        } catch (Exception e) {
            LOG.error("Failed to send startup message to log channel", e);
        }
    }

    /**
     * Returns an emoji corresponding to a boolean. If the boolean is true, <code>:white_check_mark:</code> is returned.
     * If it is false, <code>:no_entry_sign:</code> is returned.
     *
     * @param bool the boolean to use
     * @return a check mark emoji if the boolean is true, or a no entry sign emoji if the boolean is false
     */
    private static String booleanEmoji(boolean bool) {
        return bool ? "\u2705" : "\uD83D\uDEAB";
    }

    /**
     * This loads the configuration settings from the <code>bot.properties</code> resource file and stores the data as
     * instance variables within {@link Bot}.
     */
    private static Map<String, Boolean> loadProperties() {
        Properties prop = new Properties();
        Class<Bot> botClass = Bot.class;
        Map<String, Boolean> map = new HashMap<>();

        // Set the Bot ID and Name
        Bot.ID = Main.JDA.getSelfUser().getIdLong();
        Bot.NAME = Main.JDA.getSelfUser().getName();

        // Attempt to load bot.properties
        try {
            InputStream stream = botClass.getResourceAsStream("/bot.properties");
            if (stream == null) {
                LOG.error("Unable to locate bot.properties. Confirm that it is located in the " +
                          "resources folder for the module containing Bot.java.");
                return null;
            }
            prop.load(stream);
        } catch (IOException e) {
            LOG.error("Failed to read bot.properties on startup.", e);
            return null;
        }


        // Iterate through each of the properties in bot.properties and set the corresponding Bot class field

        boolean success = false;
        for (String property : prop.stringPropertyNames())
            try {
                success = false;
                // Attempt to set the field
                Field field = botClass.getDeclaredField(property.toUpperCase(Locale.ROOT));
                field.set(null, cast(prop.getProperty(property), field.getType()));

                success = true;
            } catch (NoSuchFieldException e) {
                LOG.error("Unable to find a static Bot field '" + property.toUpperCase(Locale.ROOT) +
                          "'. This property was not set.");
            } catch (SecurityException |
                    IllegalAccessException |
                    IllegalArgumentException |
                    NullPointerException e) {
                LOG.error("Unable to set the '" + property.toUpperCase(Locale.ROOT) + "' property " +
                          "within Bot.class. Is it marked public, static, and not final?");
            } catch (ClassNotFoundException e) {
                LOG.error("Unable to set the '" + property.toUpperCase(Locale.ROOT) + "' property " +
                          "within Bot.class. " + e.getMessage());
            } catch (Exception e) {
                LOG.error("Unable to set the '" + property.toUpperCase(Locale.ROOT) + "' property " +
                          "within Bot.class. Cause is unknown.");
                e.printStackTrace();
            } finally {
                // Record whether the property was imported properly
                map.put(property, success);
            }

        try {
            // Generate Activity from activity types
            Bot.ACTIVITY = getActivity(
                    Bot.ACTIVITY_TYPE,
                    Bot.ACTIVITY_TEXT,
                    Bot.ACTIVITY_URL);
        } catch (Exception e) {
            LOG.error("Invalid status. Failed to compile activity properly", e);
        }

        // Log result to console
        int successes = map.values().stream().mapToInt(v -> (v ? 1 : 0)).sum();
        LOG.info(String.format("Loaded %d properties with %d failures from bot.properties",
                successes,
                prop.stringPropertyNames().size() - successes));

        return map;
    }

    /**
     * This returns an {@link Activity} based on the type of activity and the text associated with it. If the activity
     * type is '<code>default</code>', '<code>0</code>', or not recognized, <code>null</code> is returned instead.
     * <p>
     * Activity types are based on the names and keys of {@link Activity.ActivityType} enums. The following activity
     * type values are recognized:
     * <ul>
     *     <li>'<code>default</code>' or '<code>0</code>'</li>
     *     <li>'<code>streaming</code>' or '<code>1</code>'</li>
     *     <li>'<code>listening</code>' or '<code>2</code>'</li>
     *     <li>'<code>watching</code>' or '<code>3</code>'</li>
     *     <li>'<code>competing</code>' or '<code>5</code>'</li>
     *     <li>'<code>playing</code>'</li>
     * </ul>
     *
     * @param type the name of the desired {@link Activity.ActivityType} enum, or its key
     * @param text the text (if applicable) associated with the desired {@link Activity.ActivityType}
     * @param url  the streaming url (only relevant if the activity is <code>"streaming"</code>)
     * @return the newly created {@link Activity}, or <code>null</code>> if the activity type was '<code>default</code>'
     * or unknown
     */
    private static @Nullable Activity getActivity(
            @NotNull String type, @NotNull String text, @Nullable String url) {
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "streaming", "1" -> Activity.streaming(text, url);
            case "playing" -> Activity.playing(text);
            case "listening", "2" -> Activity.listening(text);
            case "watching", "3" -> Activity.watching(text);
            case "competing", "5" -> Activity.competing(text);
            default -> null;
        };
    }

    /**
     * Attempt to parse/cast the {@link String} value obtained from <code>bot.properties</code> into the requested
     * type.
     *
     * @param value the value to parse/cast
     * @param type  the type to convert it to
     * @return the converted object matching the Java type of the type parameter
     */
    private static Object cast(String value, Class<?> type) throws ClassNotFoundException {
        if (type == String.class)
            return value;

            // Check primitive types
        else if (type == Boolean.class || type == Boolean.TYPE)
            return Boolean.parseBoolean(value);
        else if (type == Integer.class || type == Integer.TYPE)
            return Integer.parseInt(value);
        else if (type == Double.class || type == Double.TYPE)
            return Double.parseDouble(value);
        else if (type == Long.class || type == Long.TYPE)
            return Long.parseLong(value);
        else if (type == Float.class || type == Float.TYPE)
            return Float.parseFloat(value);
        else if (type == Short.class || type == Short.TYPE)
            return Short.parseShort(value);
        else if (type == Character.class || type == Character.TYPE)
            return value.charAt(0);
        else if (type == Byte.class || type == Byte.TYPE)
            return Byte.valueOf(value);

            // Check JDA types
        else if (type == OnlineStatus.class)
            return OnlineStatus.fromKey(value);

            // Check misc types
        else if (type == Color.class)
            return new Color(Integer.parseInt(value, 16));

        throw new ClassNotFoundException("Failed to recognize class type " + type.toGenericString());
    }

}
