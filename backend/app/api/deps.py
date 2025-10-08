from typing import Generator
from fastapi import Depends
from sqlalchemy.orm import Session
from db.session import SessionLocal
# from app.repositories.user_repo import UserRepository
# from app.services.user_service import UserService

def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try: yield db
    finally: db.close()

# def get_user_repo(db: Session = Depends(get_db)) -> UserRepository:
#     return UserRepository(db)

# def get_user_service(repo: UserRepository = Depends(get_user_repo)) -> UserService:
#     return UserService(repo)