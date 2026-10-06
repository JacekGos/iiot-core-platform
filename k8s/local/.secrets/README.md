# Local K8s secrets

This folder holds **inputs for creating Kubernetes Secrets** in the local K3s cluster. Nothing here is read by the
applications, Helm or Helmfile automatically.

| File                         | In git? | Purpose                                                     |
|------------------------------|---------|-------------------------------------------------------------|
| `postgres.env.example`       | yes     | Template for the PostgreSQL (`iiot_config`) credentials      |
| `timescaledb.env.example`    | yes     | Template for the TimescaleDB (`iiot_timeseries`) credentials |
| `postgres.env`, `timescaledb.env` | **no** (gitignored) | Your real passwords, created by you, local to your machine |

## How it works

1. Copy each `*.env.example` to the same name without `.example` and replace every `change-me` with a real password.
2. Load each file into the cluster as a Secret. A new `.env` file does nothing until you run these commands:

   ```bash
   kubectl create secret generic postgres-credentials -n iiot-dev --from-env-file=k8s/local/.secrets/postgres.env
   kubectl create secret generic timescaledb-credentials -n iiot-dev --from-env-file=k8s/local/.secrets/timescaledb.env
   ```

3. The values files in `k8s/local/values/` refer to these Secrets by name (`postgres-credentials`,
   `timescaledb-credentials`). The database pods read the passwords from the Secrets, not from these files.

## Changing a password later

Editing a `.env` file or the Secret does **not** change the password of an already initialized database. The database
only reads the password on first start. To rotate a password: run `ALTER USER` in the live database, update the Secret,
then restart the applications that use it. See the "Local K8s Development" section in the repository README.

## Keep a copy

The real `.env` files exist only on one machine. Keep the passwords in your password manager as well.
