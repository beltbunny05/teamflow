SELECT 'CREATE DATABASE identity_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'identity_db')\gexec

SELECT 'CREATE DATABASE team_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'team_db')\gexec

SELECT 'CREATE DATABASE project_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'project_db')\gexec

SELECT 'CREATE DATABASE planning_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'planning_db')\gexec
