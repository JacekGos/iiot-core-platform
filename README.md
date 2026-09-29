# core-platform

This project uses [Gradle](https://gradle.org/).
To build and run the application, use the *Gradle* tool window by clicking the Gradle icon in the right-hand toolbar,
or run it directly from the terminal:

* Run `./gradlew run` to build and run the application.
* Run `./gradlew build` to only build the application.
* Run `./gradlew check` to run all checks, including tests.
* Run `./gradlew clean` to clean all build outputs.

Note the usage of the Gradle Wrapper (`./gradlew`).
This is the suggested way to use Gradle in production projects.

[Learn more about the Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

[Learn more about Gradle tasks](https://docs.gradle.org/current/userguide/command_line_interface.html#common_tasks).

This project follows the suggested multi-module setup and consists of the `app` and `utils` subprojects.
The shared build logic was extracted to a convention plugin located in `buildSrc`.

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).

## Database

This service uses two PostgreSQL-family datastores, each with its own Flyway migration path (see `FlywayConfig`):

| Datastore    | Purpose                                   | Host:Port           | Database          | Migrations location        |
|--------------|--------------------------------------------|----------------------|--------------------|-----------------------------|
| PostgreSQL   | Configuration data (users, sites, zones, equipment, pipelines) | `localhost:5432` | `iiot_config`      | `db/migration`              |
| TimescaleDB  | Time-series data (`data_points` hypertable) | `localhost:5433`     | `iiot_timeseries`  | `db/migration-timescale`    |

Both run as part of the `connector-service` repo's Docker Compose stack. Credentials come from `POSTGRES_USER`/`POSTGRES_PASSWORD` and `TIMESCALEDB_USER`/`TIMESCALEDB_PASSWORD` env vars (defaulted to `iiot` / `password` in the `local` profile). Both migrations run automatically on application startup.

### Inspecting the schema

Connect with `psql` (or any GUI client, e.g. DBeaver):

```bash
psql -h localhost -p 5432 -U iiot -d iiot_config
psql -h localhost -p 5433 -U iiot -d iiot_timeseries
```

### Verifying the TimescaleDB hypertable

Once the app has started (and Flyway has run the `db/migration-timescale` migrations), you can confirm the `data_points` hypertable is set up correctly:

```bash
psql -h localhost -p 5433 -U iiot -d iiot_timeseries
```

```sql
-- confirm it's registered as a hypertable
SELECT * FROM timescaledb_information.hypertables WHERE hypertable_name = 'data_points';

-- insert a test row
INSERT INTO data_points (time, equipment_id, tag_name, value, quality)
VALUES (now(), gen_random_uuid(), 'test.tag', 42.0, 'GOOD');

-- read it back
SELECT * FROM data_points ORDER BY time DESC LIMIT 1;

-- confirm the continuous aggregate view exists
SELECT * FROM data_points_hourly LIMIT 5;
```

Data persists across container restarts via the `timescaledb_data` Docker volume, and old rows are dropped automatically after 90 days by the configured retention policy.
