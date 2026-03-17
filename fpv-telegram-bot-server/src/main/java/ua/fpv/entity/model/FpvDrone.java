package ua.fpv.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.Objects;

@Data // Створює геттери, сеттери, equals та hashCode по всіх полях автоматично
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FpvDrone {

    private Long fpvDroneId; // Можна залишити, сервер сам заповнить після збереження

    private String fpvSerialNumber;

    private String fpvCraftName;

    private FpvModel fpvModel;

    // ПРИБРАЛИ: private FpvReport fpvReport; (щоб не було циклічного посилання)

    public enum FpvModel {
        KAMIKAZE, BOMBER, PPO;
    }
}

