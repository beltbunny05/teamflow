CREATE TABLE employees (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    identification VARCHAR(150) NOT NULL,
    primary_role VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    initial_location VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE team_memberships (
    organization_id UUID NOT NULL,
    team_id UUID NOT NULL REFERENCES teams(id),
    employee_id UUID NOT NULL REFERENCES employees(id),
    role VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (team_id, employee_id),
    UNIQUE (organization_id, team_id, employee_id)
);

CREATE TABLE employee_unavailability (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL REFERENCES employees(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(500),
    CHECK (end_date >= start_date)
);

CREATE INDEX idx_employees_org_identification ON employees (organization_id, identification);
CREATE INDEX idx_memberships_org_employee ON team_memberships (organization_id, employee_id);
CREATE INDEX idx_unavailability_employee_period ON employee_unavailability (organization_id, employee_id, start_date, end_date);
