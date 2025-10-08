from pydantic import BaseModel, Field
from sqlalchemy import UUID
from model.dto.sensorReadingDto import sensorReadingDto
from typing import List, Union

class rawSensorDto(BaseModel):
    userId: str = Field(..., description="The unique identifier of the user")
    accel: list[sensorReadingDto] = Field(..., description="List of accelerometer sensor readings")
    # gyro: list[sensorReadingDto] = Field(..., description="List of gyroscope sensor readings")