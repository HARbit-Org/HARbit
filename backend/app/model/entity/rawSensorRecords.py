from sqlalchemy import BigInteger, UUID, Column, DateTime, Integer, Text, func, ForeignKey, JSON
from db.session import Base

class RawSensorRecords(Base):
    __tablename__ = "raw_sensor_records"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(UUID(as_uuid=True), nullable=False)  # Temporarily removed FK constraint
    ts = Column(DateTime(timezone=True), nullable=False)
    duration_ms = Column(Integer, nullable=True)
    client_record_id = Column(Text, nullable=False)
    payload = Column(JSON, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    # Note: Relationships removed temporarily to avoid circular import issues
    # You can add them back later once all models are properly imported

    __table_args__ = (
        {'extend_existing': True}
    )