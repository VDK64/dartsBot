package com.dartsBot.dartsBot.mainBot;

import com.dartsBot.dartsBot.repository.MatchRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@EqualsAndHashCode(callSuper = true)
@Component
@Data
public class MainDarts extends TelegramLongPollingBot {
    @Value("${bot.token}")
    private String botToken;
    @Value("${bot.username}")
    private String botUsername;
    @Autowired
    private MatchRepository matchRepo;

//    private static DefaultBotOptions setOptions() {
//        DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
//        botOptions.setProxyHost("45.13.30.144");
//        botOptions.setProxyPort(60079);
//        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
//        return botOptions;
//    }
//
//    public MainDarts() {
//        super(setOptions());
//    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            ThreadBot threadBot = new ThreadBot(update, matchRepo, this);
            threadBot.start();
        }
    }

    public synchronized void exeResponse(SendMessage response) {
        try {
            execute(response);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}


