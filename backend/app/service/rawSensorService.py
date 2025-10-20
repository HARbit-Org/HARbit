# services/raw_sensor_service.py
from pathlib import Path
from datetime import datetime
import json
import uuid
from sqlalchemy.orm import Session
from model.dto.request import sensorRequestDto
from model.entity.rawSensorRecords import RawSensorRecords
from service.external.harModelService import HarModelService


class RawSensorService:
    def __init__(self, harModelService: HarModelService, data_dir: Path):
        self.harModelService = harModelService
        self.data_dir = data_dir
        self.data_dir.mkdir(parents=True, exist_ok=True)

    def process_raw_data(self, data: sensorRequestDto, db: Session = None):
        print(f"Processing data for user: {data.userId}")
        
        # Save to database if session is provided
        if db:
            saved_record = self._save_to_database(data, db)
            print(f"Saved to database with ID: {saved_record.id}")
        
        # Send to HAR model
        response = self.harModelService.send_data_to_har_model(data)

        # Optionally save response to file
        # if response.get("success"):
        #     response_dir = self.data_dir / "har_responses"
        #     response_dir.mkdir(parents=True, exist_ok=True)
        #     ts = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
        #     response_filepath = response_dir / f"{data.userId}_response_{ts}.json"
        #     with open(response_filepath, "w", encoding="utf-8") as f:
        #         json.dump(response["response_data"].model_dump(), f, indent=2)
        #     print(f"Saved HAR model response to: {response_filepath}")
        
        return response

    def _save_to_database(self, data: sensorRequestDto, db: Session) -> RawSensorRecords:
        """Save raw sensor data to database using SQLAlchemy"""
        try:
            # Create a unique client_record_id if not present
            client_record_id = f"{data.userId}_{datetime.now().strftime('%Y%m%d_%H%M%S_%f')}"
            
            # Create the database record
            db_record = RawSensorRecords(
                user_id=uuid.UUID(data.userId) if data.userId != "TODO_GET_USER_ID" else uuid.uuid4(),
                ts=datetime.now(),
                duration_ms=None,  # You can calculate this from the data if needed
                client_record_id=client_record_id,
                payload=data.model_dump()  # Store the entire request as JSON
            )
            
            # Add to session and commit
            db.add(db_record)
            db.commit()
            db.refresh(db_record)
            
            print(f"Successfully saved raw sensor record to database")
            return db_record
            
        except Exception as e:
            print(f"Error saving to database: {e}")
            db.rollback()
            raise

    def _save_to_file(self, data: sensorRequestDto) -> Path:
        ts = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
        filepath = self.data_dir / f"{data.userId}_{ts}.json"
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(data.model_dump(), f, indent=2)
        print(f"Saved sensor data to: {filepath}")
        return filepath
