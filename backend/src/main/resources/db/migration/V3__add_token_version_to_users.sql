-- =============================================================================
-- V3: Invalidación de tokens JWT
-- Agrega la columna token_version a la tabla users. Los tokens llevan el valor
-- vigente al momento de emitirse; al incrementarlo se invalidan los anteriores.
-- =============================================================================

ALTER TABLE users ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
