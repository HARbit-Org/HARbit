from fastapi import APIRouter, Depends, HTTPException
from typing import Annotated
from api.di import get_raw_sensor_service
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
    db: Session = Depends(get_db)
    ):
    try:
        svc.process_raw_data(payload, db)  # Pass database session
        return sensorResponseDto.sensorResponseDto(status="success")
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))