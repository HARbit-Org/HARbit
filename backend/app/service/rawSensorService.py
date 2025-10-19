# services/raw_sensor_service.py
from pathlib import Path
from datetime import datetime
import json
from model.dto.request import sensorRequestDto
from service.external.harModelService import HarModelService


class RawSensorService:
    def __init__(self, harModelService: HarModelService, data_dir: Path):
        self.harModelService = harModelService
        self.data_dir = data_dir
        self.data_dir.mkdir(parents=True, exist_ok=True)

    def process_raw_data(self, data: sensorRequestDto):
        print(f"Processing data for user: {data.userId}")
        self._save_to_file(data)
        response = self.harModelService.send_data_to_har_model(data)
        # save the response in a json inside self.data_di / har_responses
        if response.get("success"):
            response_dir = self.data_dir / "har_responses"
            response_dir.mkdir(parents=True, exist_ok=True)
            ts = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
            response_filepath = response_dir / f"{data.userId}_response_{ts}.json"
            with open(response_filepath, "w", encoding="utf-8") as f:
                json.dump(response["response_data"].model_dump(), f, indent=2)
            print(f"Saved HAR model response to: {response_filepath}")
        return response

    def _save_to_file(self, data: sensorRequestDto) -> Path:
        ts = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
        filepath = self.data_dir / f"{data.userId}_{ts}.json"
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(data.model_dump(), f, indent=2)
        print(f"Saved sensor data to: {filepath}")
        return filepath
