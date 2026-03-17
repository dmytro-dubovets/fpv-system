-- 1. Таблиця дронів (FpvDrone)
CREATE TABLE IF NOT EXISTS fpv_drone (
                                         fpv_drone_id BIGSERIAL PRIMARY KEY,
                                         fpv_serial_number VARCHAR(100) UNIQUE,
    fpv_craft_name VARCHAR(255),
    fpv_model VARCHAR(50) -- Енум: KAMIKAZE, BOMBER, PPO
    );

-- 2. Таблиця пілотів (FpvPilot - версія ресурсного сервера)
CREATE TABLE IF NOT EXISTS fpv_pilot (
                                         fpv_pilot_id BIGSERIAL PRIMARY KEY,
                                         first_name VARCHAR(255),
    last_name VARCHAR(255),
    user_name VARCHAR(255) UNIQUE NOT NULL
    );

-- 3. Таблиця репортів (FpvReport)
CREATE TABLE IF NOT EXISTS fpv_report (
                                          fpv_report_id BIGSERIAL PRIMARY KEY,
                                          fpv_drone_id BIGINT UNIQUE REFERENCES fpv_drone(fpv_drone_id) ON DELETE CASCADE,
    date_time_flight TIMESTAMP,
    created_by_username VARCHAR(255) NOT NULL, -- Заповнюється через @CreatedBy
    created_at TIMESTAMP,                      -- Заповнюється через @CreatedDate
    is_lost_fpv_due_to_reb BOOLEAN DEFAULT FALSE,
    is_on_target_fpv BOOLEAN DEFAULT FALSE,
    coordinates_mgrs VARCHAR(50),
    additional_info TEXT
    );

-- Індекси для швидкого пошуку по юзернейму
CREATE INDEX IF NOT EXISTS idx_report_created_by ON fpv_report(created_by_username);