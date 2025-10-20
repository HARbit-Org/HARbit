from sqlalchemy import BigInteger, UUID, Column, DateTime, SmallInteger, func
from db.session import Base

class HeartRateReadings(Base):
    __tablename__ = "heart_rate_readings"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(UUID(as_uuid=True), nullable=False)  # Removed FK temporarily
    ts = Column(DateTime(timezone=True), nullable=False)
    bpm = Column(SmallInteger, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    # Note: Relationships removed temporarily to avoid circular import issues

    __table_args__ = (
        {'extend_existing': True}
    )