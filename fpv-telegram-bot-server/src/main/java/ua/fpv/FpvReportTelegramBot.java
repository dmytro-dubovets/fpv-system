package ua.fpv;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.fpv.entity.FpvDroneRequest;
import ua.fpv.entity.FpvReportCreateRequest;
import ua.fpv.entity.model.FpvDrone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class FpvReportTelegramBot extends TelegramLongPollingBot {

    private final FpvApiClient fpvApiClient;

    @Value("${telegram.archive-channel-id}")
    private String archiveChannelId;

    @Value("${telegram.bot.name}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.admin-ids}")
    private List<Long> allowedUsers;

    private final Map<Long, UserSession> userSessions = new ConcurrentHashMap<>();

    @PostConstruct
    public void initMenu() {
        try {
            List<BotCommand> commands = new ArrayList<>();
            commands.add(new BotCommand("/start", "Головне меню"));
            commands.add(new BotCommand("/clear", "Повністю очистити чат")); // Нова команда

            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Помилка меню: {}", e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.hasCallbackQuery()
                ? update.getCallbackQuery().getMessage().getChatId()
                : update.getMessage().getChatId();

        // Перевірка доступу
        if (!allowedUsers.contains(chatId)) {
            sendSimpleMessage(chatId, "Доступ заборонено. ID: " + chatId);
            return;
        }

        UserSession session = userSessions.computeIfAbsent(chatId, k -> createNewSession());

        if (update.hasCallbackQuery()) {
            handleCallback(update, session);
            return;
        }

        if (update.hasMessage()) {
            // ТРЕКІНГ: Зберігаємо ID кожного повідомлення користувача
            session.getMessageIds().add(update.getMessage().getMessageId());

            if (update.getMessage().hasText()) {
                String text = update.getMessage().getText(); // Тепер це точно String

                // КОМАНДА ОЧИЩЕННЯ
                if (text.equals("/clear") || text.equals("🧹 Очистити чат")) {
                    int lastMessageId = update.getMessage().getMessageId();
                    for (int i = 0; i < 50; i++) {
                        deleteMessage(chatId, lastMessageId - i);
                    }
                    sendSimpleMessage(chatId, "✨ Чат очищено!");
                    return;
                }

                if (text.equals("/start")) {
                    session.clearSession();
                    sendMessageWithKeyboard(chatId, "Вітаю! Оберіть дію:", getMainKeyboard(), session);
                    return;
                }

                if (text.equals("📊 Статистика")) {
                    handleStatisticsRequest(chatId, session);
                    return;
                }

                if (text.equals("📝 Створити звіт")) {
                    session.clearSession();
                    session.setState(BotState.AWAITING_SERIAL);
                    sendAndTrackMessage(chatId, "Введіть серійний номер дрона:", session);
                    return;
                }

                handleReportFlow(chatId, text, session);
            }
            else if ((update.getMessage().hasVideo() || update.getMessage().hasDocument())
                    && session.getState() == BotState.AWAITING_VIDEO) {
                handleVideoUpload(update, chatId, session);
            }
        }
    }

    private void handleReportFlow(long chatId, String text, UserSession session) {
        switch (session.getState()) {
            case AWAITING_SERIAL -> {
                session.getReportRequest().getFpvDrone().setFpvSerialNumber(text);
                session.getReportRequest().getFpvDrone().setFpvCraftName("Craft-" + text);
                session.setState(BotState.AWAITING_FPV_DRONE);
                sendAndTrackInline(chatId, "Оберіть тип дрона:", getDroneModelKeyboard(), session);
            }
            case AWAITING_COORDINATES -> {
                session.getReportRequest().setCoordinatesMGRS(text);
                session.setState(BotState.AWAITING_ADDITIONAL_INFO);
                sendAndTrackMessage(chatId, "Введіть додаткову інформацію (деталі вильоту):", session);
            }
            case AWAITING_ADDITIONAL_INFO -> {
                session.getReportRequest().setAdditionalInfo(text);
                session.setState(BotState.AWAITING_RESULT);
                sendAndTrackInline(chatId, "Який результат вильоту?", getHitOrMissKeyboard(), session);
            }
        }
    }

    private void handleCallback(Update update, UserSession session) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String data = update.getCallbackQuery().getData();

        // Видаляємо повідомлення з кнопками одразу після кліку для чистоти
        deleteMessage(chatId, messageId);

        if (session.getState() == BotState.AWAITING_FPV_DRONE) {
            session.getReportRequest().getFpvDrone().setFpvModel(FpvDrone.FpvModel.valueOf(data));
            session.setState(BotState.AWAITING_COORDINATES);
            sendAndTrackMessage(chatId, "Прийнято. Введіть координати (MGRS):", session);
        }
        else if (session.getState() == BotState.AWAITING_RESULT) {
            session.getReportRequest().setOnTargetFPV(data.equals("HIT"));
            session.setState(BotState.AWAITING_REB);
            sendAndTrackInline(chatId, "Чи була втрата через РЕБ?", getYesNoKeyboard("REB"), session);
        }
        else if (session.getState() == BotState.AWAITING_REB) {
            session.getReportRequest().setLostFPVDueToREB(data.equals("REB_YES"));
            session.setState(BotState.AWAITING_VIDEO);

            InlineKeyboardMarkup skipMarkup = InlineKeyboardMarkup.builder()
                    .keyboardRow(List.of(createInlineBtn("Пропустити ⏩", "SKIP_VIDEO")))
                    .build();
            sendAndTrackInline(chatId, "📹 Прикріпіть відео або натисніть 'Пропустити':", skipMarkup, session);
        }
        else if (data.equals("SKIP_VIDEO")) {
            processReportSubmission(chatId, session);
        }
    }

    private void handleVideoUpload(Update update, long chatId, UserSession session) {
        try {
            sendSimpleMessage(chatId, "⏳ Обробка відео...");

            ForwardMessage forward = new ForwardMessage();
            forward.setChatId(archiveChannelId);
            forward.setFromChatId(String.valueOf(chatId));
            forward.setMessageId(update.getMessage().getMessageId());

            Message sent = execute(forward);

            String cleanId = archiveChannelId.replace("-100", "");
            String videoLink = "https://t.me/c/" + cleanId + "/" + sent.getMessageId();

            session.getReportRequest().setAdditionalInfo(
                    (session.getReportRequest().getAdditionalInfo() != null ? session.getReportRequest().getAdditionalInfo() : "")
                            + "\n\n📹 Відео: " + videoLink
            );

            // 1. ПІДТВЕРДЖЕННЯ АРХІВАЦІЇ:
            sendSimpleMessage(chatId, "✅ Відео додано до архіву.");

            // 2. ПІДТВЕРДЖЕННЯ ФОРМУВАННЯ ЗВІТУ (як було раніше):
            sendSimpleMessage(chatId, "📝 Звіт сформовано. Надсилаю на сервер...");

            // 3. ПЕРЕХІД ДО ВІДПРАВКИ:
            processReportSubmission(chatId, session);

        } catch (Exception e) {
            log.error("Помилка відео: {}", e.getMessage());
            sendSimpleMessage(chatId, "⚠️ Не вдалося зберегти відео, відправляю звіт без нього...");
            processReportSubmission(chatId, session);
        }
    }

    private void processReportSubmission(long chatId, UserSession session) {
        log.info("Відправка звіту на сервер для чату: {}", chatId);

        fpvApiClient.sendReport(session.getReportRequest())
                .subscribe(
                        success -> {
                            log.info("Сервер прийняв звіт");
                            // 1. Обов'язково повідомляємо користувача
                            sendSimpleMessage(chatId, "✅ *Звіт успішно сформовано та відправлено!*");

                            // 2. Скидаємо сесію, щоб наступний звіт був чистим
                            session.clearSession();
                        },
                        error -> {
                            log.error("Помилка API: {}", error.getMessage());
                            sendSimpleMessage(chatId, "❌ Помилка: Сервер не зміг прийняти звіт. Перевірте зв'язок.");
                        }
                );
    }

    private void handleStatisticsRequest(long chatId, UserSession session) {
        sendAndTrackMessage(chatId, "⏳ Завантажую статистику...", session);
        fpvApiClient.getStats().subscribe(stats -> {
            String msg = String.format("📊 *Статистика:*\n🚀 Всього: %s\n🎯 Влучань: %s\n📡 РЕБ: %s",
                    stats.get("total"), stats.get("hits"), stats.get("rebLosses"));
            sendAndTrackMessage(chatId, msg, session);
        }, err -> sendSimpleMessage(chatId, "❌ Помилка статистики"));
    }

    // --- ДОПОМІЖНІ МЕТОДИ З ТРЕКІНГОМ ---

    private void sendAndTrackMessage(long chatId, String text, UserSession session) {
        SendMessage sm = SendMessage.builder().chatId(String.valueOf(chatId)).text(text).parseMode("Markdown").build();
        try {
            Message m = execute(sm);
            session.getMessageIds().add(m.getMessageId());
        } catch (TelegramApiException e) { log.error(e.getMessage()); }
    }

    private void sendAndTrackInline(long chatId, String text, InlineKeyboardMarkup kb, UserSession session) {
        SendMessage sm = SendMessage.builder().chatId(String.valueOf(chatId)).text(text).replyMarkup(kb).parseMode("Markdown").build();
        try {
            Message m = execute(sm);
            session.getMessageIds().add(m.getMessageId());
        } catch (TelegramApiException e) { log.error(e.getMessage()); }
    }

    private void sendMessageWithKeyboard(long chatId, String text, ReplyKeyboardMarkup kb, UserSession session) {
        SendMessage sm = SendMessage.builder().chatId(String.valueOf(chatId)).text(text).replyMarkup(kb).parseMode("Markdown").build();
        try {
            Message m = execute(sm);
            session.getMessageIds().add(m.getMessageId());
        } catch (TelegramApiException e) { log.error(e.getMessage()); }
    }

    private void sendSimpleMessage(long chatId, String text) {
        SendMessage sm = SendMessage.builder().chatId(String.valueOf(chatId)).text(text).parseMode("Markdown").build();
        try { execute(sm); } catch (TelegramApiException e) { log.error(e.getMessage()); }
    }

    private void deleteMessage(long chatId, int messageId) {
        try { execute(new DeleteMessage(String.valueOf(chatId), messageId)); }
        catch (TelegramApiException e) { log.warn("Can't delete: {}", messageId); }
    }

    // --- Кнопки ---
    private InlineKeyboardButton createInlineBtn(String text, String data) {
        InlineKeyboardButton b = new InlineKeyboardButton(); b.setText(text); b.setCallbackData(data); return b;
    }

    private InlineKeyboardMarkup getDroneModelKeyboard() {
        return new InlineKeyboardMarkup(List.of(
                List.of(createInlineBtn("🚀 KAMIKAZE", "KAMIKAZE")),
                List.of(createInlineBtn("💣 BOMBER", "BOMBER")),
                List.of(createInlineBtn("🎯 PPO", "PPO"))
        ));
    }

    private InlineKeyboardMarkup getHitOrMissKeyboard() {
        return new InlineKeyboardMarkup(List.of(List.of(createInlineBtn("✅ Влучання", "HIT"), createInlineBtn("❌ Промах", "MISS"))));
    }

    private InlineKeyboardMarkup getYesNoKeyboard(String p) {
        return new InlineKeyboardMarkup(List.of(List.of(createInlineBtn("Так ✅", p + "_YES"), createInlineBtn("Ні ❌", p + "_NO"))));
    }

    public ReplyKeyboardMarkup getMainKeyboard() {
        ReplyKeyboardMarkup m = new ReplyKeyboardMarkup();
        m.setResizeKeyboard(true);
        KeyboardRow r1 = new KeyboardRow();
        r1.add("📝 Створити звіт");
        r1.add("📊 Статистика");
        KeyboardRow r2 = new KeyboardRow();
        r2.add("🧹 Очистити чат"); // Додаємо другим рядом
        m.setKeyboard(List.of(r1, r2));
        return m;
    }

    private UserSession createNewSession() {
        UserSession s = new UserSession();
        s.setState(BotState.IDLE);

        // Створюємо головний об'єкт звіту
        FpvReportCreateRequest request = new FpvReportCreateRequest();

        // ВАЖЛИВО: ініціалізуємо внутрішній об'єкт дрона, щоб не було NULL
        request.setFpvDrone(new FpvDroneRequest());

        s.setReportRequest(request);
        return s;
    }

    @Override public String getBotUsername() { return botUsername; }
    @Override public String getBotToken() { return botToken; }
}