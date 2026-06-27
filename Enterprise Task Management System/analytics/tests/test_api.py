from fastapi.testclient import TestClient

from app.main import app


def test_health():
    client = TestClient(app)
    response = client.get("/health")
    assert response.status_code == 200


def test_summary_requires_token():
    client = TestClient(app)
    response = client.get("/api/v1/summary")
    assert response.status_code == 422
