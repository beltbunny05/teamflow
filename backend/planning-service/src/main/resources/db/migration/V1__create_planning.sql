CREATE TABLE allocations (
    id UUID PRIMARY KEY, organization_id UUID NOT NULL, project_id UUID NOT NULL,
    resource_type VARCHAR(20) NOT NULL, resource_id UUID NOT NULL,
    start_date DATE NOT NULL, end_date DATE NOT NULL, fixed BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED', created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL,
    CHECK (end_date >= start_date), CHECK (resource_type IN ('TEAM','EMPLOYEE'))
);
CREATE INDEX idx_allocations_org_period ON allocations(organization_id,start_date,end_date);
CREATE INDEX idx_allocations_resource ON allocations(organization_id,resource_type,resource_id);
CREATE TABLE transports (
    id UUID PRIMARY KEY, organization_id UUID NOT NULL, resource_type VARCHAR(20) NOT NULL,
    resource_id UUID NOT NULL, origin VARCHAR(150) NOT NULL, destination VARCHAR(150) NOT NULL,
    start_date DATE NOT NULL, duration_days INTEGER, arrival_date DATE, status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL, updated_at TIMESTAMPTZ NOT NULL,
    CHECK (duration_days IS NULL OR duration_days > 0), CHECK (resource_type IN ('TEAM','EMPLOYEE'))
);
CREATE INDEX idx_transports_resource_period ON transports(organization_id,resource_type,resource_id,start_date);
CREATE TABLE planning_proposals (
    id UUID PRIMARY KEY, organization_id UUID NOT NULL, version BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL, payload JSONB NOT NULL, created_at TIMESTAMPTZ NOT NULL,
    approved_at TIMESTAMPTZ
);
