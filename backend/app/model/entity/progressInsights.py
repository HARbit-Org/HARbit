import uuid
from sqlalchemy import UUID, Column, Date, DateTime, Text, Numeric, func
from db.session import Base

class ProgressInsights(Base):
    __tablename__ = "progress_insights"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(UUID(as_uuid=True), nullable=False)  # Removed FK temporarily
    type = Column(Text, nullable=False)
    category = Column(Text, nullable=False)
    period_type = Column(Text, nullable=False)
    period_start = Column(Date, nullable=False)
    comparison_start = Column(Date, nullable=False)
    comparison_value = Column(Numeric(12, 3), nullable=False)
    current_value = Column(Numeric(12, 3), nullable=False)
    delta_value = Column(Numeric(12, 3), nullable=False)
    delta_pct = Column(Numeric(7, 4), nullable=False)
    message_title = Column(Text, nullable=False)
    message_body = Column(Text, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    # Note: Relationships removed temporarily to avoid circular import issues

    __table_args__ = (
        {'extend_existing': True}
    )