package ua.fpv.util;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Data
public class AppError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

}
