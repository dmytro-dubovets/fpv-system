package ua.fpv.entity.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PilotRegistry {
    PILOT_1(268843967L, "Свірідов", "Давід"),
    PILOT_2(377451642L, "Дубовець", "Дмитро"),
    UNKNOWN(0L, "Невідомий", "Пілот");

    private final Long chatId;
    private final String lastName;
    private final String firstName;

    public static PilotRegistry getByChatId(Long chatId) {
        for (PilotRegistry pilot : values()) {
            if (pilot.getChatId().equals(chatId)) {
                return pilot;
            }
        }
        return UNKNOWN;
    }
}
