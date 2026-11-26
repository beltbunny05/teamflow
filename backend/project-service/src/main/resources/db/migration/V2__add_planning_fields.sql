ALTER TABLE projects ADD COLUMN priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL';
ALTER TABLE projects ADD COLUMN continuous_execution BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE projects ADD COLUMN desired_people INTEGER CHECK (desired_people IS NULL OR desired_people >= 0);
ALTER TABLE projects ADD CONSTRAINT projects_priority_check CHECK (priority IN ('LOW','NORMAL','HIGH','CRITICAL'));
