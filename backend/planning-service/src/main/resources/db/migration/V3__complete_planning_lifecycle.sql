ALTER TABLE allocations ADD COLUMN revision BIGINT NOT NULL DEFAULT 0;
ALTER TABLE transports ADD COLUMN revision BIGINT NOT NULL DEFAULT 0;
ALTER TABLE planning_proposals ADD COLUMN base_revision BIGINT NOT NULL DEFAULT 0;
ALTER TABLE planning_proposals ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE TABLE planning_state (
    organization_id UUID PRIMARY KEY,
    allocation_revision BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL
);

INSERT INTO planning_state (organization_id, allocation_revision, updated_at)
SELECT organization_id, 0, CURRENT_TIMESTAMP
FROM allocations
GROUP BY organization_id
ON CONFLICT (organization_id) DO NOTHING;

CREATE TABLE allocation_exceptions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    allocation_id UUID NOT NULL REFERENCES allocations(id),
    employee_id UUID NOT NULL,
    kind VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    target_project_id UUID,
    target_start_date DATE,
    target_end_date DATE,
    reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    CHECK (kind IN ('EXCLUDE', 'REALLOCATE')),
    CHECK (end_date >= start_date),
    CHECK (kind = 'EXCLUDE' OR target_project_id IS NOT NULL)
);
CREATE INDEX idx_allocation_exceptions_period
    ON allocation_exceptions (organization_id, allocation_id, employee_id, start_date, end_date);

CREATE TABLE planning_change_log (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    details JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_planning_change_log_org_created
    ON planning_change_log (organization_id, created_at);
