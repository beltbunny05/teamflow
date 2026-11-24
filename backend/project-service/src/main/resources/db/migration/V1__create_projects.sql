CREATE TABLE projects (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    identification VARCHAR(150) NOT NULL,
    client VARCHAR(150),
    description TEXT,
    planned_start_date DATE,
    planned_end_date DATE,
    estimated_duration_days INTEGER CHECK (estimated_duration_days >= 0),
    competencies JSONB,
    qualifications JSONB,
    localities JSONB,
    services JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_projects_organization_identification
    ON projects (organization_id, identification);
