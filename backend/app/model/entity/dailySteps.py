from sqlalchemy import UUID, Column, DateTime, Integer, func
from db.session import Base

class DailySteps(Base):
    __tablename__ = "daily_steps"

    user_id = Column(UUID(as_uuid=True), primary_key=True)  # Removed FK temporarily
    date = Column(DateTime(timezone=True), primary_key=True)
    steps_total = Column(Integer, nullable=False, default=0)
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)

    # Note: Relationships removed temporarily to avoid circular import issues

    __table_args__ = (
        {'extend_existing': True}
    )