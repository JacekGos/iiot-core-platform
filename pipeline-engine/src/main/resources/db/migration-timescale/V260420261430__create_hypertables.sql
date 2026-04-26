-- flyway:disableTransactionForMigration

CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS data_points (
    time            TIMESTAMPTZ       NOT NULL,
    equipment_id    UUID              NOT NULL,
    tag_name        TEXT              NOT NULL,
    value           DOUBLE PRECISION  NOT NULL,
    quality         TEXT              NOT NULL
);

SELECT create_hypertable('data_points', 'time', chunk_time_interval => INTERVAL '1 week');

SELECT add_retention_policy('data_points', INTERVAL '90 days');

CREATE MATERIALIZED VIEW data_points_hourly
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', time) AS hour,
    equipment_id,
    tag_name,
    avg(value)  AS avg_value,
    min(value)  AS min_value,
    max(value)  AS max_value
FROM data_points
GROUP BY hour, equipment_id, tag_name;