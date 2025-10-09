from pydantic import BaseModel, Field
from model.dto.sensorBatchDto import sensorBatchDto

class sensorRequestDto(BaseModel):
    userId: str = Field(..., description="The unique identifier of the user")
    batches: list[sensorBatchDto]