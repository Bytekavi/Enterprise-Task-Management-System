import secrets
from contextlib import asynccontextmanager

import asyncpg
from fastapi import Depends, FastAPI, Header, HTTPException
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    database_url: str = "postgresql://task_readonly:password@db:5432/task_management"
    internal_api_token: str = "development-internal-token-change-me"
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    app.state.pool = await asyncpg.create_pool(settings.database_url, min_size=1, max_size=5)
    yield
    await app.state.pool.close()


app = FastAPI(title="Task Analytics API", version="1.0.0", lifespan=lifespan)


def require_internal_token(x_internal_token: str = Header()) -> None:
    if not secrets.compare_digest(x_internal_token, settings.internal_api_token):
        raise HTTPException(status_code=401, detail="Invalid internal token")


@app.get("/health")
async def health():
    return {"status": "healthy"}


@app.get("/api/v1/summary", dependencies=[Depends(require_internal_token)])
async def summary():
    query = """
        SELECT status, priority, COUNT(*) AS task_count
        FROM tasks
        GROUP BY status, priority
        ORDER BY status, priority
    """
    async with app.state.pool.acquire() as connection:
        rows = await connection.fetch(query)
    return {"groups": [dict(row) for row in rows]}

