CREATE TABLE allocation_member_snapshots (
    allocation_id UUID NOT NULL REFERENCES allocations(id),
    organization_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    source VARCHAR(30) NOT NULL DEFAULT 'TEAM_COMPOSITION',
    exception_kind VARCHAR(20),
    recorded_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (allocation_id, employee_id),
    CHECK (source IN ('TEAM_COMPOSITION', 'INDIVIDUAL', 'LEGACY_UNAVAILABLE')),
    CHECK (exception_kind IS NULL OR exception_kind IN ('EXCLUDE', 'REALLOCATE'))
);
CREATE INDEX idx_allocation_member_snapshots_org_employee
    ON allocation_member_snapshots (organization_id, employee_id);

-- Existing allocations intentionally remain without rows: their historical
-- composition cannot be reconstructed reliably from the current Team Service.
