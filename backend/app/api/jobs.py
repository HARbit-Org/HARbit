"""
Scheduled Jobs for HARbit Backend

This module contains all scheduled/cron jobs that run automatically.
Uses fastapi_crons for scheduling.
"""

from datetime import datetime, timezone
from db.session import SessionLocal
from repository.progressInsightsRepository import ProgressInsightsRepository
from repository.activityRepository import ActivityRepository
from repository.userRepository import UserRepository
from repository.notificationRepository import NotificationRepository
from service.progressService import ProgressService
from service.notificationService import NotificationService


async def weekly_progress_cron():
    """
    Automated weekly progress calculation.
    
    Runs every Sunday at 8:00 PM to:
    1. Calculate weekly progress for all users
    2. Generate progress insights
    3. Send push notifications
    
    Scheduled via cron expression: "0 20 * * 0"
    - Minute: 0
    - Hour: 20 (8:00 PM)
    - Day of month: * (any)
    - Month: * (any)
    - Day of week: 0 (Sunday)
    """
    print("=" * 80)
    print(f"🚀 Starting Automated Weekly Progress Job - {datetime.now(timezone.utc).isoformat()}")
    print("=" * 80)
    
    # Create a new database session for this job
    db = SessionLocal()
    
    try:
        # Manually instantiate all dependencies
        # (we can't use FastAPI's Depends() in a cron job)
        progress_insights_repo = ProgressInsightsRepository(db)
        activity_repo = ActivityRepository(db)
        user_repo = UserRepository(db)
        notification_repo = NotificationRepository(db)
        
        # Create notification service
        notification_service = NotificationService(notification_repo, user_repo)
        
        # Create progress service with all dependencies
        progress_service = ProgressService(
            progress_insights_repo=progress_insights_repo,
            activity_repo=activity_repo,
            user_repo=user_repo,
            notification_service=notification_service
        )
        
        # Execute the weekly progress calculation
        results = progress_service.calculate_weekly_progress_for_all_users()
        
        print("=" * 80)
        print(f"✅ Automated Weekly Progress Job completed - {datetime.now(timezone.utc).isoformat()}")
        print(f"   Processed: {len(results)} users")
        successful = len([r for r in results if r.get('status') == 'success'])
        failed = len([r for r in results if r.get('status') == 'failed'])
        print(f"   Successful: {successful} | Failed: {failed}")
        print("=" * 80)
        
    except Exception as e:
        print(f"\n❌ CRITICAL ERROR in automated weekly progress job: {e}")
        import traceback
        traceback.print_exc()
        
    finally:
        # Always close the database session
        db.close()
