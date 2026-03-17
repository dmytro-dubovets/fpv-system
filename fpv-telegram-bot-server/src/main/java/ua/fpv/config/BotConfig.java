package ua.fpv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ua.fpv.FpvReportTelegramBot;

@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(FpvReportTelegramBot bot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        System.out.println("✅ Бот успішно зареєстрований і готовий до роботи (Long Polling)");
        return api;
    }
}