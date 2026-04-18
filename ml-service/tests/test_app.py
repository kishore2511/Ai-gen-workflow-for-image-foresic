from io import BytesIO

from fastapi.testclient import TestClient
from PIL import Image

from app import app

client = TestClient(app)


def _png_bytes() -> bytes:
    image = Image.new("RGB", (8, 8), color=(120, 120, 120))
    buf = BytesIO()
    image.save(buf, format="PNG")
    return buf.getvalue()


def test_health() -> None:
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_predict_rejects_non_image() -> None:
    response = client.post(
        "/predict",
        files={"file": ("bad.txt", b"hello", "text/plain")},
    )
    assert response.status_code == 400


def test_predict_accepts_image() -> None:
    response = client.post(
        "/predict",
        files={"file": ("sample.png", _png_bytes(), "image/png")},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["label"] in {"real", "fake"}
    assert 0.0 <= float(body["confidence"]) <= 1.0
