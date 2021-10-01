package utils;

import events.OnStartup;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;

/**
 * This class is designed to work with the <code>bot.properties</code> resource file associated with this Discord bot.
 * All the properties contained in that file have a matching instance variable here that is set with {@link
 * OnStartup#loadProperties() loadproperties()} in {@link OnStartup}.
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
 * {@link OnStartup#loadProperties() loadproperties()} method. If you use a non-standard field type, you will need to
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
    /**
     * Constants pertaining to the bot's own {@link JDA#getSelfUser() self} user, along with its {@link #DESCRIPTION
     * description} and current {@link #VERSION version} number.
     */
    public static class Self {
        /**
         * This is the bot's {@link User} account, equivalent to <code>Main.JDA.getSelfUser()</code>.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #ID
         * @see #NAME
         */
        public static User USER;

        /**
         * The {@link User#getIdLong() id} of the bot's {@link #USER user} account.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #USER
         * @see #NAME
         */
        public static long ID;

        /**
         * The {@link User#getName() id} of the bot's {@link #USER user} account.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #USER
         * @see #ID
         */
        public static String NAME;

        /**
         * The bot's description. This is sent to users when they use <code>/help</code>.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #VERSION
         */
        public static String DESCRIPTION;

        /**
         * The bot's current version number. This is sent to users when they use <code>/help</code>.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #DESCRIPTION
         */
        public static String VERSION;
    }

    /**
     * General, miscellaneous bot configuration settings.
     */
    public static class Config {
        /**
         * If enabled, a message is sent to the {@link ID.Channel#LOG log} channel whenever the bot starts.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see ID.Channel#LOG
         */
        public static boolean ENABLE_STARTUP_MESSAGE;

        /**
         * The prefix that the bot uses for text-based commands in channels.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         */
        public static String PREFIX;

        /**
         * If enabled, the bot's global slash commands will be sent to Discord on {@link OnStartup#startupTasks()
         * startup}. Make sure to disable this while not actively modifying slash commands, as overuse can result in
         * Discord rate-limiting or even banning the bot account.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #LOAD_LOCAL_COMMANDS
         */
        public static boolean LOAD_GLOBAL_COMMANDS;

        /**
         * If enabled, the bot's local slash commands (for each server, namely the {@link ID.Guild#DEVELOPMENT
         * development} one) will be sent to Discord on {@link OnStartup#startupTasks() startup}. This should be
         * disabled while not actively modifying slash commands.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #LOAD_GLOBAL_COMMANDS
         */
        public static boolean LOAD_LOCAL_COMMANDS;
    }

    /**
     * Constants pertaining to the bot's {@link OnlineStatus status} settings at startup (online, idle,
     * listening/playing, etc).
     */
    public static class Status {
        /**
         * The bot's initial {@link OnlineStatus status} on startup.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #ACTIVITY
         */
        public static OnlineStatus STATUS;

        /**
         * The {@link net.dv8tion.jda.api.entities.Activity.ActivityType type} of {@link Activity activity} the bot is
         * doing as part of its status on startup, if any.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #ACTIVITY
         * @see #ACTIVITY_TEXT
         * @see #ACTIVITY_URL
         */
        public static String ACTIVITY_TYPE;

        /**
         * The {@link Activity#getName() text} shown for the {@link Activity activity} the bot is doing as part of its
         * status on startup, if any.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #ACTIVITY
         * @see #ACTIVITY_TYPE
         * @see #ACTIVITY_URL
         */
        public static String ACTIVITY_TEXT;

        /**
         * The {@link Activity#getUrl() URL} of {@link Activity activity} the bot is doing as part of its status on
         * startup, if applicable to the activity {@link net.dv8tion.jda.api.entities.Activity.ActivityType type}.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #ACTIVITY
         * @see #ACTIVITY_TEXT
         * @see #ACTIVITY_TYPE
         */
        public static String ACTIVITY_URL;

        /**
         * The {@link Activity} that the bot is doing on startup. This is created based on the activity {@link
         * #ACTIVITY_TYPE type}, {@link #ACTIVITY_TEXT text}, and optionally a {@link #ACTIVITY_URL URL}.
         * <p>
         * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
         *
         * @see #STATUS
         * @see #ACTIVITY_TYPE
         * @see #ACTIVITY_TEXT
         * @see #ACTIVITY_URL
         */
        public static Activity ACTIVITY;
    }

    /**
     * Discord {@link net.dv8tion.jda.api.entities.ISnowflake IDs}, such as for {@link
     * net.dv8tion.jda.api.entities.Guild guilds}, {@link net.dv8tion.jda.api.entities.MessageChannel channels}, and
     * {@link net.dv8tion.jda.api.entities.User users}, are stored here.
     */
    public static class ID {
        public static class Guild {
            /**
             * The development server for testing the bot. This should be the server that contains the {@link
             * Channel#LOG log} channel.
             * <p>
             * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
             *
             * @see Channel#LOG
             * @see Config#ENABLE_STARTUP_MESSAGE
             */
            public static long DEVELOPMENT;
        }

        public static class Channel {
            /**
             * This is the channel in the {@link Guild#DEVELOPMENT development} server where the startup {@link
             * Config#ENABLE_STARTUP_MESSAGE message} is sent when the bot {@link OnStartup#startupTasks() starts}.
             * <p>
             * <i>This constant is used by the TemplateBot core library. It should not be renamed or removed.</i>
             *
             * @see Guild#DEVELOPMENT
             * @see Config#ENABLE_STARTUP_MESSAGE
             */
            public static long LOG;
        }
    }
}
