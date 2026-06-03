-- V2: Add two-factor authentication support

-- Add 2FA flag to users
ALTER TABLE users
    ADD two_factor_enabled BIT NOT NULL DEFAULT 0;

-- Store 2FA codes (one-time, time-limited)
CREATE TABLE two_factor_tokens (
    id            BIGINT        PRIMARY KEY IDENTITY,
    user_id       BIGINT        NOT NULL REFERENCES users(id),
    code          VARCHAR(6)    NOT NULL,
    expires_at    DATETIME      NOT NULL,
    used          BIT           NOT NULL DEFAULT 0,
    attempts      INT           NOT NULL DEFAULT 0,
    created_at    DATETIME      NOT NULL DEFAULT GETDATE()
);

CREATE INDEX idx_2fa_tokens_user_id ON two_factor_tokens(user_id);
