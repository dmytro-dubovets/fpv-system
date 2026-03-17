-- ===============================
-- FPV Entities
-- ===============================

create table if not exists fpv_pilot (
                                         fpv_pilot_id bigserial primary key,
                                         first_name varchar(100),
                                         last_name varchar(100),
                                         user_name varchar(100) unique not null,
                                         password varchar(200) not null,
                                         client_id varchar(200) not null,
                                         created_at timestamp default now(),
                                         updated_at timestamp,
                                         created_by BIGINT REFERENCES fpv_pilot(fpv_pilot_id),
                                         updated_by BIGINT REFERENCES fpv_pilot(fpv_pilot_id)
);

create table if not exists fpv_pilot_authorities (
                                                     fpv_pilot_id bigint not null,
                                                     authority varchar(100) not null,
                                                     foreign key (fpv_pilot_id) references fpv_pilot(fpv_pilot_id)
);

create table if not exists fpv_drone (
                                         fpv_drone_id bigserial primary key,
                                         fpv_serial_number varchar(100),
                                         fpv_craft_name varchar(100),
                                         fpv_model varchar(50)
);

create table if not exists fpv_report (
                                          fpv_report_id bigserial primary key,
                                          fpv_drone_id bigint unique,
                                          date_time_flight timestamp,
                                          fpv_pilot_id bigint not null,
                                          is_lost_fpv_due_to_reb boolean,
                                          is_on_target_fpv boolean,
                                          coordinates_mgrs varchar(100),
                                          additional_info text,
                                          foreign key (fpv_pilot_id) references fpv_pilot(fpv_pilot_id),
                                          foreign key (fpv_drone_id) references fpv_drone(fpv_drone_id)
);

CREATE TABLE IF NOT EXISTS refresh_token (
                                             id BIGSERIAL PRIMARY KEY,
                                             token VARCHAR(255) NOT NULL UNIQUE,
    fpv_pilot_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_refresh_token_pilot
    FOREIGN KEY (fpv_pilot_id)
    REFERENCES fpv_pilot(fpv_pilot_id)
    );

CREATE INDEX idx_refresh_token_value ON refresh_token(token);
