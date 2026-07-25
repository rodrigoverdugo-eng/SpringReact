-- =============================================================================
-- V2: Preferencia de tema por usuario
-- Agrega la columna theme_preference a la tabla users
-- =============================================================================

ALTER TABLE users ADD COLUMN theme_preference VARCHAR(5) NOT NULL DEFAULT 'light';
