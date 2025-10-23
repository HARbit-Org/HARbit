from fastapi import APIRouter, Depends, HTTPException, Query
from typing import Annotated
from datetime import datetime, time, timezone, timedelta
import uuid
from service.activityService import ActivityService
from api.di import get_activity_service
from api.v1.authController import get_current_user
from model.dto.response.activityDistributionDto import ActivityDistributionResponseDto

router = APIRouter(prefix="/activities", tags=["activities"])


@router.get("/distribution", response_model=ActivityDistributionResponseDto)
async def get_activity_distribution(
    date_start: str = Query(..., description="Start date in YYYY-MM-DD format (inclusive). For single day, use same value as date_end."),
    date_end: str = Query(..., description="End date in YYYY-MM-DD format (inclusive). For single day, use same value as date_start."),
    timezone_offset: int = Query(..., description="Timezone offset in minutes from UTC (e.g., -300 for UTC-5, 0 for UTC, 60 for UTC+1)"),
    current_user: dict = Depends(get_current_user),
    activity_service: Annotated[ActivityService, Depends(get_activity_service)] = None
):
    """
    Get activity distribution for a date range in the user's timezone.
    
    Returns breakdown of activities by label with total time and percentage.
    Each data point represents ~2.5 seconds of activity (50% overlap).
    
    The dates are interpreted in the user's timezone (specified by timezone_offset),
    then converted to UTC for querying the database.
    
    Usage:
    - Single day: ?date_start=2025-10-22&date_end=2025-10-22&timezone_offset=-300
    - Date range: ?date_start=2025-10-20&date_end=2025-10-22&timezone_offset=-300
    """
    user_id = uuid.UUID(current_user['user_id'])
    
    try:
        # Parse dates
        start_date = datetime.strptime(date_start, "%Y-%m-%d").date()
        end_date = datetime.strptime(date_end, "%Y-%m-%d").date()
        
        # Validate date range
        if start_date > end_date:
            raise HTTPException(
                status_code=400,
                detail="date_start must be before or equal to date_end"
            )
        
        # Create timedelta for user's timezone offset
        user_tz_offset = timedelta(minutes=timezone_offset)
        
        # Create datetime range in user's timezone, then convert to UTC
        # Start of day in user's timezone (e.g., 2025-10-22 00:00:00 in Peru)
        start_time_local = datetime.combine(start_date, time.min)
        # Convert to UTC by subtracting the offset
        start_time = start_time_local - user_tz_offset
        start_time = start_time.replace(tzinfo=timezone.utc)
        
        # End of day in user's timezone (e.g., 2025-10-22 23:59:59 in Peru)
        end_time_local = datetime.combine(end_date, time.max)
        # Convert to UTC by subtracting the offset
        end_time = end_time_local - user_tz_offset
        end_time = end_time.replace(tzinfo=timezone.utc)
        
        # Get distribution
        distribution = activity_service.get_activity_distribution(
            user_id=user_id,
            start_time=start_time,
            end_time=end_time
        )
        
        return distribution
        
    except ValueError as e:
        raise HTTPException(
            status_code=400,
            detail=f"Invalid date format. Use YYYY-MM-DD. Error: {str(e)}"
        )
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"Failed to get activity distribution: {str(e)}"
        )
