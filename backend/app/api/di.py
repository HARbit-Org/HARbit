from typing import Generator
from fastapi import Depends
from typing import Annotated
from sqlalchemy.orm import Session
from db.session import SessionLocal
from service.external.harModelService import HarModelService
from pathlib import Path
from service.rawSensorService import RawSensorService
# from app.repositories.user_repo import UserRepository
# from app.services.user_service import UserService

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
    # By default this is per-request; see “Singletons” below
    return RawSensorService(har, data_dir)

# def get_user_repo(db: Session = Depends(get_db)) -> UserRepository:
#     return UserRepository(db)

# def get_user_service(repo: UserRepository = Depends(get_user_repo)) -> UserService:
#     return UserService(repo)