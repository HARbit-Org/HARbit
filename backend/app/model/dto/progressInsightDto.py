from pydantic import BaseModel, Field

class ProgressInsightDto(BaseModel):
    id: str = Field(..., description="The unique identifier of the progress insight")
    userId: str = Field(..., description="The unique identifier of the user")
    type: str = Field(..., description="The type of progress insight (progress, improvement_opportunity, other)")
    category: str = Field(..., description="The category of the progress insight (e.g., activity, sedentary)")
    message_title: str = Field(..., description="The title of the progress insight message")
    message_body: str = Field(..., description="The body content of the progress insight message")
    created_at: str = Field(..., description="The timestamp when the progress insight was created")