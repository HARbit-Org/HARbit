from pydantic import BaseModel, Field
from typing import Optional
import uuid


class UserDto(BaseModel):
    id: uuid.UUID = Field(..., description="User unique identifier")
    email: str = Field(..., description="User email address")
    display_name: Optional[str] = Field(None, description="User display name")
    picture_url: Optional[str] = Field(None, description="User profile picture URL")
    sex: Optional[str] = Field(None, description="User sex")
    birth_year: Optional[int] = Field(None, description="User birth year")
    daily_step_goal: int = Field(10000, description="Daily step goal")
    timezone: str = Field("America/Lima", description="User timezone")

    class Config:
        from_attributes = True
        json_schema_extra = {
            "example": {
                "id": "123e4567-e89b-12d3-a456-426614174000",
                "email": "user@example.com",
                "display_name": "John Doe",
                "picture_url": "https://example.com/photo.jpg",
                "sex": "male",
                "birth_year": 1990,
                "daily_step_goal": 10000,
                "timezone": "America/Lima"
            }
        }
