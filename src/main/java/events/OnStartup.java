package events;

import commands.GlobalCommands;
import commands.LocalCommands;
import main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import utils.Bot;
import utils.Colors;
import utils.Utils;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * This class contains almost all of the code specific to TemplateBot. This is what retrieves the settings defined in
 * <code>bot.properties</code> and stores them in the {@link Bot} class constants.
 * <p>
 * For the most part, this class should not be modified except to make changes to TemplateBot's underlying structure.
 * <p>
 * <b>If you are trying to execute initial tasks at startup</b>:
 * <ol>
 *     <li>Write a <code>public static</code> method to execute the desired task. Make sure all {@link Exception
 *     Exceptions} are caught within the method.
 *     <li>Call your method somewhere in {@link #startupTasks(Map)}. It will run exactly once when the bot is started.
 *     <li>Optionally, have your method return a {@link Result Result} indicating whether it was successful, and add
 *     that result and a short description of the task to the <code>results</code> map. (See the implementation of
 *     {@link #setStatus()} within {@link #startupTasks(Map) startupTasks()} for an example).
 * </ol>
 */
public class OnStartup extends ListenerAdapter {
    /**
     * This method runs once when the bot starts. Add code to this method that you need to run when the bot loads.
     * <p>
     * Optionally, add a description of the task and a {@link Result Result} to <code>results</code>, and the task will
     * be printed in the {@link Bot.Config#ENABLE_STARTUP_MESSAGE startup message} that the bot sends to the {@link
     * Bot.ID.Channel#LOG log} channel.
     */
    private static void startupTasks(@Nonnull Map<String, Result> results) {
        // Set the bot status and activity
        results.put("Set bot status/activity", setStatus());

        // Load slash commands, if enabled
        loadSlashCommands(results);
    }

    /**
     * This is the logger for printing bot startup information. Use this only for logging done through {@link
     * #startupTasks(Map)} and the methods that it calls.
     */
    public static final Logger LOG = JDALogger.getLog(OnStartup.class);
    private static int propertiesTotal = 0;

    private static int propertiesSuccessful = 0;

    /**
     * The possible results that can be returned when attempting to perform task on startup. When compiled, these are
     * used to help print the {@link Bot.Config#ENABLE_STARTUP_MESSAGE startup message}. There are three possible
     * results:
     * <ul>
     *     <li>{@link #SUCCESS} indicates that the task executed properly
     *     <li>{@link #FAILURE} indicates that the task failed for some reason
     *     <li>{@link #OMITTED} means that the task was not attempted, and thus neither succeeded nor failed
     * </ul>
     */
    public enum Result {
        /**
         * The task executed as expected.
         */
        SUCCESS,

        /**
         * The task encountered an unexpected failure or {@link Exception}, and did not complete successfully.
         */
        FAILURE,

        /**
         * The task was not attempted, typically because it was disabled either by another task or a configuration
         * setting from <code>bot.properties</code>.
         */
        OMITTED;

        /**
         * Returns an emoji corresponding to a {@link Result Result}, according to the following mapping:
         * <ul>
         * <li>{@link #SUCCESS} - "<code>:white_check_mark:</code>"
         * <li>{@link #FAILURE} - "<code>:x:</code>"
         * <li>{@link #OMITTED} - "<code>:no_entry:</code>"
         * </ul>
         *
         * @param result the result to represent as an emoji
         *
         * @return an emoji representing the given result
         */
        private static String emoji(Result result) {
            return switch (result) {
                case SUCCESS -> "\u2705";
                case FAILURE -> "\u274C";
                case OMITTED -> "\u26D4";
            };
        }
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

        // This map stores the results of all the startup tasks. It's used to help print the startup message.
        Map<String, Result> startupResults = new LinkedHashMap<>();

        LOG.info(String.format("ReadyEvent count: %d available guilds; %d unavailable guilds. Total: %d",
                event.getGuildAvailableCount(),
                event.getGuildUnavailableCount(),
                event.getGuildTotalCount()));

        // Load bot.properties and store the results
        if (loadProperties() == Result.FAILURE)
            return;

        if (propertiesTotal == propertiesSuccessful)
            startupResults.put("Loaded `bot.properties`", Result.SUCCESS);
        else
            startupResults.put("Partial failure loading `bot.properties`", Result.FAILURE);

        // Run startup tasks
        try {
            startupTasks(startupResults);
        } catch (Exception e) {
            LOG.error("Encountered an error while running startup tasks.");
            e.printStackTrace();
        }

        // Send the startup message (if enabled)
        if (Bot.Config.ENABLE_STARTUP_MESSAGE)
            sendLogMessage(startupResults);

        // Create break in console now that setup has finished
        LOG.info("Finished startup processes");
        System.out.println();
    }

    /**
     * This sends a message to the {@link Bot.ID.Channel#LOG log} channel containing information on the initial bot
     * state and a report on which {@link #startupTasks(Map) startup tasks} were completed successfully.
     *
     * @param startupResults the map of startup tasks and their corresponding result when executed
     */
    private static void sendLogMessage(@Nonnull Map<String, Result> startupResults) {
        // Define the initial log message
        EmbedBuilder log = Utils.makeEmbed(
                Bot.Self.NAME + " Startup Log",
                String.format("Startup time: %s%nVersion: `%s`",
                        TimeFormat.TIME_LONG.now(), Bot.Self.VERSION),
                Colors.WHITE);

        // Add a field listing the number of successfully loaded properties. If some properties failed to load,
        // change the embed to red and mark an error.
        if (propertiesSuccessful == propertiesTotal)
            log.addField(Utils.makeField("Bot Properties", String.format(
                    "Successfully loaded **%d** properties from `bot.properties` with **0** failures.",
                    propertiesTotal
            )));
        else {
            log.addField(Utils.makeField("Bot Properties Error", String.format(
                    "Failed to load **%d**/**%d** properties from `bot.properties`. " +
                    "Consult the console for more information.",
                    propertiesTotal - propertiesSuccessful,
                    propertiesTotal
            )));
            log.setColor(Colors.RED);
        }

        StringBuilder tasksLog = new StringBuilder();

        // Build the startup checklist for each of the properties
        for (String property : startupResults.keySet())
            tasksLog
                    .append("\n")
                    .append(Result.emoji(startupResults.get(property)))
                    .append(" ")
                    .append(property);

        log.addField(Utils.makeField(
                "Startup Tasks",
                tasksLog.substring(1)));

        // Attempt to send the startup message
        try {
            TextChannel channel = Utils.getGuild(Bot.ID.Guild.DEVELOPMENT).getTextChannelById(Bot.ID.Channel.LOG);
            assert channel != null;
            channel.sendMessageEmbeds(log.build()).queue();
        } catch (NullPointerException ignore) {
            LOG.error("Failed to send the startup message. Couldn't get log channel in development server. " +
                      "Check IDs in bot.properties.");
        } catch (Exception e) {
            LOG.error("Failed to send startup message to the log channel.", e);
        }
    }

    /**
     * This loads the configuration settings from the <code>bot.properties</code> resource file and stores the data as
     * instance variables within {@link Bot}.
     *
     * @return {@link Result#SUCCESS success} if the <code>bot.properties</code> file is loaded successfully. Otherwise
     *         returns {@link Result#FAILURE failure}. This is not directly related to whether any of the properties
     *         themselves are properly loaded.
     */
    @Nonnull
    private static Result loadProperties() {
        Properties prop = new Properties();
        Class<Bot> botClass = Bot.class;

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
            return Result.FAILURE;
        } catch (IOException e) {
            LOG.error("Failed to read the bot.properties file on startup (IOException).", e);
            return Result.FAILURE;
        }

        // Get all the fields in the Bot class and its subclasses
        Map<String, Field> fields = getAllFields(botClass);

        // Iterate through each of the properties in bot.properties and set the corresponding Bot class field
        propertiesTotal = prop.stringPropertyNames().size();
        for (String property : prop.stringPropertyNames())
            if (setProperty(property, prop.getProperty(property), fields) == Result.SUCCESS)
                propertiesSuccessful++;

        // Log result to console
        LOG.info(String.format("Loaded %d properties with %d %s from bot.properties",
                propertiesTotal,
                propertiesTotal - propertiesSuccessful,
                propertiesTotal - propertiesSuccessful == 1 ? "failure" : "failures"));

        return Result.SUCCESS;
    }

    /**
     * Store the value of the given property in the field with the same name.
     *
     * @param property the property name
     * @param value    the property value
     * @param fields   the list of all fields in the {@link Bot} class.
     *
     * @return {@link Result#SUCCESS Success} if and only if the value is set without errors; {@link Result#FAILURE
     *         failure} if an exception is thrown and an error is {@link #LOG logged} to the console.
     */
    @Nonnull
    private static Result setProperty(@Nonnull String property, @Nonnull String value,
                                      @Nonnull Map<String, Field> fields) {
        String name = property.toUpperCase();

        try {
            // Attempt to set the field
            Field field = fields.get(property.toUpperCase());
            if (field == null)
                throw new NoSuchFieldException();
            field.set(null, cast(value, field.getType()));

            return Result.SUCCESS;
        } catch (NoSuchFieldException e) {
            LOG.error("Unable to find a static Bot field with the name '" + name +
                      "'. This property was not set.");
        } catch (SecurityException |
                IllegalAccessException |
                NullPointerException |
                IllegalArgumentException e) {
            LOG.error("Unable to set the property '" + name + "'. Is it marked public, static, and not final?");
        } catch (ClassNotFoundException e) {
            // The data type was not recognized. Use the custom error message thrown by the cast() method
            LOG.error("Unable to set the property '" + name + "'. " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Unable to set the property '" + name + "'. Cause is unknown.", e);
        }

        return Result.FAILURE;
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
     *
     * @return {@link Result#SUCCESS Success} if the status and activity {@link Presence#setPresence(OnlineStatus,
     *         Activity) presence} data were set. Returns {@link Result#OMITTED not_attempted} if this was intentionally
     *         disabled by setting the status to {@link OnlineStatus#UNKNOWN unknown}. Otherwise, returns {@link
     *         Result#FAILURE failure} to indicate an error or failure of some sort.
     */
    private static Result setStatus() {
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
                return Result.FAILURE;
            }

            if (Bot.Status.STATUS == OnlineStatus.UNKNOWN)
                return Result.OMITTED;

            // If the status or activity is null or unknown, don't set it.
            if (Bot.Status.ACTIVITY == null || Bot.Status.STATUS == null)
                return Result.FAILURE;

            // Set bot status and activity
            Main.JDA.getPresence().setPresence(Bot.Status.STATUS, Bot.Status.ACTIVITY);
            LOG.info("Updated bot presence. Set status to " + Bot.Status.STATUS.name() + ".");

        } catch (Exception e) {
            LOG.error("Encountered an unexpected error while attempting to set the bot status and activity:\n" +
                      e.getClass().getName() + ": " + e.getMessage());
        }

        return Result.SUCCESS;
    }

    /**
     * Load the slash commands, if enabled.
     *
     * @param results the map in which to log the {@link Result Results}
     */
    private static void loadSlashCommands(@Nonnull Map<String, Result> results) {
        Result global, local;

        try {
            if (Bot.Config.LOAD_GLOBAL_COMMANDS) {
                GlobalCommands.registerGlobalSlashCommands(Main.JDA.updateCommands());
                global = Result.SUCCESS;
            } else
                global = Result.OMITTED;
        } catch (Exception e) {
            LOG.error("Encountered an unexpected error while attempting to load global slash commands.", e);
            global = Result.FAILURE;
        }

        try {
            if (Bot.Config.LOAD_LOCAL_COMMANDS) {
                LocalCommands.registerLocalSlashCommands(Utils.getGuild(Bot.ID.Guild.DEVELOPMENT).updateCommands());
                local = Result.SUCCESS;
            } else
                local = Result.OMITTED;
        } catch (Exception e) {
            LOG.error("Encountered an unexpected error while attempting to load local slash commands.", e);
            local = Result.FAILURE;
        }

        results.put("Loaded global slash commands", global);
        results.put("Loaded local slash commands", local);
    }
}
