package ua.fpv;

import lombok.Data;
import ua.fpv.entity.FpvDroneRequest;
import ua.fpv.entity.FpvReportCreateRequest;

import java.util.ArrayList;
import java.util.List;

@Data // Це автоматично створить методи getReportRequest() та setReportRequest()
public class UserSession {
    private BotState state = BotState.IDLE;

    private List<Integer> messageIds = new ArrayList<>();

    // Ініціалізуємо об'єкт відразу, щоб уникнути null
    private FpvReportCreateRequest reportRequest = new FpvReportCreateRequest();

    public UserSession() {
        // Оскільки в звіті є вкладений об'єкт дрона, його теж треба ініціалізувати
        this.reportRequest.setFpvDrone(new FpvDroneRequest());
    }

    public void clearSession() {
        this.state = BotState.IDLE;
        this.messageIds.clear();
        this.reportRequest = new FpvReportCreateRequest();
        this.reportRequest.setFpvDrone(new FpvDroneRequest()); // Щоб не було null
    }

    // Додаткові поля, якщо ти хочеш зберігати проміжні дані окремо
    private String serialNumber;
    private String coordinates;
}
