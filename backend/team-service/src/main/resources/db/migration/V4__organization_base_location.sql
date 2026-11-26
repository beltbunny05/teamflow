CREATE TABLE organization_settings (
    organization_id UUID PRIMARY KEY,
    base_location VARCHAR(150),
    updated_at TIMESTAMPTZ NOT NULL
);
