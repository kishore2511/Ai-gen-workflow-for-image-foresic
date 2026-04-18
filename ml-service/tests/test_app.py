from fastapi.testclient import TestClient

import app as app_module


class StubModel:
    def predict(self, image):  # noqa: ANN001
        return type("Prediction", (), {"label": "fake", "confidence": 0.99})()


client = TestClient(app_module.app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_predict_rejects_non_image_content_type():
    response = client.post(
        "/predict",
        files={"file": ("bad.txt", b"abc123", "text/plain")},
    )
    assert response.status_code == 400
    assert "Only image uploads are supported" in response.text


def test_predict_accepts_valid_image(monkeypatch):
    monkeypatch.setattr(app_module, "model", StubModel())

    # Tiny valid PNG bytes
    png_bytes = (
        b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01"
        b"\x00\x00\x00\x01\x08\x02\x00\x00\x00\x90wS\xde\x00"
        b"\x00\x00\nIDATx\x9cc`\x00\x00\x00\x02\x00\x01\xe2!"
        b"\xbc3\x00\x00\x00\x00IEND\xaeB`\x82"
    )

    response = client.post(
        "/predict",
        files={"file": ("tiny.png", png_bytes, "image/png")},
    )
    assert response.status_code == 200
    assert response.json() == {"label": "fake", "confidence": 0.99}
