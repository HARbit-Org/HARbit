from pydantic import BaseModel, Field


class UpdateFcmTokenRequestDto(BaseModel):
    """Request DTO for updating FCM token"""
    fcm_token: str = Field(..., alias="fcmToken", description="Firebase Cloud Messaging token")

    class Config:
        populate_by_name = True
        json_schema_extra = {
            "example": {
                "fcmToken": "dQw4w9WgXcQ:APA91bF..."
            }
        }
