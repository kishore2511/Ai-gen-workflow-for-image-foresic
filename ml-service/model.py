from dataclasses import dataclass
from pathlib import Path
import numpy as np
from PIL import Image
import torch
import torch.nn as nn
from torchvision import models, transforms


@dataclass
class Prediction:
    label: str
    confidence: float


class LightweightForensicModel:
    """
    A deterministic baseline model for project scaffolding.
    Replace with a trained EfficientNet checkpoint for production use.
    """

    def __init__(self, model_path: str = "best_model.pt"):
        self.model_path = Path(model_path)
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self.image_size = 224
        self.use_heuristic_fallback = not self.model_path.exists()
        self.transform = transforms.Compose(
            [
                transforms.Resize((self.image_size, self.image_size)),
                transforms.ToTensor(),
                transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
            ]
        )
        self.model = None if self.use_heuristic_fallback else self._load_model()

    def predict(self, image: Image.Image) -> Prediction:
        if self.model is not None:
            return self._predict_with_checkpoint(image)
        return self._predict_with_heuristic(image)

    def _predict_with_checkpoint(self, image: Image.Image) -> Prediction:
        self.model.eval()
        rgb = image.convert("RGB")
        tensor = self.transform(rgb).unsqueeze(0).to(self.device)
        with torch.inference_mode():
            logits = self.model(tensor)
            probs = torch.softmax(logits, dim=1).squeeze(0).cpu().numpy()
        fake_prob = float(probs[1])
        label = "fake" if fake_prob >= 0.5 else "real"
        confidence = fake_prob if label == "fake" else 1.0 - fake_prob
        return Prediction(label=label, confidence=round(confidence, 4))

    def _predict_with_heuristic(self, image: Image.Image) -> Prediction:
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

    def _load_model(self) -> nn.Module:
        model = models.efficientnet_b0(weights=None)
        in_features = model.classifier[1].in_features
        model.classifier[1] = nn.Linear(in_features, 2)
        checkpoint = torch.load(self.model_path, map_location=self.device)

        state_dict = checkpoint["state_dict"] if isinstance(checkpoint, dict) and "state_dict" in checkpoint else checkpoint
        model.load_state_dict(state_dict)
        model.to(self.device)
        return model
