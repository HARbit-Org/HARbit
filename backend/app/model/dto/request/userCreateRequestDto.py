from pydantic import BaseModel
from typing import Optional
import uuid
from datetime import datetime

class UserCreateRequestDto(BaseModel):
    email: str
    preferred_email: Optional[str] = None
    display_name: Optional[str] = None
    phone: Optional[str] = None
    picture_url: Optional[str] = None
    sex: Optional[str] = None
    birth_year: Optional[int] = None
    daily_step_goal: Optional[int] = 10000
    timezone: str = "America/Lima"