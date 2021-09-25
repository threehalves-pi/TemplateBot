package commands;

import events.OnStartup;
import main.Main;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import utils.Bot;
import utils.Utils;

import java.awt.*;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class GlobalCommands {
    public static void registerGlobalSlashCommands(CommandListUpdateAction action) {
        List<CommandData> commands = new ArrayList<>();

        commands.add(new CommandData("ping", "Ping the bot."));
        commands.add(new CommandData("help", "Get info about " + Main.JDA.getSelfUser().getName()));

        action.addCommands(commands).queue();
        OnStartup.LOG.info("Registered global slash commands");
    }

    public static void ping(SlashCommandEvent event) {
        event.reply(String.format("pong (%d)",
                Duration.between(event.getTimeCreated(), OffsetDateTime.now()).toMillis()))
                .queue();
    }

    public static void help(SlashCommandEvent event) {
        event.replyEmbeds(Utils.makeEmbed(
                Bot.NAME + " Info",
                "Hi, I'm " + Bot.NAME + "! " + Bot.BOT_DESCRIPTION,
                Color.WHITE,
                Utils.makeEmbedField("Version", "I'm currently running `" + Bot.VERSION + "`.", true)
        ).build()).setEphemeral(true).queue();
    }
}
