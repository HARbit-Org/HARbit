from pydantic import BaseModel
from typing import Optional
import uuid
from datetime import datetime

class GoogleAuthRequestDto(BaseModel):
    id_token: str
    client_type: str  # "android" or "web"