from sqlalchemy import BigInteger, UUID, Column, DateTime, Text, func
from db.session import Base

class ProcessedActivities(Base):
    __tablename__ = "processed_activities"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(UUID(as_uuid=True), nullable=False)  # Removed FK temporarily
    ts_start = Column(DateTime(timezone=True), nullable=False)
    ts_end = Column(DateTime(timezone=True), nullable=False)
    activity_label = Column(Text, nullable=False)
    model_version = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    # Note: Relationships removed temporarily to avoid circular import issues

    __table_args__ = (
        {'extend_existing': True}
    )