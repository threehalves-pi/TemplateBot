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

import javax.annotation.Nonnull;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This class contains almost all of the code specific to TemplateBot. This is what retrieves the settings defined in
 * <code>bot.properties</code> and stores them in the {@link Bot} class constants.
 * <p>
 * For the most part, this class should not be modified except to make changes to TemplateBot's underlying structure.
 * <p>
 * If you are trying to execute initial tasks at startup, write a method to execute those tasks, and call your setup
 * method at the end of {@link #startupTasks()}. It will run exactly once when the bot is started. Make sure that your
 * method does not throw any exceptions (except for errors printed to console) so as not to impede the startup process.
 */
public class OnStartup extends ListenerAdapter {
    /**
     * This is the logger for printing bot startup information. Use this only for logging done through {@link
     * #startupTasks()} and the methods that it calls.
     */
    public static final Logger LOG = JDALogger.getLog(OnStartup.class);

    /**
     * This method runs once when the bot starts. Add code to the end of this method that you need to run when the bot
     * loads.
     */
    private static void startupTasks() {
        // Load bot.properties and send startup message (if enabled)
        Map<String, Boolean> propertyImportResults = loadProperties();
        if (Bot.Config.ENABLE_STARTUP_MESSAGE)
            sendLogMessage(propertyImportResults);

        // Set the bot status and activity
        setStatus();

        // Load slash commands, if applicable
        if (Bot.Config.LOAD_GLOBAL_COMMANDS)
            GlobalCommands.registerGlobalSlashCommands(Main.JDA.updateCommands());
        if (Bot.Config.LOAD_LOCAL_COMMANDS)
            LocalCommands.registerLocalSlashCommands(Utils.getGuild(Bot.ID.Guild.DEVELOPMENT).updateCommands());
    }

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

        // Run startup tasks
        try {
            startupTasks();
        } catch (Exception e) {
            LOG.error("Encountered an error while running startup tasks.");
            e.printStackTrace();
        }

        // Create break in console now that setup has finished
        LOG.info("Finished startup processes");
        System.out.println();
    }

    /**
     * This sends a message to {@link Bot.ID.Channel#LOG} whenever the bot starts that contains information on the
     * initial bot state. Primarily, it lists all the properties imported from <code>bot.properties</code>, along with
     * whether they were imported correctly.
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
                                    .getGuildById(Bot.ID.Guild.DEVELOPMENT))
                            .getTextChannelById(Bot.ID.Channel.LOG))
                    .sendMessageEmbeds(
                            Utils.makeEmbed(
                                    Main.JDA.getSelfUser().getName() + " Startup Log",
                                    "Bot started on " +
                                    DateTimeFormatter.ofPattern("MM/dd 'at' HH:mm:ss").format(LocalDateTime.now()),
                                    results == null ? Color.RED : Color.WHITE,
                                    Utils.makeField(
                                            "Bot.properties",
                                            log.substring(1)),
                                    Utils.makeField(
                                            "Slash Commands",
                                            booleanEmoji(Bot.Config.LOAD_GLOBAL_COMMANDS) + " Loaded global " +
                                            "commands\n" +
                                            booleanEmoji(Bot.Config.LOAD_LOCAL_COMMANDS) + " Loaded local commands")
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
     *
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
        Bot.Self.USER = Main.JDA.getSelfUser();
        Bot.Self.ID = Bot.Self.USER.getIdLong();
        Bot.Self.NAME = Bot.Self.USER.getName();

        // Attempt to load bot.properties
        try {
            InputStream stream = Utils.getResourceStream("/bot.properties");
            prop.load(stream);
            stream.close();
        } catch (NullPointerException e) {
            LOG.error("Unable to locate the bot.properties file. Confirm that it is located in the " +
                      "resources folder for the module containing Main.java.");
            return null;
        } catch (IOException e) {
            LOG.error("Failed to read the bot.properties file on startup (IOException).", e);
            return null;
        }

        // Get all the fields in the Bot class and its subclasses
        Map<String, Field> fields = getAllFields(botClass);

        // Iterate through each of the properties in bot.properties and set the corresponding Bot class field
        for (String property : prop.stringPropertyNames())
            map.put(property, setProperty(property, prop.getProperty(property), fields));

        // Log result to console
        int successes = (int) map.values().stream().filter(b -> b).count();
        LOG.info(String.format("Loaded %d properties with %d failures from bot.properties",
                successes,
                prop.stringPropertyNames().size() - successes));

        return map;
    }

    /**
     * Store the value of the given property in the field with the same name.
     *
     * @param property the property name
     * @param value    the property value
     * @param fields   the list of all fields in the {@link Bot} class.
     *
     * @return <code>true</code> if and only if the value is set without errors; <code>false</code> if an exception
     *         is thrown and an error is {@link #LOG logged} to the console.
     */
    private static boolean setProperty(@Nonnull String property, @Nonnull String value,
                                       @Nonnull Map<String, Field> fields) {
        String name = property.toUpperCase();

        try {
            // Attempt to set the field
            Field field = fields.get(property.toUpperCase());
            if (field == null)
                throw new NoSuchFieldException();
            field.set(null, cast(value, field.getType()));

            return true;
        } catch (NoSuchFieldException e) {
            LOG.error("Unable to find a static Bot field with the name '" + name +
                      "'. This property was not set.");
        } catch (SecurityException |
                IllegalAccessException |
                NullPointerException |
                IllegalArgumentException e) {
            LOG.error("Unable to set the property '" + name + "'. Is it marked public, static, and not final?");
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to set the property '" + name + "'. " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Unable to set the property '" + name + "'. Cause is unknown.", e);
        }

        return false;
    }

    /**
     * Get all the {@link Field Fields} accessible within a given {@link Class} and return them as a map based on their
     * {@link Field#getName() name}.
     * <p>
     * If multiple fields with the same name are found, a warning is {@link #LOG logged} to the console and only the
     * first encountered field is included.
     * <p>
     * If any exception occurs that prevents retrieving the fields, a warning is logged and an empty map is returned.
     *
     * @param baseClass the base class to search (all its sub classes are searched as well)
     *
     * @return a map of fields based on their names
     */
    @Nonnull
    private static Map<String, Field> getAllFields(@Nonnull Class<?> baseClass) {
        Map<String, Field> allFields = new HashMap<>();

        // Go through each subclass looking for fields
        List<Class<?>> subClasses = getSubClasses(baseClass);

        //List<Class<?>> subClasses = new ArrayList<>(List.of(baseClass));
        for (Class<?> subClass : subClasses) {
            try {
                // Get each field in the class and add it to the fields map
                Field[] fields = subClass.getDeclaredFields();
                for (Field field : fields)
                    if (allFields.containsKey(field.getName()))
                        LOG.warn(String.format(
                                "Found duplicate field name '%s' within class '%s'. Ignoring duplicate instance.",
                                field.getName(), baseClass.getName()
                        ));
                    else
                        allFields.put(field.getName(), field);

            } catch (SecurityException ignore) {
                LOG.error("Unable to load subclasses of '" + baseClass.getName() + "' class. " +
                          "Encountered a security exception.");
            }
        }

        return allFields;
    }

    /**
     * Retrieve a list of all the subclasses contained within the given class (including the baseClass itself). This
     * runs recursively.
     * <p>
     * If any exception occurs that prevents retrieving the fields, a warning is {@link #LOG logged} and an empty list
     * is returned.
     *
     * @param baseClass the top level class to consider
     *
     * @return an array of the top level class and all sub classes
     */
    @Nonnull
    private static List<Class<?>> getSubClasses(@Nonnull Class<?> baseClass) {
        try {
            List<Class<?>> classes = new ArrayList<>();
            List<Class<?>> subClasses = new ArrayList<>(List.of(baseClass.getClasses()));

            for (Class<?> subClass : subClasses)
                classes.addAll(getSubClasses(subClass));

            classes.add(baseClass);
            return classes;
        } catch (SecurityException e) {
            LOG.error("Unable to load subclasses of '" + baseClass.getName() + "' class. " +
                      "Encountered a security exception.", e);
            return new ArrayList<>();
        }
    }

    /**
     * This creates and sets an {@link Activity} based on the {@link Bot.Status#ACTIVITY_TYPE type}, {@link
     * Bot.Status#ACTIVITY_TEXT text}, and {@link Bot.Status#ACTIVITY_URL URL} parameters from
     * <code>bot.properties</code>.
     * <p>
     * It also sets the bot {@link Bot.Status#STATUS status} at the same time. If the bot status is {@link
     * OnlineStatus#UNKNOWN unknown} or <code>null</code>, no activity or status is set.
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
     */
    private static void setStatus() {
        try {
            try {
                // Create the desired Activity
                Bot.Status.ACTIVITY = switch (Bot.Status.ACTIVITY_TYPE.toLowerCase(Locale.ROOT)) {
                    case "streaming", "1" -> Activity.streaming(Bot.Status.ACTIVITY_TEXT, Bot.Status.ACTIVITY_URL);
                    case "playing" -> Activity.playing(Bot.Status.ACTIVITY_TEXT);
                    case "listening", "2" -> Activity.listening(Bot.Status.ACTIVITY_TEXT);
                    case "watching", "3" -> Activity.watching(Bot.Status.ACTIVITY_TEXT);
                    case "competing", "5" -> Activity.competing(Bot.Status.ACTIVITY_TEXT);
                    default -> null;
                };
            } catch (IllegalArgumentException e) {
                LOG.error("Invalid activity parameters: bot status was not set. Failed to compile activity properly. " +
                          "Check activity_type, activity_text, and activity_url properties.\n" +
                          e.getClass().getName() + ": " + e.getMessage());
                Bot.Status.ACTIVITY = null;
                return;
            }

            // If the status or activity is null or unknown, don't set it.
            if (Bot.Status.ACTIVITY == null || Bot.Status.STATUS == null || Bot.Status.STATUS == OnlineStatus.UNKNOWN)
                return;

            // Set bot status and activity
            Main.JDA.getPresence().setPresence(Bot.Status.STATUS, Bot.Status.ACTIVITY);
            LOG.info("Updated bot presence. Set status to " + Bot.Status.STATUS.name() + ".");

        } catch (Exception e) {
            LOG.error("Encountered an unexpected error while attempting to set the bot status and activity:\n" +
                      e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Attempt to parse/cast the {@link String} value obtained from <code>bot.properties</code> into the requested
     * type.
     *
     * @param value the value to parse/cast
     * @param type  the type to convert it to
     *
     * @return the converted object matching the Java type of the type parameter
     */
    @Nonnull
    private static Object cast(@Nonnull String value, @Nonnull Class<?> type) throws ClassNotFoundException {
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
