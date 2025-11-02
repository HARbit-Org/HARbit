from typing import Generator
from fastapi import Depends
from typing import Annotated
from sqlalchemy.orm import Session
from db.session import SessionLocal
from service.external.harModelService import HarModelService
from pathlib import Path
from service.rawSensorService import RawSensorService
from repository.userRepository import UserRepository
from repository.sessionRepository import SessionRepository
from repository.activityRepository import ActivityRepository
from repository.notificationRepository import NotificationRepository
from repository.progressInsightsRepository import ProgressInsightsRepository
from service.authService import AuthService
from service.userService import UserService
from service.activityService import ActivityService
from service.notificationService import NotificationService
from service.progressService import ProgressService

def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try: 
        yield db
    finally: 
        db.close()

def get_data_dir() -> Path:
    # Could read from env/config in real life
    return Path("sensor_data")

def get_har_model_service() -> HarModelService:
    return HarModelService()

def get_raw_sensor_service(
    har: Annotated[HarModelService, Depends(get_har_model_service)],
    data_dir: Annotated[Path, Depends(get_data_dir)],
) -> RawSensorService:
    # By default this is per-request; see "Singletons" below
    return RawSensorService(har, data_dir)

def get_user_repository(db: Annotated[Session, Depends(get_db)]) -> UserRepository:
    return UserRepository(db)

def get_session_repository(db: Annotated[Session, Depends(get_db)]) -> SessionRepository:
    return SessionRepository(db)

def get_auth_service(
    user_repo: Annotated[UserRepository, Depends(get_user_repository)],
    session_repo: Annotated[SessionRepository, Depends(get_session_repository)]
) -> AuthService:
    return AuthService(user_repo, session_repo)

def get_user_service(
    user_repo: Annotated[UserRepository, Depends(get_user_repository)]
) -> UserService:
    return UserService(user_repo)

def get_activity_repository(db: Annotated[Session, Depends(get_db)]) -> ActivityRepository:
    return ActivityRepository(db)

def get_activity_service(
    activity_repo: Annotated[ActivityRepository, Depends(get_activity_repository)]
) -> ActivityService:
    return ActivityService(activity_repo)

def get_notification_repository(db: Annotated[Session, Depends(get_db)]) -> NotificationRepository:
    return NotificationRepository(db)

def get_notification_service(
    notification_repo: Annotated[NotificationRepository, Depends(get_notification_repository)],
    user_repo: Annotated[UserRepository, Depends(get_user_repository)]
) -> NotificationService:
    return NotificationService(notification_repo, user_repo)

def get_progress_insights_repository(db: Annotated[Session, Depends(get_db)]) -> ProgressInsightsRepository:
    return ProgressInsightsRepository(db)

def get_progress_service(
    progress_insights_repo: Annotated[ProgressInsightsRepository, Depends(get_progress_insights_repository)],
    activity_repo: Annotated[ActivityRepository, Depends(get_activity_repository)],
    user_repo: Annotated[UserRepository, Depends(get_user_repository)],
    notification_service: Annotated[NotificationService, Depends(get_notification_service)]
) -> ProgressService:
    return ProgressService(progress_insights_repo, activity_repo, user_repo, notification_service)
