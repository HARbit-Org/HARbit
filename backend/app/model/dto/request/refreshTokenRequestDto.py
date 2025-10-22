from pydantic import BaseModel, Field


class RefreshTokenRequestDto(BaseModel):
    refresh_token: str = Field(..., description="Refresh token for getting new access token")

    class Config:
        json_schema_extra = {
            "example": {
                "refresh_token": "abc123xyz456..."
            }
        }
