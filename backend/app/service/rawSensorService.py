# services/raw_sensor_service.py
from pathlib import Path
from datetime import datetime, timezone
import json
import uuid
from sqlalchemy.orm import Session
from model.dto.request import sensorRequestDto
from model.entity.rawSensorRecords import RawSensorRecords
from model.entity.processedActivities import ProcessedActivities
from service.external.harModelService import HarModelService


class RawSensorService:
    def __init__(self, harModelService: HarModelService, data_dir: Path):
        self.harModelService = harModelService
        self.data_dir = data_dir
        self.data_dir.mkdir(parents=True, exist_ok=True)

    def process_raw_data(self, data: sensorRequestDto, authenticated_user_id: str, db: Session = None):
        """
        Process raw sensor data. Uses authenticated_user_id from JWT token instead of client-provided userId.
        
        Args:
            data: The sensor data payload
            authenticated_user_id: User ID from JWT token (trusted source)
            db: Database session
        """
        print(f"Processing data for authenticated user: {authenticated_user_id}")
        
        # Save to database if session is provided
        if db:
            saved_record = self._save_to_database(data, authenticated_user_id, db)
            print(f"Saved to database with ID: {saved_record.id}")
        
        # Send to HAR model
        response = self.harModelService.send_data_to_har_model(data)

        # Save processed activities to database if successful
        if response.get("success") and db:
            har_response = response.get("response_data")
            if har_response and har_response.data:
                self._save_processed_activities(authenticated_user_id, har_response, db)
        
        return response

    def _save_to_database(self, data: sensorRequestDto, user_id: str, db: Session) -> RawSensorRecords:
        """Save raw sensor data to database using SQLAlchemy"""
        try:
            # Create a unique client_record_id
            client_record_id = f"{user_id}_{datetime.now().strftime('%Y%m%d_%H%M%S_%f')}"
            
            # Create the database record using authenticated user_id
            db_record = RawSensorRecords(
                user_id=uuid.UUID(user_id),
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

    def _save_processed_activities(self, user_id: str, har_response, db: Session):
        """Save HAR model classification results to processed_activities table"""
        try:
            saved_count = 0
            
            for classification in har_response.data:
                # Convert millisecond timestamps to datetime objects
                ts_start = datetime.fromtimestamp(classification.ts_start / 1000.0, tz=timezone.utc)
                ts_end = datetime.fromtimestamp(classification.ts_end / 1000.0, tz=timezone.utc)
                
                # Create processed activity record using authenticated user_id
                processed_activity = ProcessedActivities(
                    user_id=uuid.UUID(user_id),
                    ts_start=ts_start,
                    ts_end=ts_end,
                    activity_label=classification.activity_label,
                    model_version=classification.model_version
                )
                
                db.add(processed_activity)
                saved_count += 1
            
            # Commit all processed activities at once
            db.commit()
            print(f"Successfully saved {saved_count} processed activities to database")
            
        except Exception as e:
            print(f"Error saving processed activities to database: {e}")
            db.rollback()
            raise

    def _save_to_file(self, data: sensorRequestDto) -> Path:
        ts = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
        filepath = self.data_dir / f"{data.userId}_{ts}.json"
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(data.model_dump(), f, indent=2)
        print(f"Saved sensor data to: {filepath}")
        return filepath
