# AWS Deployment Checklist — HRMS Backend

## Summary: Is the code ready?

**Not fully ready out of the box**, but the repo is now **configured for AWS**. You must:

1. Use **production profile** and provide **secrets via environment variables** (no secrets in repo).
2. Run **RDS (or Aurora) for MySQL** and point the app to it.
3. Use **S3 for file uploads** (optional but recommended) or ensure **EFS/EC2 persistent storage** for upload dirs if you keep local disk.
4. Set **SPRING_PROFILES_ACTIVE=prod** in your AWS environment.

---

## What was added for AWS

- **`application-prod.properties`** — Production config driven by environment variables.
- **Spring Boot Actuator** — Health endpoints for ALB/Elastic Beanstalk: `/actuator/health`, `/actuator/info`.
- **Configurable upload base path** — Prod uses `/tmp/hrms/uploads/` by default (override with `APP_UPLOAD_BASE_DIR`).
- **`.gitignore`** — Ignores `application-local.properties`, `*.local.properties`, `.env` so secrets are not committed.

---

## Required environment variables (production)

Set these in **Elastic Beanstalk** (Configuration → Software → Environment properties), **EC2** (systemd or shell env), or **Parameter Store** (and pass into the app):

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Profile to run | `prod` |
| `SPRING_DATASOURCE_URL` | JDBC URL for RDS/Aurora | `jdbc:mysql://your-db.xxx.us-east-1.rds.amazonaws.com:3306/hrms` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `hrms_app` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | From Secrets Manager |
| `JWT_SECRET` | Secret for JWT signing | Long random string (e.g. from Secrets Manager) |
| `JWT_EXPIRATION` | Access token TTL (ms) | `86400000` |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL (ms) | `604800000` |
| `SPRING_MAIL_USERNAME` | SMTP user (e.g. Gmail) | Your email |
| `SPRING_MAIL_PASSWORD` | SMTP password / app password | From Secrets Manager |
| `APP_BASE_URL` | Public URL of this API | `https://api.yourdomain.com` |
| `APP_MAIL_ADMIN` | Admin email for notifications | `admin@yourcompany.com` |

Optional (have defaults):

- `SERVER_PORT` — default `8080`.
- `APP_UPLOAD_BASE_DIR` — default `/tmp/hrms/uploads/` (on EC2 use a persistent volume path; or switch to S3 later).
- `APP_MAIL_FROM`, `APP_MAIL_FROM_NAME`, `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`.

---

## Security notes

1. **Remove secrets from `application-dev.properties`** before pushing to a shared repo (or add `application-dev.properties` to `.gitignore` if it contains real credentials). Use `application-local.properties` (gitignored) for local dev overrides.
2. **JWT secret**: Generate a strong secret (e.g. 256-bit) and store in **AWS Secrets Manager**; inject into env (e.g. via Beanstalk env or EC2 user data / Parameter Store).
3. **Database**: Prefer **RDS in a private subnet**; run the app in a private subnet with a security group allowing 3306 only to RDS.
4. **CORS**: `WebConfig` currently allows `allowedOriginPatterns("*")`. For production, set explicit origins (e.g. your front-end domain) instead of `*`.

---

## Build and run locally (production-like)

```bash
# Build JAR
./mvnw package -DskipTests

# Run with prod profile (all required env vars must be set)
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:mysql://your-rds-host:3306/hrms
export SPRING_DATASOURCE_USERNAME=hrms
export SPRING_DATASOURCE_PASSWORD=yourpassword
export JWT_SECRET=your-256-bit-secret
export SPRING_MAIL_USERNAME=your@gmail.com
export SPRING_MAIL_PASSWORD=your-app-password
export APP_BASE_URL=http://localhost:8080
export APP_MAIL_ADMIN=admin@example.com
java -jar target/HrmsClient-0.0.1-SNAPSHOT.jar
```

## Deploy on AWS (no Docker)

- **Elastic Beanstalk**: Use the **Java 17 with Corretto** platform. Upload the JAR (or let Beanstalk run `mvn package`) and set the env vars above in **Configuration → Software → Environment properties**. Set `SERVER_PORT=5000` if using the default Beanstalk proxy (or configure nginx to proxy to 8080).
- **EC2**: Install Java 17, copy the JAR to the instance, set environment variables (e.g. in `/etc/environment` or a systemd service file), and run `java -jar HrmsClient-0.0.1-SNAPSHOT.jar`. Use a process manager (systemd) or supervisor to keep it running. For uploads, use a persistent EBS volume and set `APP_UPLOAD_BASE_DIR` to a path on that volume.

---

## Health checks (for ALB / Elastic Beanstalk)

- **Liveness**: `GET /actuator/health/liveness` → 200 if app is up.
- **Readiness**: `GET /actuator/health/readiness` → 200 when DB is reachable and app can serve traffic.
- **Simple**: `GET /actuator/health` → 200 with `{"status":"UP"}`.

Configure your target group / service to use one of these paths (e.g. `/actuator/health`) with interval 30s and healthy threshold 2.

---

## File uploads on AWS

- **EC2 / Beanstalk**: Set `APP_UPLOAD_BASE_DIR` to a path on a **persistent EBS volume** so uploads survive restarts and redeploys.
- **Recommended**: Implement S3 for `attendance`, `profiles`, `documents`, `payslips` and set URLs in responses to S3 (or CloudFront) URLs. Then `APP_UPLOAD_BASE_DIR` and serving via `WebConfig` can be replaced by S3.

---

## Before first production deploy

- [ ] Create RDS MySQL (or Aurora) and run migrations / `ddl-auto=validate` (schema already applied).
- [ ] Store DB password, JWT secret, and mail password in **Secrets Manager** and inject via env (e.g. Beanstalk or EC2).
- [ ] Set `SPRING_PROFILES_ACTIVE=prod` and all required env vars in the runtime environment.
- [ ] Restrict CORS to your front-end origin(s).
- [ ] Plan for file storage: persistent EBS path or S3.
- [ ] Ensure `application-dev.properties` (or any file with real secrets) is not committed or is gitignored.
