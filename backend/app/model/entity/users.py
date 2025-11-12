import uuid
from sqlalchemy import UUID, Column, DateTime, Integer, Text, Float, func
from db.session import Base

class Users(Base):
    __tablename__ = "users"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    email = Column(Text, unique=True, nullable=False)
    preferred_email = Column(Text, nullable=True)
    display_name = Column(Text, nullable=True)
    phone = Column(Text, nullable=True)
    picture_url = Column(Text, nullable=True)
    sex = Column(Text, nullable=True)
    birth_year = Column(Integer, nullable=True)
    height = Column(Float, nullable=True)
    weight = Column(Float, nullable=True)
    daily_step_goal = Column(Integer, nullable=True)
    timezone = Column(Text, nullable=False, default='America/Lima')
    fcm_token = Column(Text, nullable=True)
    fcm_updated_at = Column(DateTime(timezone=True), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now(), nullable=False)

    # Note: Relationships removed temporarily to avoid circular import issues
    # You can add them back later once all models are properly imported

    __table_args__ = (
        {'extend_existing': True}
    )