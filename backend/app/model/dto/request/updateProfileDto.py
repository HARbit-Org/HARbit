from pydantic import BaseModel
from typing import Optional


class UpdateProfileDto(BaseModel):
    display_name: Optional[str] = None
    preferred_email: Optional[str] = None
    phone: Optional[str] = None
    sex: Optional[str] = None
    birth_year: Optional[int] = None
    daily_step_goal: Optional[int] = None
    timezone: Optional[str] = None
    height: Optional[float] = None  # in cm
    weight: Optional[float] = None  # in kg
