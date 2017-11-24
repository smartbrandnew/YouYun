CREATE USER datamonitor WITH PASSWORD 'datamonitor';
GRANT SELECT ON pg_stat_database TO datamonitor;
CREATE DATABASE datamonitor_test;
GRANT ALL PRIVILEGES ON DATABASE datamonitor_test TO datamonitor;
CREATE DATABASE monitors;
