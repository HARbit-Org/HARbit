from pydantic import BaseModel, Field

class sensorResponseDto(BaseModel):
    status: str = Field(..., description="Response message indicating the result of the operation")