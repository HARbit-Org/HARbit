from sqlalchemy.orm import Session
from typing import Optional
from datetime import datetime
import uuid
from model.entity.userSessions import UserSessions


class SessionRepository:
    def __init__(self, db: Session):
        self.db = db

    def create_session(
        self,
        user_id: uuid.UUID,
        access_token: str,
        refresh_token: Optional[str],
        expires_at: datetime,
        user_agent: Optional[str] = None,
        ip_address: Optional[str] = None
    ) -> UserSessions:
        """Create a new user session"""
        session = UserSessions(
            user_id=user_id,
            access_token=access_token,
            refresh_token=refresh_token,
            expires_at=expires_at,
            user_agent=user_agent,
            ip_address=ip_address
        )
        self.db.add(session)
        self.db.commit()
        self.db.refresh(session)
        return session

    def find_by_access_token(self, access_token: str) -> Optional[UserSessions]:
        """Find session by access token"""
        return (
            self.db.query(UserSessions)
            .filter(UserSessions.access_token == access_token)
            .first()
        )

    def find_by_refresh_token(self, refresh_token: str) -> Optional[UserSessions]:
        """Find session by refresh token"""
        return (
            self.db.query(UserSessions)
            .filter(UserSessions.refresh_token == refresh_token)
            .first()
        )

    def find_valid_session(self, access_token: str) -> Optional[UserSessions]:
        """Find valid (not expired) session by access token"""
        now = datetime.utcnow()
        return (
            self.db.query(UserSessions)
            .filter(
                UserSessions.access_token == access_token,
                UserSessions.expires_at > now
            )
            .first()
        )

    def update_last_used(self, session_id: uuid.UUID) -> None:
        """Update session last used timestamp"""
        session = self.db.query(UserSessions).filter(UserSessions.id == session_id).first()
        if session:
            session.last_used_at = datetime.utcnow()
            self.db.commit()

    def delete_session(self, session_id: uuid.UUID) -> bool:
        """Delete a session (logout)"""
        session = self.db.query(UserSessions).filter(UserSessions.id == session_id).first()
        if session:
            self.db.delete(session)
            self.db.commit()
            return True
        return False

    def delete_user_sessions(self, user_id: uuid.UUID) -> int:
        """Delete all sessions for a user"""
        deleted = (
            self.db.query(UserSessions)
            .filter(UserSessions.user_id == user_id)
            .delete()
        )
        self.db.commit()
        return deleted

    def delete_expired_sessions(self) -> int:
        """Delete all expired sessions"""
        now = datetime.utcnow()
        deleted = (
            self.db.query(UserSessions)
            .filter(UserSessions.expires_at < now)
            .delete()
        )
        self.db.commit()
        return deleted
