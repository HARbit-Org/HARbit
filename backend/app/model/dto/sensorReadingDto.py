from pydantic import BaseModel, Field

class sensorReadingDto(BaseModel):
    timestamp: int = Field(..., description="The timestamp of the sensor reading (e.g., 13232642723975)")
    sensorType: int = Field(..., description="The type of sensor (e.g., 1 for accelerometer, 2 for gyroscope)")
    x: float = Field(..., description="The x-axis reading from the sensor (e.g., 0.123)")
    y: float = Field(..., description="The y-axis reading from the sensor (e.g., 0.456)")
    z: float = Field(..., description="The z-axis reading from the sensor (e.g., 0.789)")
    