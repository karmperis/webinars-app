--V1__initial_schema.sql
--SQL Server 2022 (16.0.1175)
--Collation: GREEK_CI_AS

/*
============================================================================
Database Cleanup / Schema Reset
============================================================================
Caution: Drops all existing tables to provide a clean state for migrations.
============================================================================
*/
DROP TABLE IF EXISTS tokens;
DROP TABLE IF EXISTS roles_capabilities;
DROP TABLE IF EXISTS users_webinars;
DROP TABLE IF EXISTS users_details;
DROP TABLE IF EXISTS webinars;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS capabilities;

/*
============================================================================
Authentication & Authorization
============================================================================
Tables for authentication (users) and authorization (roles, capabilities).
*/

CREATE TABLE roles
(
    id         BIGINT IDENTITY(1,1) NOT NULL,
    uuid       UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    name       NVARCHAR(50) NOT NULL,
    created_at DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    deleted_at DATETIME2(6) NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_roles_name_not_deleted
    ON roles (name) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ix_roles_uuid
    ON roles (uuid);

CREATE TABLE capabilities
(
    id          BIGINT IDENTITY(1,1) NOT NULL,
    uuid        UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    name        NVARCHAR(50) NOT NULL,
    description NVARCHAR(255) NULL,
    created_at  DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at  DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    deleted_at  DATETIME2(6) NULL,
    CONSTRAINT pk_capabilities PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_capabilities_name_not_deleted
    ON capabilities (name) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ix_capabilities_uuid
    ON capabilities (uuid);

CREATE TABLE roles_capabilities
(
    role_id       BIGINT NOT NULL,
    capability_id BIGINT NOT NULL,
    CONSTRAINT pk_roles_capabilities PRIMARY KEY (role_id, capability_id),

    CONSTRAINT fk_roles_capabilities_role_id
        FOREIGN KEY (role_id) REFERENCES roles (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_roles_capabilities_capability_id
        FOREIGN KEY (capability_id) REFERENCES capabilities (id)
            ON DELETE CASCADE
);
CREATE INDEX ix_roles_capabilities_capability_id
    ON roles_capabilities (capability_id);

CREATE TABLE users
(
    id         BIGINT IDENTITY(1,1) NOT NULL,
    uuid       UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    username   NVARCHAR(255) NOT NULL,
    password   NVARCHAR(255) NOT NULL,
    role_id    BIGINT           NOT NULL,
    active     BIT              NOT NULL DEFAULT 0,
    created_at DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    deleted_at DATETIME2(6) NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),

    CONSTRAINT fk_users_role_id
        FOREIGN KEY (role_id) REFERENCES roles (id)
            ON DELETE NO ACTION
);
CREATE UNIQUE INDEX ix_users_username_not_deleted
    ON users (username) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ix_users_uuid
    ON users (uuid);

CREATE INDEX ix_users_role_id
    ON users (role_id);

/*
============================================================================
AUTHENTICATION TOKENS
============================================================================
Tokens for account verification and password reset.
*/

CREATE TABLE tokens
(
    id         BIGINT IDENTITY(1,1) NOT NULL,
    token      NVARCHAR(255) NOT NULL,
    type       NVARCHAR(50) NOT NULL,
    used       BIT    NOT NULL DEFAULT 0,
    user_id    BIGINT NOT NULL,
    created_at DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    expiry_at  DATETIME2(6) NOT NULL,
    CONSTRAINT pk_tokens PRIMARY KEY (id),

    CONSTRAINT fk_tokens_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE UNIQUE INDEX ix_tokens_token
    ON tokens (token);

CREATE INDEX ix_tokens_user_id
    ON tokens (user_id);

/*
============================================================================
Domain tables
============================================================================
Tables for business logic: User details, webinars and users_webinars.
*/

CREATE TABLE users_details
(
    user_id      BIGINT NOT NULL,
    firstname    NVARCHAR(100) NOT NULL,
    lastname     NVARCHAR(100) NOT NULL,
    phone_number NVARCHAR(20) NULL,
    created_at   DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at   DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT pk_users_details PRIMARY KEY (user_id),

    CONSTRAINT fk_users_details_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE TABLE webinars
(
    id             BIGINT IDENTITY(1,1) NOT NULL,
    uuid           UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    title          NVARCHAR(100) NOT NULL,
    description    NVARCHAR(1000),
    scheduled_date DATETIME2(6) NOT NULL,
    duration       INT              NOT NULL DEFAULT 0,
    user_id        BIGINT           NOT NULL,
    created_at     DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    updated_at     DATETIME2(6) NOT NULL DEFAULT SYSUTCDATETIME(),
    deleted_at     DATETIME2(6) NULL,
    CONSTRAINT pk_webinars PRIMARY KEY (id),

    CONSTRAINT fk_webinars_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE NO ACTION
);
CREATE UNIQUE INDEX ix_webinars_uuid
    ON webinars (uuid);

CREATE INDEX ix_webinars_scheduled_date
    ON webinars (scheduled_date);

CREATE INDEX ix_webinars_user_id
    ON webinars (user_id);

CREATE TABLE users_webinars
(
    user_id    BIGINT NOT NULL,
    webinar_id BIGINT NOT NULL,
    CONSTRAINT pk_users_webinars PRIMARY KEY (user_id, webinar_id),

    CONSTRAINT fk_users_webinars_user_id
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_users_webinars_webinar_id
        FOREIGN KEY (webinar_id) REFERENCES webinars (id)
            ON DELETE CASCADE
);

CREATE INDEX ix_users_webinars_webinar_id
    ON users_webinars (webinar_id);