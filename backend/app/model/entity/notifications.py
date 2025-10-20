from sqlalchemy import BigInteger, UUID, Column, DateTime, Text, Boolean, func, JSON
from db.session import Base

class Notifications(Base):
    __tablename__ = "notifications"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(UUID(as_uuid=True), nullable=False)  # Removed FK temporarily
    type = Column(Text, nullable=False)
    ts = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    payload = Column(JSON, nullable=True)
    delivered_push = Column(Boolean, default=False, nullable=False)
    delivered_at = Column(DateTime(timezone=True), nullable=True)
    read_at = Column(DateTime(timezone=True), nullable=True)

    # Note: Relationships removed temporarily to avoid circular import issues

    __table_args__ = (
        {'extend_existing': True}
    )