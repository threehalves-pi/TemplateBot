package events;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utils.Bot;
import utils.Utils;

import java.util.Locale;

public class OnMessage extends ListenerAdapter {
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Get the user who sent the message
        User user = event.getAuthor();

        // Ignore messages from the bot
        if (user.getIdLong() == Bot.Self.ID)
            return;

        // Get the message and its contents
        Message message = event.getMessage();
        String contents = message.getContentRaw();

        // Ignore messages that don't use the prefix. If the prefix IS used, remove it.
        if (contents.startsWith(Bot.Config.PREFIX) && contents.length() > Bot.Config.PREFIX.length())
            contents = contents.substring(Bot.Config.PREFIX.length());
        else
            return;

        // Get the channel that the message was sent in, and parse the command arguments
        MessageChannel channel = event.getChannel();
        String[] args = contents.split("\\s+");

        // Check and evaluate commands
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "ping" -> channel.sendMessage("pong").queue();
            case "pong" -> channel.sendMessage("ping").queue();
            default -> Utils.error(message, "Sorry, I don't recognize that command.");
        }
    }
}
