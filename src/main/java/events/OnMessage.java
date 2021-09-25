package events;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utils.Bot;

public class OnMessage extends ListenerAdapter {
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Ignore messages from the bot
        if (event.getAuthor().getIdLong() == Bot.ID)
            return;

        if (event.getMessage().getContentRaw().equals("ping"))
            event.getMessage().reply("pong").queue();
    }
}
