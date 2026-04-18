from fastapi import FastAPI, File, UploadFile
from PIL import Image
import io

from model import LightweightForensicModel

app = FastAPI(title="Deepfake Detection Service", version="0.1.0")
model = LightweightForensicModel()


@app.get("/health")
def health() -> dict:
    return {"status": "ok"}


@app.post("/predict")
async def predict(file: UploadFile = File(...)) -> dict:
    content = await file.read()
    image = Image.open(io.BytesIO(content))
    prediction = model.predict(image)
    return {"label": prediction.label, "confidence": prediction.confidence}
