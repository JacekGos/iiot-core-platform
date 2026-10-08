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

## Local Development Infrastructure

The `docker/` directory contains a Docker Compose configuration that runs all required infrastructure components locally. This allows both `connector-service` and `core-platform` to be developed and tested against real dependencies without mocking.

> **Note:** Neither `connector-service` nor `core-platform` is part of the Docker Compose stack. You run them locally via Gradle or your IDE while the infrastructure stack is running in the background. This gives you fast hot-reload and full debugger access during development.

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) or Docker Engine + Docker Compose plugin
- Ports `80`, `1883`, `5432`, `5433`, `8080`, `9001`, `9092`, `19092` free on your machine

---

### Stack Overview

| Service            | Image                         | Purpose                                      | Ports                         |
|--------------------|-------------------------------|----------------------------------------------|-------------------------------|
| `redpanda`         | redpandadata/redpanda         | Kafka-compatible event streaming             | `9092` (internal), `19092` (host) |
| `redpanda-console` | redpandadata/console          | Redpanda web UI for topic inspection         | `8080`                        |
| `postgres`         | postgres:16-alpine            | Relational storage — pipeline config, assets | `5432`                        |
| `timescaledb`      | timescale/timescaledb-pg16    | Time-series storage — processed sensor data  | `5433`                        |
| `mosquitto`        | eclipse-mosquitto:2           | MQTT broker for local device simulation      | `1883`, `9001` (WebSocket)    |
| `nginx`            | nginx:alpine                  | Reverse proxy — routes API and WebSocket traffic | `80`                      |

All services run on an isolated Docker network `iiot-net` and communicate with each other by container name.

---

### Configuration

The stack is configured via a `.env` file. Before starting for the first time:

```bash
cp docker/.env.example.example docker/.env.example
```

Then open `docker/.env` and set values. The defaults in `.env.example` are safe for local development.

> `.env` is gitignored and must never be committed. `.env.example` is the committed reference, always keep it up to date when adding new variables.

---

### Starting the Stack

```bash
docker-compose -f docker/docker-compose.yml up -d
```

Check that all containers are healthy:

```bash
docker-compose -f docker/docker-compose.yml ps
```

All services should show `healthy` status. Redpanda takes the longest — allow up to 30 seconds on first start.

Follow logs for the whole stack:

```bash
docker-compose -f docker/docker-compose.yml logs -f
```

Follow logs for a specific service:

```bash
docker-compose -f docker/docker-compose.yml logs -f redpanda
```

---

### Stopping the Stack

Stop containers without removing data:

```bash
docker-compose -f docker/docker-compose.yml down
```

---

### Resetting Volumes

> ⚠️ This permanently deletes all local data — Kafka messages, PostgreSQL tables, TimescaleDB records.

```bash
docker-compose -f docker/docker-compose.yml down -v
```

---

### Connecting to Services

#### Redpanda Console (Kafka UI)

Open in browser: [http://localhost:8080](http://localhost:8080)

Use the Console to browse topics, inspect messages, and monitor consumer group lag.

Produce a test message via CLI:

```bash
docker exec -it redpanda rpk topic produce raw-events --brokers localhost:9092
```

Consume messages from a topic:

```bash
docker exec -it redpanda rpk topic consume raw-events --brokers localhost:9092
```

---

#### PostgreSQL

| Property | Value            |
|----------|------------------|
| Host     | `localhost`      |
| Port     | `5432`           |
| Database | `iiot_config`    |
| Username | value from `.env` `POSTGRES_USER` |
| Password | value from `.env` `POSTGRES_PASSWORD` |

Connect via psql:

```bash
docker exec -it postgres psql -U iiot -d iiot_config
```

---

#### TimescaleDB

| Property | Value               |
|----------|---------------------|
| Host     | `localhost`         |
| Port     | `5433`              |
| Database | `iiot_timeseries`   |
| Username | value from `.env` `TIMESCALEDB_USER` |
| Password | value from `.env` `TIMESCALEDB_PASSWORD` |

Connect via psql:

```bash
docker exec -it timescaledb psql -U iiot -d iiot_timeseries
```

Verify TimescaleDB extension is active:

```sql
SELECT default_version, installed_version FROM pg_available_extensions WHERE name = 'timescaledb';
```

---

#### Mosquitto (MQTT Broker)

| Property | Value       |
|----------|-------------|
| Host     | `localhost` |
| Port     | `1883`      |

Mosquitto runs with anonymous access enabled. This matches the production edge deployment model where the broker is inside the customer's private OT network and not exposed externally. If a customer deployment requires MQTT authentication, configure `password_file` in `docker/config/mosquitto.conf`.

Publish a test message:

```bash
mosquitto_pub -h localhost -p 1883 -t "test/sensor" -m '{"value": 42}'
```

Subscribe to a topic:

```bash
mosquitto_sub -h localhost -p 1883 -t "test/#"
```

Alternatively use [MQTT Explorer](https://mqtt-explorer.com/) — a GUI client useful during development.

---

#### Nginx (Reverse Proxy)

| Path         | Routes to                      |
|--------------|--------------------------------|
| `/api/`      | Core Platform REST API         |
| `/ws/`       | Core Platform WebSocket        |
| `/redpanda/` | Redpanda Console UI            |
| `/health`    | Nginx health check (`ok`)      |

> Core Platform is not part of this stack — Nginx will return `502` for `/api/` and `/ws/` until `core-platform` is started locally.

### Kafka Topics

Three topics are created automatically on Redpanda startup via the `redpanda-init` container.

| Topic | Partitions | Cleanup | Retention |
|---|---|---|---|
| `raw-events` | 8 | delete | 24h |
| `config-changes` | 4 | compact | forever (latest per key) |
| `connector-status` | 4 | delete | 24h |

#### Produce and consume test messages

**`raw-events`**
```bash
# produce
docker exec -it redpanda rpk topic produce raw-events --brokers localhost:9092

# consume
docker exec -it redpanda rpk topic consume raw-events --brokers localhost:9092
```

**`config-changes`**
```bash
# produce
docker exec -it redpanda rpk topic produce config-changes --brokers localhost:9092

# consume
docker exec -it redpanda rpk topic consume config-changes --brokers localhost:9092
```

**`connector-status`**
```bash
# produce
docker exec -it redpanda rpk topic produce connector-status --brokers localhost:9092

# consume
docker exec -it redpanda rpk topic consume connector-status --brokers localhost:9092
```

When producing, type your message and hit `Enter` to send, `Ctrl+C` to stop.

---

## Database

This service uses two PostgreSQL-family datastores, each with its own Flyway migration path (see `FlywayConfig`):

| Datastore    | Purpose                                   | Host:Port           | Database          | Migrations location        |
|--------------|--------------------------------------------|----------------------|--------------------|-----------------------------|
| PostgreSQL   | Configuration data (users, sites, zones, equipment, pipelines) | `localhost:5432` | `iiot_config`      | `db/migration`              |
| TimescaleDB  | Time-series data (`data_points` hypertable) | `localhost:5433`     | `iiot_timeseries`  | `db/migration-timescale`    |

Both run as part of this repo's Docker Compose stack (`docker/`, see above). Credentials come from `POSTGRES_USER`/`POSTGRES_PASSWORD` and `TIMESCALEDB_USER`/`TIMESCALEDB_PASSWORD` env vars (defaulted to `iiot` / `password` in the `local` profile). Both migrations run automatically on application startup.

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

### ArgoCD

ArgoCD deploys the services from Git. It lives in its own `argocd` namespace with its own Helmfile, deliberately
separate from the infra above — resetting `iiot-dev` should not take out the thing that puts it back.

#### First install

Run once per cluster — and again only if you rebuild the cluster from scratch.

```bash
cd k8s/argocd
helmfile apply
```

The chart generates a random admin password into a bootstrap secret. Read it, change it, then drop the secret so the
plaintext copy stops sitting in the cluster:

#### Accessing the UI

The server runs with TLS termination disabled (`server.insecure`), so it is reached over plain HTTP through a
port-forward. Port 8081 keeps it clear of the Redpanda Console on 8080.

```bash
kubectl port-forward -n argocd svc/argocd-server 8081:80    # http://localhost:8081
```

Log in as `admin` with the password you set during install.

#### Manual sync

Once Applications exist, sync from the UI (**Sync** on the Application) or from the CLI against the same
port-forward:

```bash
argocd login localhost:8081 --insecure
argocd app sync core-platform
```
