from datetime import datetime, timezone
from typing import Optional
import uuid
from repository.activityRepository import ActivityRepository
from model.dto.response.activityDistributionDto import (
    ActivityDistributionResponseDto,
    ActivityDistributionItemDto
)


class ActivityService:
    def __init__(self, activity_repo: ActivityRepository):
        self.activity_repo = activity_repo
    
    def get_activity_distribution(
        self,
        user_id: uuid.UUID,
        start_time: datetime,
        end_time: datetime
    ) -> ActivityDistributionResponseDto:
        """
        Get activity distribution for a user within a time range.
        
        Args:
            user_id: UUID of the user
            start_time: Start of time range (datetime with timezone)
            end_time: End of time range (datetime with timezone)
            
        Returns:
            ActivityDistributionResponseDto with activity breakdown
        """
        # Get distribution from repository
        distribution_data = self.activity_repo.get_activity_distribution(
            user_id=user_id,
            start_time=start_time,
            end_time=end_time
        )
        
        # Convert to DTOs
        activities = [
            ActivityDistributionItemDto(**item)
            for item in distribution_data
        ]
        
        # Calculate total hours
        total_hours = sum(activity.total_hours for activity in activities)
        
        return ActivityDistributionResponseDto(
            user_id=str(user_id),
            start_time=start_time.isoformat(),
            end_time=end_time.isoformat(),
            activities=activities,
            total_hours=round(total_hours, 2)
        )
