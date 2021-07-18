package events;

import commands.GlobalCommands;
import commands.LocalCommands;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OnSlash extends ListenerAdapter {
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        switch (event.getName()) {
            case "ping" -> GlobalCommands.ping(event);
            case "help" -> GlobalCommands.help(event);
            case "hello" -> LocalCommands.hello(event);
            default -> event
                    .reply("Sorry, I don't recognize that command. Please try again later.")
                    .setEphemeral(true)
                    .queue();
        }
    }
}
