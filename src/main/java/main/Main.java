package main;

import events.OnMessage;
import events.OnSlash;
import events.OnStartup;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.slf4j.Logger;
import utils.Utils;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class Main {
    public static JDA JDA;
    public static final Logger LOG = JDALogger.getLog(Main.class);

    public static void main(String[] args) throws IOException, LoginException {
        String token = new String(
                Utils.loadResource("/bot.token").readAllBytes()
        );

        JDA = JDABuilder.createDefault(token)
                .addEventListeners(new OnMessage())
                .addEventListeners(new OnStartup())
                .addEventListeners(new OnSlash())
                .build();
    }
}
