from pydantic import BaseModel, Field


class ActivityDistributionItemDto(BaseModel):
    """Single activity distribution item"""
    activity_label: str = Field(..., description="Activity type label")
    total_seconds: float = Field(..., description="Total seconds spent on this activity")
    total_minutes: float = Field(..., description="Total minutes spent on this activity")
    total_hours: float = Field(..., description="Total hours spent on this activity")
    percentage: float = Field(..., description="Percentage of time spent on this activity")


class ActivityDistributionResponseDto(BaseModel):
    """Response containing activity distribution for a time range"""
    user_id: str = Field(..., description="User ID")
    start_time: str = Field(..., description="Start of time range (ISO format)")
    end_time: str = Field(..., description="End of time range (ISO format)")
    activities: list[ActivityDistributionItemDto] = Field(..., description="List of activities with their distribution")
    total_hours: float = Field(..., description="Total hours of tracked activity")
