ALTER TABLE planning_proposals
    ADD CONSTRAINT uq_planning_proposals_organization_version
    UNIQUE (organization_id, version);
