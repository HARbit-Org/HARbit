from fastapi import APIRouter, Depends, HTTPException
from typing import Annotated
from datetime import datetime, timezone
from api.di import (
    get_progress_service,
    get_notification_service,
    get_progress_insights_repository,
    get_db
)
# from api.v1.authController import get_current_user
from service.progressService import ProgressService
from service.notificationService import NotificationService
from repository.progressInsightsRepository import ProgressInsightsRepository
from sqlalchemy.orm import Session

router = APIRouter(prefix="/jobs", tags=["jobs"])


@router.post("/weekly-progress", status_code=200)
def run_weekly_progress_job(
    progress_service: Annotated[ProgressService, Depends(get_progress_service)],
    notification_service: Annotated[NotificationService, Depends(get_notification_service)],
    progress_insights_repo: Annotated[ProgressInsightsRepository, Depends(get_progress_insights_repository)],
    db: Session = Depends(get_db),
    # current_user: dict = Depends(get_current_user)
):
    """
    Execute the weekly progress job manually.
    
    This endpoint:
    1. Calculates weekly progress for all users
    2. Creates progress insights in the database
    3. Sends push notifications to users
    
    Normally runs automatically every Sunday at 8:00 PM via cron job.
    This endpoint allows manual execution for testing or emergency runs.
    
    Returns:
        Summary of the job execution with counts and results
    """
    print("=" * 80)
    print(f"🚀 Starting Weekly Progress Job (Manual) - {datetime.now(timezone.utc).isoformat()}")
    # print(f"   Triggered by user: {current_user.get('user_id')}")
    print("=" * 80)
    
    try:
        # Step 1: Calculate weekly progress for all users
        print("\n📊 Step 1: Calculating weekly progress for all users...")
        results = progress_service.calculate_weekly_progress_for_all_users()
        
        print("=" * 80)
        print(f"✅ Weekly Progress Job completed - {datetime.now(timezone.utc).isoformat()}")
        print("=" * 80)
        
        # Return summary
        return {
            'status': 'completed',
            'timestamp': datetime.now(timezone.utc).isoformat()
        }
        
    except Exception as e:
        print(f"\n❌ CRITICAL ERROR in weekly progress job: {e}")
        import traceback
        traceback.print_exc()
        
        raise HTTPException(
            status_code=500,
            detail={
                'error': 'Weekly progress job failed',
                'message': str(e),
                'timestamp': datetime.now(timezone.utc).isoformat()
            }
        )
