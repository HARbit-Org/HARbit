from pydantic import BaseModel


class UpdateFcmTokenResponseDto(BaseModel):
    """Response DTO for FCM token update"""
    status: str
    message: str

    class Config:
        json_schema_extra = {
            "example": {
                "status": "success",
                "message": "FCM token updated successfully"
            }
        }
