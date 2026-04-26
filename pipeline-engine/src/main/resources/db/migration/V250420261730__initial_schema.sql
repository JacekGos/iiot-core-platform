CREATE TABLE users
(
    id            UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    username      VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE sites
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE zones
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    site_id UUID         NOT NULL REFERENCES sites (id) ON DELETE CASCADE,
    name    VARCHAR(255) NOT NULL
);

CREATE TABLE equipments
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    zone_id UUID         NOT NULL REFERENCES zones (id) ON DELETE CASCADE,
    name    VARCHAR(255) NOT NULL,
    type    VARCHAR(100)
);

CREATE TABLE pipelines
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    status      VARCHAR(50)  NOT NULL DEFAULT 'STOPPED',
    config_json JSONB        NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);