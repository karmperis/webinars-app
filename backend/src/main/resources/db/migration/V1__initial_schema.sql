--V1__initial_schema.sql
--SQL Server 2022 (16.0.1175)
--Collation: GREEK_CI_AS

/*
============================================================================
SECURITY & AUTHORIZATION CONTROL
============================================================================
Tables for roles, capabilities, users and authentication.
*/
CREATE TABLE roles(
    id BIGINT IDENTITY(1,1) NOT NULL,
    uuid UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    name NVARCHAR(50) NOT NULL,
    created_at DATETIME2(6) NOT NULL,
    updated_at DATETIME2(6) NOT NULL,
    deleted_at DATETIME2(6) NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_roles_name_not_deleted
    ON roles (name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ix_roles_uuid
    ON roles (uuid);

CREATE TABLE capabilities(
    id BIGINT IDENTITY(1,1) NOT NULL,
    uuid UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
    name NVARCHAR(50) NOT NULL,
    description NVARCHAR(255) NULL,
    created_at DATETIME2(6) NOT NULL,
    updated_at DATETIME2(6) NOT NULL,
    deleted_at DATETIME2(6) NULL,
    CONSTRAINT pk_capabilities PRIMARY KEY(id)
);
CREATE UNIQUE INDEX ix_capabilities_name_not_deleted
    ON capabilities (name)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ix_capabilities_uuid
    ON capabilities (uuid);

CREATE TABLE roles_capabilities(
    role_id BIGINT NOT NULL,
    capability_id BIGINT NOT NULL,
    CONSTRAINT pk_roles_capabilities PRIMARY KEY (role_id, capability_id),

    CONSTRAINT fk_roles_capabilities_role_id
        FOREIGN KEY(role_id) REFERENCES roles(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_roles_capabilities_capability_id
        FOREIGN KEY(capability_id) REFERENCES capabilities(id)
            ON DELETE CASCADE
);
CREATE INDEX ix_roles_capabilities_capability_id
    ON roles_capabilities(capability_id);

CREATE TABLE users(
  id BIGINT IDENTITY(1,1) NOT NULL,
  uuid UNIQUEIDENTIFIER NOT NULL DEFAULT NEWID(),
  username NVARCHAR(255) NOT NULL,
  password NVARCHAR(255) NOT NULL,
  role_id BIGINT NOT NULL,
  active BIT NOT NULL DEFAULT 0,
  created_at DATETIME2(6) NOT NULL,
  updated_at DATETIME2(6) NOT NULL,
  deleted_at DATETIME2(6) NULL,
  CONSTRAINT pk_users PRIMARY KEY(id),

  CONSTRAINT fk_users_role_id
      FOREIGN KEY(role_id) REFERENCES roles(id)
        ON DELETE NO ACTION
);
CREATE UNIQUE INDEX ix_users_username_not_deleted
    ON users(username)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX ix_users_uuid
    ON users(uuid);

CREATE INDEX ix_users_role_id ON users(role_id);