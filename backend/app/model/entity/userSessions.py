import uuid
from sqlalchemy import Column, String, ForeignKey, DateTime, func
from db.session import Base
from sqlalchemy.dialects.postgresql import UUID, INET


class UserSessions(Base):
    __tablename__ = "user_sessions"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    access_token = Column(String, nullable=False)
    refresh_token = Column(String)
    expires_at = Column(DateTime(timezone=True), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    last_used_at = Column(DateTime(timezone=True), server_default=func.now())
    user_agent = Column(String)
    ip_address = Column(INET)  # ✅ Changed from String to INET

    __table_args__ = (
        {'extend_existing': True}
    )