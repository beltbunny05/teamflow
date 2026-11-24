CREATE TABLE teams (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    identification VARCHAR(150) NOT NULL,
    total_members INTEGER NOT NULL CHECK (total_members >= 0),
    composition JSONB,
    competencies JSONB,
    qualifications JSONB,
    availability JSONB,
    status VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_teams_organization_id ON teams (organization_id);
