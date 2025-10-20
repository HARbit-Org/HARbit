import uuid
from sqlalchemy import UUID, Column, DateTime, Text, func
from db.session import Base

class UserProviders(Base):
    __tablename__ = "user_providers"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(UUID(as_uuid=True), nullable=False)  # Removed FK temporarily
    provider = Column(Text, nullable=False)
    provider_user_id = Column(Text, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now(), nullable=False)

    # Note: Relationships removed temporarily to avoid circular import issues

    __table_args__ = (
        {'extend_existing': True}
    )