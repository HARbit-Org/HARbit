from pydantic import BaseModel, Field
from model.dto.sensorReadingDto import sensorReadingDto

class sensorBatchDto(BaseModel):
    id: str = Field(..., description="The unique identifier of the sensor batch")
    deviceId: str = Field(..., description="The unique identifier of the device")
    timestamp: int = Field(..., description="The timestamp when the sensor batch was recorded (e.g., 13232642723975)")
    sampleCount: int = Field(..., description="The number of samples in the sensor batch")
    readings: list[sensorReadingDto] = Field(..., description="List of sensor readings in the batch")
