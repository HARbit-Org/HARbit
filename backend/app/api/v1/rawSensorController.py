from fastapi import APIRouter, Depends, HTTPException

from model.dto import rawSensorDto
from service.rawSensorService import RawSensorService

router = APIRouter(prefix="/raw-sensors", tags=["raw-sensors"])

@router.post("", response_model=rawSensorDto.rawSensorDto, status_code=201)
def receive_raw_sensor_data(body: rawSensorDto.rawSensorDto, svc: RawSensorService = Depends(RawSensorService)):
    try:
        svc.process_raw_data(body)
        return body
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))