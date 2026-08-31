-- Add session_id column to refresh_tokens for session management.
-- When refreshing tokens, the same sessionId is reused to maintain the session.
-- When logging out, the session is deleted from Redis and all refresh tokens for the user are invalidated.

ALTER TABLE refresh_tokens
    ADD COLUMN session_id VARCHAR(36) NOT NULL DEFAULT '';
