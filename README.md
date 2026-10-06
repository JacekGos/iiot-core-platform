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

## Local K8s Development

Infrastructure runs on the local K3s cluster in the `iiot-dev` namespace and is deployed with Helmfile from
`k8s/local/`:

```bash
cd k8s/local
helmfile apply
```

It is run by hand and is **not** synced by ArgoCD (see ADR-005). Full setup guide (prerequisites, secrets, password
rotation, reset): <CONFLUENCE LINK>.

| Component   | In-cluster address                                        | Disk  |
|-------------|-----------------------------------------------------------|-------|
| PostgreSQL  | `postgres:5432` (database `iiot_config`)                  | 2Gi   |
| TimescaleDB | `timescaledb:5432` (database `iiot_timeseries`)           | 5Gi   |
| Mosquitto   | `mosquitto:1883` (MQTT), `mosquitto:9001` (WebSocket)     | 256Mi |
| Redpanda    | `redpanda:9093` (Kafka), `redpanda-console:8080` (UI)     | 5Gi   |

### Check status

```bash
kubectl get pods -n iiot-dev       # all Running; redpanda-configuration shows Completed
kubectl get svc -n iiot-dev
kubectl get pvc -n iiot-dev
```

### Connect from your machine

Services are cluster-internal. Use port-forwarding and stop it with Ctrl+C. Pick other local ports if the Docker
Compose stack is running, since it uses the same ones.

```bash
kubectl port-forward -n iiot-dev svc/postgres 5432:5432
kubectl port-forward -n iiot-dev svc/timescaledb 5433:5432
kubectl port-forward -n iiot-dev svc/redpanda-console 8080:8080    # http://localhost:8080
```

```bash
psql -h localhost -p 5432 -U iiot -d iiot_config
psql -h localhost -p 5433 -U iiot -d iiot_timeseries
```

The Redpanda Kafka API is not exposed outside the cluster.

### Quick checks

```bash
# Mosquitto: publish and receive one message
kubectl exec -n iiot-dev mosquitto-0 -- sh -c 'mosquitto_sub -t test/hello -C 1 -W 5 & sleep 1; mosquitto_pub -t test/hello -m "hello"; wait'

# TimescaleDB: the timescaledb extension should be listed
kubectl exec -n iiot-dev timescaledb-0 -- psql -U iiot -d iiot_timeseries -c "\dx"

# Redpanda: broker health
kubectl exec -n iiot-dev redpanda-0 -c redpanda -- rpk cluster health
```
