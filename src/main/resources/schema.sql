CREATE TABLE IF NOT EXISTS users (
    id           UUID          NOT NULL DEFAULT RANDOM_UUID() PRIMARY KEY,
    name         VARCHAR(255)  NOT NULL,
    email        VARCHAR(255)  NOT NULL,
    password     VARCHAR(255)  NOT NULL,
    token        TEXT,
    is_active    BOOLEAN       NOT NULL DEFAULT TRUE,
    created      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

CREATE TABLE IF NOT EXISTS phones (
    id           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id      UUID          NOT NULL,
    number       VARCHAR(50)   NOT NULL,
    city_code    VARCHAR(10)   NOT NULL,
    country_code VARCHAR(10)   NOT NULL,

    CONSTRAINT fk_phones_user FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_phones_user_id ON phones (user_id);
