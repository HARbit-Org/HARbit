from pydantic import BaseModel, Field

class classificationDto(BaseModel):
    model_version: str = Field(..., description="The version of the model used for classification (e.g., 'v1.0.0')")
    activity_label: str = Field(..., description="The activity predicted by the model (e.g., 'walking', 'running')")
    ts_start: int = Field(..., description="The start timestamp of the classified activity segment (e.g., 13232642723975)")
    ts_end: int = Field(..., description="The end timestamp of the classified activity segment (e.g., 13232642733975)")
