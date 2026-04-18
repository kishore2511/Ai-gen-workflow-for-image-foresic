from dataclasses import dataclass
import numpy as np
from PIL import Image


@dataclass
class Prediction:
    label: str
    confidence: float


class LightweightForensicModel:
    """
    A deterministic baseline model for project scaffolding.
    Replace with a trained EfficientNet checkpoint for production use.
    """

    def predict(self, image: Image.Image) -> Prediction:
        rgb = image.convert("RGB")
        arr = np.asarray(rgb).astype(np.float32) / 255.0

        # Simple forensic-style handcrafted signals.
        channel_std = arr.std(axis=(0, 1)).mean()
        gradient_x = np.abs(np.diff(arr, axis=1)).mean()
        gradient_y = np.abs(np.diff(arr, axis=0)).mean()
        texture_score = float((gradient_x + gradient_y) / 2.0)

        # Heuristic fake probability (placeholder).
        fake_prob = float(np.clip((0.55 * texture_score + 0.45 * (1 - channel_std)), 0.01, 0.99))
        label = "fake" if fake_prob >= 0.5 else "real"
        confidence = fake_prob if label == "fake" else 1 - fake_prob
        return Prediction(label=label, confidence=round(confidence, 4))
