from sqlalchemy.orm import Session
from typing import Optional
import uuid
from model.entity.users import Users
from model.entity.userProviders import UserProviders


class UserRepository:
    def __init__(self, db: Session):
        self.db = db

    def find_by_email(self, email: str) -> Optional[Users]:
        """Find user by email address"""
        return self.db.query(Users).filter(Users.email == email).first()

    def find_by_id(self, user_id: uuid.UUID) -> Optional[Users]:
        """Find user by ID"""
        return self.db.query(Users).filter(Users.id == user_id).first()

    def find_by_provider(self, provider: str, provider_user_id: str) -> Optional[Users]:
        """Find user by OAuth provider and provider user ID"""
        user_provider = (
            self.db.query(UserProviders)
            .filter(
                UserProviders.provider == provider,
                UserProviders.provider_user_id == provider_user_id
            )
            .first()
        )

        if user_provider:
            return self.find_by_id(user_provider.user_id)
        return None

    def create_user(
        self,
        email: str,
        display_name: Optional[str] = None,
        picture_url: Optional[str] = None,
        sex: Optional[str] = None,
        birth_year: Optional[int] = None,
        daily_step_goal: int = 10000,
        timezone: str = "America/Lima"
    ) -> Users:
        """Create a new user"""
        user = Users(
            email=email,
            display_name=display_name,
            picture_url=picture_url,
            sex=sex,
            birth_year=birth_year,
            daily_step_goal=daily_step_goal,
            timezone=timezone
        )
        self.db.add(user)
        self.db.commit()
        self.db.refresh(user)
        return user

    def update_user(
        self,
        user_id: uuid.UUID,
        display_name: Optional[str] = None,
        picture_url: Optional[str] = None,
        sex: Optional[str] = None,
        birth_year: Optional[int] = None,
        daily_step_goal: Optional[int] = None,
        timezone: Optional[str] = None
    ) -> Optional[Users]:
        """Update user information"""
        user = self.find_by_id(user_id)
        if not user:
            return None

        if display_name is not None:
            user.display_name = display_name
        if picture_url is not None:
            user.picture_url = picture_url
        if sex is not None:
            user.sex = sex
        if birth_year is not None:
            user.birth_year = birth_year
        if daily_step_goal is not None:
            user.daily_step_goal = daily_step_goal
        if timezone is not None:
            user.timezone = timezone

        self.db.commit()
        self.db.refresh(user)
        return user

    def create_user_provider(
        self,
        user_id: uuid.UUID,
        provider: str,
        provider_user_id: str
    ) -> UserProviders:
        """Link user to OAuth provider"""
        user_provider = UserProviders(
            user_id=user_id,
            provider=provider,
            provider_user_id=provider_user_id
        )
        self.db.add(user_provider)
        self.db.commit()
        self.db.refresh(user_provider)
        return user_provider

    def get_user_providers(self, user_id: uuid.UUID) -> list[UserProviders]:
        """Get all OAuth providers for a user"""
        return (
            self.db.query(UserProviders)
            .filter(UserProviders.user_id == user_id)
            .all()
        )
