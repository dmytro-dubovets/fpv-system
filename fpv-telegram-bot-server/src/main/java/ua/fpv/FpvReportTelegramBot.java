package ua.fpv;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
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
import ua.fpv.entity.model.FpvPilot;
import ua.fpv.entity.model.PilotRegistry;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.awt.SystemColor.text;

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

        // 1. Перевірка доступу
        if (!allowedUsers.contains(chatId)) {
            sendSimpleMessage(chatId, "Доступ заборонено. ID: " + chatId);
            return;
        }

        UserSession session = userSessions.computeIfAbsent(chatId, k -> createNewSession());

        // 2. Обробка кнопок під повідомленнями (Inline)
        if (update.hasCallbackQuery()) {
            handleCallback(update, session);
            return;
        }

        // 3. Обробка звичайних повідомлень
        if (update.hasMessage()) {
            session.getMessageIds().add(update.getMessage().getMessageId());

            // А) ОБРОБКА ТЕКСТУ (Команди та введення даних)
            if (update.getMessage().hasText()) {
                String text = update.getMessage().getText();

                if (text.equals("/clear") || text.equals("🧹 Очистити чат")) {
                    int lastId = update.getMessage().getMessageId();
                    for (int i = 0; i < 50; i++) deleteMessage(chatId, lastId - i);
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

                if (text.equals("📥 Завантажити Excel")) {
                    handleExcelDownloadRequest(chatId);
                    return;
                }

                if (text.equals("📝 Створити звіт")) {
                    session.clearSession();
                    session.setState(BotState.AWAITING_SERIAL);
                    sendAndTrackMessage(chatId, "Введіть серійний номер дрона:", session);
                    return;
                }

                // Будь-який інший текст йде в логіку звіту
                handleReportFlow(chatId, text, session);
            }
            // Б) ОБРОБКА ВІДЕО / ФАЙЛІВ
            else if ((update.getMessage().hasVideo() || update.getMessage().hasDocument())
                    && session.getState() == BotState.AWAITING_VIDEO) {
                handleVideoUpload(update, chatId, session);
            }
            // В) ВСЕ ІНШЕ (стікери, фото без тексту тощо)
            else {
                sendSimpleMessage(chatId, "Будь ласка, скористайтеся кнопками меню або введіть коректну команду.");
            }
        }
    }

    private void handleReportFlow(long chatId, String text, UserSession session) {
        switch (session.getState()) {
            case AWAITING_SERIAL -> {
                session.getReportRequest().getFpvDrone().setFpvSerialNumber(text);
                session.getReportRequest().getFpvDrone().setFpvCraftName(text);
                session.setState(BotState.AWAITING_FPV_DRONE);
                sendAndTrackInline(chatId, "Оберіть тип дрона:", getDroneModelKeyboard(), session);
            }
            case AWAITING_COORDINATES -> {
                session.getReportRequest().setCoordinatesMGRS(text);
                session.setState(BotState.AWAITING_ADDITIONAL_INFO);
                sendAndTrackMessage(chatId, "Введіть додаткову інформацію (деталі вильоту):", session);
            }
            case AWAITING_ADDITIONAL_INFO -> {
                // Зберігаємо текст опису
                session.getReportRequest().setAdditionalInfo(text);

                // ПЕРЕХОДИМО ДО РЕЗУЛЬТАТУ (Влучання/Промах)
                session.setState(BotState.AWAITING_RESULT);
                sendAndTrackInline(chatId, "🎯 Який результат вильоту?", getHitOrMissKeyboard(), session);
            }
            default -> {
                sendSimpleMessage(chatId, "⚠️ Невідома команда...");
            }
        }
    }

    private void handleCallback(Update update, UserSession session) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String data = update.getCallbackQuery().getData();

        // 1. Одразу видаляємо повідомлення з кнопками, щоб уникнути повторних натискань
        deleteMessage(chatId, messageId);

        // 2. ПРІОРИТЕТ: Обробка пропуску відео
        if (data.equals("SKIP_VIDEO")) {
            log.info("Користувач {} вибрав пропуск відео", chatId);

            String info = session.getReportRequest().getAdditionalInfo();
            session.getReportRequest().setAdditionalInfo(
                    (info != null ? info : "") + "\n\n📹 Відео: відсутнє"
            );

            sendSimpleMessage(chatId, "📝 Звіт сформовано (без відео). Надсилаю на сервер...");
            processReportSubmission(chatId, session);
            return;
        }

        // 3. ВИБІР МОДЕЛІ ДРОНА
        if (session.getState() == BotState.AWAITING_FPV_DRONE) {
            session.getReportRequest().getFpvDrone().setFpvModel(FpvDrone.FpvModel.valueOf(data));
            session.setState(BotState.AWAITING_COORDINATES);
            sendAndTrackMessage(chatId, "Прийнято. Введіть координати (MGRS):", session);
        }

        // 4. ОБРОБКА РЕЗУЛЬТАТУ (Влучання / Промах / Обрив)
        else if (session.getState() == BotState.AWAITING_RESULT) {
            if (data.equals("HIT")) {
                // ВЛУЧАННЯ -> Ставимо мітки і пропускаємо РЕБ
                session.getReportRequest().setOnTargetFPV(true);
                session.getReportRequest().setLostFPVDueToREB(false);

                goToVideoUploadState(chatId, session);
            }
            else if (data.equals("MISS")) {
                // ПРОМАХ -> Запитуємо про РЕБ
                session.getReportRequest().setOnTargetFPV(false);
                session.setState(BotState.AWAITING_REB);
                sendAndTrackInline(chatId, "Чи була втрата через РЕБ?", getYesNoKeyboard("REB"), session);
            }
            else if (data.equals("FIBER_CUT")) {
                // ОБРИВ (Оптика) -> Одразу до відео
                session.getReportRequest().setOnTargetFPV(false);
                session.getReportRequest().setLostFPVDueToREB(false);

                String currentInfo = session.getReportRequest().getAdditionalInfo();
                session.getReportRequest().setAdditionalInfo(
                        (currentInfo != null ? currentInfo : "") + "\n📍 Результат: Обрив оптоволокна"
                );

                goToVideoUploadState(chatId, session);
            }
        }

        // 5. ВІДПОВІДЬ ПРО РЕБ (тільки якщо був промах)
        else if (session.getState() == BotState.AWAITING_REB) {
            session.getReportRequest().setLostFPVDueToREB(data.equals("REB_YES"));
            goToVideoUploadState(chatId, session);
        }
    }

    /**
     * Допоміжний метод для переходу до завантаження відео.
     * Викликається після Влучання, Обриву або відповіді про РЕБ.
     */
    private void goToVideoUploadState(long chatId, UserSession session) {
        session.setState(BotState.AWAITING_VIDEO);

        InlineKeyboardMarkup skipMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(createInlineBtn("Пропустити відео ⏩", "SKIP_VIDEO")))
                .build();

        sendAndTrackInline(chatId, "📹 Прикріпіть відео або натисніть 'Пропустити':", skipMarkup, session);
    }

    private void handleVideoUpload(Update update, long chatId, UserSession session) {
        try {

            // --- НОВИЙ БЛОК ПЕРЕВІРКИ РОЗМІРУ ---
            long fileSize = 0;
            if (update.getMessage().hasVideo()) {
                fileSize = update.getMessage().getVideo().getFileSize();
            } else if (update.getMessage().hasDocument()) {
                fileSize = update.getMessage().getDocument().getFileSize();
            }

            // Перевірка на 50 МБ (50 * 1024 * 1024 байт)
            if (fileSize > 52428800) {
                log.warn("Користувач {} надіслав завеликий файл: {} байт", chatId, fileSize);
                sendSimpleMessage(chatId, "⚠️ *Файл занадто великий (>50MB)*.\n" +
                        "На жаль, Telegram обмежує завантаження таких файлів для ботів.\n" +
                        "Надсилаю звіт без відео...");
                processReportSubmission(chatId, session);
                return; // Виходимо з методу, далі код не йде
            }

            sendSimpleMessage(chatId, "⏳ Обробка та архівування відео...");

            String fileId;
            String fileName = "video.mp4";
            boolean isVideo = false;

            if (update.getMessage().hasVideo()) {
                fileId = update.getMessage().getVideo().getFileId();
                isVideo = true;
            } else {
                fileId = update.getMessage().getDocument().getFileId();
                fileName = update.getMessage().getDocument().getFileName();
            }

            // 1. Отримуємо дані пілота з нашого реєстру
            PilotRegistry pilot = PilotRegistry.getByChatId(chatId);
            String pilotName = pilot.getLastName() + " " + pilot.getFirstName();
            String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            // 2. Формуємо гарний підпис для архіву
            String caption = String.format(
                    "📹 <b>Новий звіт з відео</b>\n" +
                            "👤 Пілот: %s\n" +
                            "📅 Дата: %s\n" +
                            "🆔 ChatID: %s\n" +
                            "🤖 Дрон: %s (%s)",
                    pilotName,
                    timestamp,
                    chatId,
                    session.getReportRequest().getFpvDrone().getFpvSerialNumber(),
                    session.getReportRequest().getFpvDrone().getFpvModel()
            );

            Message sent;
            if (isVideo) {
                org.telegram.telegrambots.meta.api.methods.send.SendVideo sendVideo = new org.telegram.telegrambots.meta.api.methods.send.SendVideo();
                sendVideo.setChatId(archiveChannelId);
                sendVideo.setVideo(new InputFile(fileId));
                sendVideo.setCaption(caption);
                sendVideo.setParseMode("HTML");
                sent = execute(sendVideo);
            } else {
                org.telegram.telegrambots.meta.api.methods.send.SendDocument sendDoc = new org.telegram.telegrambots.meta.api.methods.send.SendDocument();
                sendDoc.setChatId(archiveChannelId);
                sendDoc.setDocument(new InputFile(fileId));
                sendDoc.setCaption(caption);
                sendDoc.setParseMode("HTML");
                sent = execute(sendDoc);
            }

            // 3. Генеруємо посилання на повідомлення в архіві
            String cleanId = archiveChannelId.replace("-100", "");
            String videoLink = "https://t.me/c/" + cleanId + "/" + sent.getMessageId();

            session.getReportRequest().setAdditionalInfo(
                    (session.getReportRequest().getAdditionalInfo() != null ? session.getReportRequest().getAdditionalInfo() : "")
                            + "\n\n📹 Відео в архіві: " + videoLink
            );

            sendSimpleMessage(chatId, "✅ Відео підписано та додано до архіву.");
            sendSimpleMessage(chatId, "📝 Надсилаю фінальний звіт на сервер...");

            processReportSubmission(chatId, session);

        } catch (Exception e) {
            log.error("Помилка при пересиланні відео з підписом: {}", e.getMessage());
            sendSimpleMessage(chatId, "⚠️ Не вдалося підписати відео, спробую відправити звіт без посилання...");
            processReportSubmission(chatId, session);
        }
    }

    private void processReportSubmission(long chatId, UserSession session) {
        log.info("Відправка звіту на сервер для чату: {}", chatId);

        // Встановлюємо chatId як ідентифікатор пілота
        session.getReportRequest().setFpvPilotId(chatId);

        session.getReportRequest().setCreatedByUsername(String.valueOf(chatId));

        log.info("Відправка звіту. Пілот: {}", session.getReportRequest().getCreatedByUsername());

        fpvApiClient.sendReport(session.getReportRequest())
                .subscribe(
                        success -> {
                            log.info("Сервер прийняв звіт від чату {}", chatId);
                            sendSimpleMessage(chatId, "✅ *Звіт успішно сформовано та відправлено!*");
                            session.clearSession();
                        },
                        error -> {
                            log.error("Помилка API для чату {}: {}", chatId, error.getMessage());
                            sendSimpleMessage(chatId, "❌ Помилка: Сервер не зміг прийняти звіт. Перевірте зв'язок.");
                        }
                );
    }

    private void handleStatisticsRequest(long chatId, UserSession session) {
        sendAndTrackMessage(chatId, "⏳ Завантажую статистику...", session);

        fpvApiClient.getStats().subscribe(stats -> {
            // Отримуємо значення з мапи, додаючи захист від null (getOrDefault)
            Object total = stats.getOrDefault("total", 0);
            Object hits = stats.getOrDefault("hits", 0);
            Object reb = stats.getOrDefault("rebLosses", 0);
            Object fiber = stats.getOrDefault("fiberCuts", 0); // Нове поле

            String msg = String.format(
                    "📊 *Статистика:*\n" +
                            "🚀 Всього вильотів: %s\n" +
                            "🎯 Влучань: %s\n" +
                            "📡 Втрачено через РЕБ: %s\n" +
                            "✂️ Обривів оптики: %s",
                    total, hits, reb, fiber
            );

            sendAndTrackMessage(chatId, msg, session);
        }, err -> {
            log.error("Помилка статистики: {}", err.getMessage());
            sendSimpleMessage(chatId, "❌ Помилка: Не вдалося отримати статистику з сервера.");
        });
    }

    private void handleExcelDownloadRequest(long chatId) {
        sendSimpleMessage(chatId, "⏳ Формую файл, зачекайте...");

        fpvApiClient.downloadExcelReports()
                .doOnError(e -> log.error("❌ ПОМИЛКА WEBCLIENT ПРИ ЗАВАНТАЖЕННІ EXCEL: ", e)) // Покаже помилку в логах бота
                .subscribe(fileBytes -> {
                    if (fileBytes == null || fileBytes.length == 0) {
                        log.warn("⚠️ Сервер повернув порожній масив байтів для чату {}", chatId);
                        sendSimpleMessage(chatId, "⚠️ Файл порожній або даних для звіту не знайдено.");
                        return;
                    }

                    log.info("📂 Отримано Excel файл розміром: {} байт", fileBytes.length);

                    try {
                        SendDocument sendDocument = new SendDocument();
                        sendDocument.setChatId(String.valueOf(chatId));

                        // Використовуємо ByteArrayInputStream для формування файлу
                        InputFile inputFile = new InputFile(new ByteArrayInputStream(fileBytes), "FPV_Reports.xlsx");
                        sendDocument.setDocument(inputFile);
                        sendDocument.setCaption("📊 Повний звіт по дронах та вильотах");

                        execute(sendDocument);
                        log.info("✅ Excel успішно надіслано користувачу {}", chatId);
                    } catch (Exception e) {
                        log.error("❌ Помилка Telegram API при відправці документа: ", e);
                        sendSimpleMessage(chatId, "❌ Помилка при відправці файлу в Telegram.");
                    }
                }, err -> {
                    // Цей блок спрацює, якщо WebClient отримає 4xx або 5xx помилку
                    log.error("❌ КРИТИЧНА ПОМИЛКА ПРИ ЗВЕРНЕННІ ДО API: {}", err.getMessage());
                    sendSimpleMessage(chatId, "❌ Не вдалося отримати дані з сервера. Перевірте статус Resource Server.");
                });
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
        return new InlineKeyboardMarkup(List.of(
                List.of(createInlineBtn("✅ Влучання", "HIT"),
                        createInlineBtn("❌ Промах", "MISS")),
                List.of(createInlineBtn("✂️ Обрив (оптика)", "FIBER_CUT"))
        ));

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
        r2.add("📥 Завантажити Excel");
        r2.add("🧹 Очистити чат");
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