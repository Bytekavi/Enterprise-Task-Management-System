# Enterprise Task Management System

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Python](https://img.shields.io/badge/Python-3.12-3776AB?logo=python&logoColor=white)](https://www.python.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![AWS](https://img.shields.io/badge/AWS-ECS-FF9900?logo=amazonwebservices&logoColor=white)](https://aws.amazon.com/ecs/)
[![GCP](https://img.shields.io/badge/GCP-Cloud_Run-4285F4?logo=googlecloud&logoColor=white)](https://cloud.google.com/run)
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?logo=githubactions&logoColor=white)](https://github.com/features/actions)

A secure, scalable task-management platform with a transactional Spring Boot API,
read-only Python analytics service, PostgreSQL RBAC, and automated deployments to AWS
ECS or Google Cloud Run.

## Tech Stack

| Layer | Technologies |
|---|---|
| Backend | Java 21, Spring Boot, Spring MVC |
| Analytics | Python 3.12, FastAPI, AsyncPG |
| Data | PostgreSQL 16, Spring Data JPA, Flyway |
| Security | Spring Security, JWT, bcrypt, API and database RBAC |
| Concurrency | ACID transactions, pessimistic write locks, optimistic versioning |
| Testing | JUnit 5, Mockito, Spring Security Test, pytest |
| Containers | Docker, Docker Compose |
| CI/CD | GitHub Actions, OWASP Dependency Check, Bandit, pip-audit, Trivy |
| Cloud | AWS ECS/Fargate, ECR, GCP Cloud Run, Artifact Registry |

## Key Engineering Features

- Service-repository-controller architecture with request validation and centralized errors
- Owner-scoped data access with `USER`, `MANAGER`, and `ADMIN` roles
- PostgreSQL grants plus row-level security as defense in depth
- Composite and partial indexes for common task-list and due-date queries
- Pessimistic locking and entity versions to prevent lost updates and race conditions
- Stateless JWT authentication with 12-round bcrypt password hashing
- Pagination for safely handling thousands of concurrent records
- Exactly 22 JUnit tests covering domain rules, authentication, stale writes, ownership,
  privilege escalation, credential handling, and malformed tokens
- Keyless GitHub OIDC authentication for AWS and GCP deployments

## Architecture

```text
Client -> Spring Security/JWT -> Controllers -> Transactional services
                                             -> JPA repositories -> PostgreSQL
Internal client -> FastAPI analytics ----------------------------^ (read only)

GitHub Actions -> tests/scans -> Docker image -> GCP Cloud Run or AWS ECS
```

## Run Locally

Requirements: Docker Desktop and Docker Compose.

```bash
cp .env.example .env
# Replace every development password and secret in .env.
docker compose up --build
```

- Java API: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- Python analytics: `http://localhost:8081/docs`

## REST API

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |
| GET | `/api/v1/tasks` | Authenticated, owner/assignee scoped |
| POST | `/api/v1/tasks` | Authenticated |
| PUT | `/api/v1/tasks/{id}` | Owner or admin |
| DELETE | `/api/v1/tasks/{id}` | Owner or admin |
| POST | `/api/v1/tasks/{id}/assignee` | Manager or admin |
| GET | Analytics `/api/v1/summary` | Internal token |

## Test and Scan

```bash
mvn verify
cd analytics
pip install -r requirements.txt
pytest
bandit -r app
pip-audit -r requirements.txt
```

## Deployment

The workflows use short-lived OIDC credentials. Configure repository variables and secrets
referenced in `.github/workflows/deploy-gcp.yml` or `deploy-aws.yml`; never store cloud keys
in the repository. Replace placeholders in the ECS task definition with actual IAM roles,
Secrets Manager ARNs, log region, and runtime database configuration.

For production, use Cloud SQL or Amazon RDS with private networking, TLS-enforced connections,
point-in-time recovery, database audit logs, secret rotation, and a migration job that runs
before application rollout.

## Security Notes

- RLS does not replace API authorization; both layers are intentionally enforced.
- Use a dedicated migration identity because application runtime roles should not own tables.
- Rate-limit authentication endpoints at the gateway/load balancer.
- Treat JWT and internal analytics tokens as secrets and rotate them regularly.
- Review dependency and container findings before suppressing any scanner result.

## License

MIT

