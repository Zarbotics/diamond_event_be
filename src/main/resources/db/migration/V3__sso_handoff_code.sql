-- =====================================================================
-- V3 — one-time SSO handoff codes
-- =====================================================================
--
-- Replaces putting the access and refresh tokens directly in the redirect
-- URL after Google or Apple sign-in. A URL is not a private channel: it is
-- recorded in browser history, in server and proxy access logs, and in the
-- Referer header sent to whatever the page loads next. A refresh token
-- captured that way is a durable account takeover.
--
-- The redirect now carries a code that is worthless on its own — it is
-- exchanged once, over POST, within two minutes, and destroyed on use.
--
-- Purely additive: creates one new table and touches nothing existing.
-- =====================================================================

CREATE TABLE IF NOT EXISTS sso_handoff_code (
    ser_sso_handoff_code_id BIGSERIAL PRIMARY KEY,

    -- SHA-256 of the code, never the code itself, so a database snapshot or an
    -- operator reading the table cannot replay a live one.
    txt_code_hash      VARCHAR(64)  NOT NULL UNIQUE,

    txt_access_token   VARCHAR(2048) NOT NULL,
    txt_refresh_token  VARCHAR(512)  NOT NULL,

    dte_expires_at     TIMESTAMPTZ  NOT NULL,
    dte_created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- The scheduled purge deletes by expiry; without this it is a full scan every
-- fifteen minutes.
CREATE INDEX IF NOT EXISTS idx_sso_handoff_code_expires
    ON sso_handoff_code (dte_expires_at);

COMMENT ON TABLE sso_handoff_code IS
    'Single-use, short-lived codes exchanged for auth tokens after SSO. Rows are deleted on redemption; the scheduled purge clears sign-ins that were abandoned.';
