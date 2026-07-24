-- =============================================================================
-- V1: Esquema inicial
-- Tablas: roles, users, user_login_history
-- =============================================================================

CREATE TABLE roles (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(10)  NOT NULL,
    descripcion VARCHAR(100),
    CONSTRAINT  uk_roles_name UNIQUE (name)
);

CREATE TABLE users (
    id                       BIGSERIAL    PRIMARY KEY,
    name                     VARCHAR(100) NOT NULL,
    email                    VARCHAR(100) NOT NULL,
    password                 VARCHAR(100) NOT NULL,
    requires_password_change BOOLEAN      NOT NULL,
    vigencia                 BOOLEAN      NOT NULL,
    role_id                  BIGINT       NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT fk_users_role  FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE user_login_history (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    login_at   TIMESTAMP(6) NOT NULL,
    ip_address VARCHAR(45),
    CONSTRAINT fk_login_history_user FOREIGN KEY (user_id) REFERENCES users(id)
);
