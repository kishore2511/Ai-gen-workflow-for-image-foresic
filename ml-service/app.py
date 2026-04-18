from fastapi import FastAPI, File, HTTPException, UploadFile
from PIL import Image
from PIL import UnidentifiedImageError
import io
import os

from model import LightweightForensicModel

app = FastAPI(title="Deepfake Detection Service", version="0.1.0")
model = LightweightForensicModel(model_path=os.getenv("MODEL_PATH", "best_model.pt"))


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/predict")
async def predict(file: UploadFile = File(...)) -> dict:
    if not file.filename:
        raise HTTPException(status_code=400, detail="Missing filename")

    content_type = file.content_type or ""
    if not content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Only image uploads are supported")

    content = await file.read()
    if not content:
        raise HTTPException(status_code=400, detail="Empty file upload")

    try:
        image = Image.open(io.BytesIO(content))
    except UnidentifiedImageError as exc:
        raise HTTPException(status_code=400, detail="Invalid image file") from exc

    prediction = model.predict(image)
    return {"label": prediction.label, "confidence": prediction.confidence}
