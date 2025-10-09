import json
from datetime import datetime
from pathlib import Path
from model.dto.request import sensorRequestDto

class RawSensorService:
    def __init__(self):
        # Create a directory for storing sensor data if it doesn't exist
        self.data_dir = Path("sensor_data")
        self.data_dir.mkdir(exist_ok=True)

    def process_raw_data(self, data: sensorRequestDto):
        print(f"Processing data for user: {data.userId}")
        
        # Save the JSON to a file
        self._save_to_file(data)
        
        # TODO: Process and store in database
        
    def _save_to_file(self, data: sensorRequestDto):
        """Save the sensor data to a JSON file"""
        try:
            # Create a unique filename with timestamp
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
            filename = f"{data.userId}_{timestamp}.json"
            filepath = self.data_dir / filename
            
            # Convert Pydantic model to dict and save as JSON
            with open(filepath, 'w', encoding='utf-8') as f:
                json.dump(data.model_dump(), f, indent=2)
            
            print(f"Saved sensor data to: {filepath}")
            return filepath
            
        except Exception as e:
            print(f"Error saving sensor data: {e}")
            raise ValueError(f"Failed to save sensor data: {str(e)}")