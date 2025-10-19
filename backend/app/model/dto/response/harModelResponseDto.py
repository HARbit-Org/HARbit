from pydantic import BaseModel, Field
from model.dto.classificationDto import classificationDto

class harModelResponseDto(BaseModel):
    data: list[classificationDto] = Field(..., description="List of classification results from the HAR model")