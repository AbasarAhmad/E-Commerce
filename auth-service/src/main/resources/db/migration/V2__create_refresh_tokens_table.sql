CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token VARCHAR(500) NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_refresh_tokens_token (token),

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);