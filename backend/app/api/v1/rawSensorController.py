from fastapi import APIRouter, Depends, HTTPException
from typing import Annotated
from api.di import get_raw_sensor_service
from api.v1.authController import get_current_user
from sqlalchemy.orm import Session

from model.dto.request import sensorRequestDto
from model.dto.response import sensorResponseDto
from service.rawSensorService import RawSensorService
from api.di import get_db

router = APIRouter(prefix="/sensor-data", tags=["sensor-data"])

@router.post("", response_model=sensorResponseDto.sensorResponseDto, status_code=201)
def receive_raw_sensor_data(
    payload: sensorRequestDto.sensorRequestDto,
    svc: Annotated[RawSensorService, Depends(get_raw_sensor_service)],
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user)
    ):
    try:
        # Use authenticated user ID instead of client-provided userId
        svc.process_raw_data(payload, current_user['user_id'], db)
        return sensorResponseDto.sensorResponseDto(status="success")
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))