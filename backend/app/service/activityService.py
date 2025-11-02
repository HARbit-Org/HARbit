from datetime import datetime, timedelta, timezone
from typing import Optional, Dict, Any
import uuid
from repository.activityRepository import ActivityRepository
from model.dto.response.activityDistributionDto import (
    ActivityDistributionResponseDto,
    ActivityDistributionItemDto
)


class ActivityService:
    """
    Service for activity analysis and processing.
    
    Handles:
    - Activity distribution queries
    - Sedentary behavior analysis
    - Activity pattern analysis (future)
    - Progress tracking (future)
    """
    
    # Analysis configuration
    SEDENTARY_EVALUATION_MINUTES = 30  # Evaluate last 30 minutes
    SEDENTARY_THRESHOLD_PERCENTAGE = 83.0  # 83% sedentary activities triggers alert
    
    # Activity classifications
    SEDENTARY_ACTIVITIES = {'sit', 'type', 'eat', 'write'}  # Activities considered sedentary
    ACTIVE_ACTIVITIES = {'walk', 'workouts', 'others'}  # Activities considered active
    
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

    def analyze_sedentary_behavior(self, user_id: uuid.UUID) -> Optional[Dict[str, Any]]:
        """
        Analyze if user has been sedentary in the recent period.
        
        Sedentary activities include: Sit, Type, Eat, Write
        Active activities include: Walk, Workouts, Others
        
        This method performs the analysis and returns the results.
        It does NOT send notifications - that's handled by NotificationService.
        
        Args:
            user_id: UUID of the user to analyze
            
        Returns:
            Dict with analysis results:
            {
                'is_sedentary': bool,
                'sedentary_percentage': float,
                'sedentary_hours': float,
                'total_hours': float,
                'threshold': float,
                'evaluation_period_minutes': int,
                'has_sufficient_data': bool,
                'sedentary_breakdown': dict  # Hours per sedentary activity
            }
            Or None if no data available or insufficient data
        """
        # Define time range (last X minutes)
        end_time = datetime.now(timezone.utc)
        start_time = end_time - timedelta(minutes=self.SEDENTARY_EVALUATION_MINUTES)
        
        # Get activity distribution for the time range
        distribution = self.activity_repo.get_activity_distribution(
            user_id=user_id,
            start_time=start_time,
            end_time=end_time
        )
        
        # If no data, can't analyze
        if not distribution:
            return None
        
        # Calculate total hours of data
        total_hours = sum(activity['total_hours'] for activity in distribution)
        
        # Validate that we have at least SEDENTARY_EVALUATION_MINUTES of data
        # Allow a small margin (e.g., 90% of required time) to account for gaps
        minimum_required_hours = (self.SEDENTARY_EVALUATION_MINUTES / 60) * 0.9
        
        if total_hours < minimum_required_hours:
            # Insufficient data for reliable analysis
            print(f"⚠️ Insufficient data for user {user_id}: {total_hours:.2f}h < {minimum_required_hours:.2f}h required")
            return {
                'is_sedentary': False,
                'sedentary_percentage': 0.0,
                'sedentary_hours': 0.0,
                'total_hours': total_hours,
                'threshold': self.SEDENTARY_THRESHOLD_PERCENTAGE,
                'evaluation_period_minutes': self.SEDENTARY_EVALUATION_MINUTES,
                'has_sufficient_data': False,
                'minimum_required_hours': minimum_required_hours,
                'sedentary_breakdown': {},
                'timestamp': end_time.isoformat()
            }
        
        # Calculate total sedentary time (sum of all sedentary activities)
        sedentary_hours = 0.0
        sedentary_breakdown = {}
        
        for activity in distribution:
            activity_label = activity['activity_label'].lower()
            
            # Check if this activity is sedentary
            if activity_label in self.SEDENTARY_ACTIVITIES:
                activity_hours = activity['total_hours']
                sedentary_hours += activity_hours
                sedentary_breakdown[activity_label] = activity_hours
        
        # Calculate sedentary percentage
        sedentary_percentage = (sedentary_hours / total_hours * 100) if total_hours > 0 else 0.0
        
        # Determine if sedentary threshold is exceeded
        is_sedentary = sedentary_percentage >= self.SEDENTARY_THRESHOLD_PERCENTAGE
        
        return {
            'is_sedentary': is_sedentary,
            'sedentary_percentage': round(sedentary_percentage, 2),
            'sedentary_hours': round(sedentary_hours, 2),
            'total_hours': round(total_hours, 2),
            'threshold': self.SEDENTARY_THRESHOLD_PERCENTAGE,
            'evaluation_period_minutes': self.SEDENTARY_EVALUATION_MINUTES,
            'has_sufficient_data': True,
            'sedentary_breakdown': sedentary_breakdown,
            'timestamp': end_time.isoformat()
        }

    def analyze_activity_patterns(self, user_id: uuid.UUID) -> Optional[Dict[str, Any]]:
        """
        Analyze user's activity patterns over time.
        
        Future implementation:
        - Daily activity trends
        - Peak activity hours
        - Activity consistency
        - Week vs weekend comparison
        
        Args:
            user_id: UUID of the user to analyze
            
        Returns:
            Dict with pattern analysis results
        """
        # TODO: Implement activity pattern analysis
        raise NotImplementedError("Activity pattern analysis not yet implemented")
