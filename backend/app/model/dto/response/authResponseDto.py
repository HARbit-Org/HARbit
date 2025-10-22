from pydantic import BaseModel
from typing import Optional
import uuid
from datetime import datetime

class AuthResponseDto(BaseModel):
    access_token: str
    refresh_token: Optional[str] = None
    token_type: str = "Bearer"
    user: dict
    expires_in: int
    is_profile_complete: bool  # Indicates if user has completed their profile