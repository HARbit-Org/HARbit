from fastapi import APIRouter, Depends, HTTPException
from typing import Annotated
from api.di import get_raw_sensor_service, get_activity_service, get_notification_service
from api.v1.authController import get_current_user
from sqlalchemy.orm import Session

from model.dto.request import sensorRequestDto
from model.dto.response import sensorResponseDto
from service.rawSensorService import RawSensorService
from service.activityService import ActivityService
from service.notificationService import NotificationService
from api.di import get_db
import uuid

router = APIRouter(prefix="/sensor-data", tags=["sensor-data"])

@router.post("", response_model=sensorResponseDto.sensorResponseDto, status_code=201)
def receive_raw_sensor_data(
    payload: sensorRequestDto.sensorRequestDto,
    svc: Annotated[RawSensorService, Depends(get_raw_sensor_service)],
    activity_svc: Annotated[ActivityService, Depends(get_activity_service)],
    notification_svc: Annotated[NotificationService, Depends(get_notification_service)],
    db: Session = Depends(get_db),
    current_user: dict = Depends(get_current_user)
    ):
    try:
        # Use authenticated user ID instead of client-provided userId
        user_id = uuid.UUID(current_user['user_id'])
        
        # Process raw sensor data
        svc.process_raw_data(payload, current_user['user_id'], db) # TODO - ASYNC
        
        # Analyze sedentary behavior after processing batch
        try:
            # Step 1: Analyze activity (ActivityService)
            analysis_result = activity_svc.analyze_sedentary_behavior(user_id) # TODO - ASYNC
            
            if analysis_result:
                print(f"📊 Sedentary analysis for user {user_id}: {analysis_result}")
                
                # Step 2: Send notification if needed (NotificationService)
                notification_result = notification_svc.send_sedentary_alert(
                    user_id=user_id,
                    analysis_result=analysis_result
                )
                
                if notification_result:
                    print(f"🔔 Notification result: {notification_result}")
                    
                    # Log different scenarios
                    if notification_result.get('notification_sent'):
                        print(f"✅ Sedentary alert sent to user {user_id}")
                    elif notification_result.get('reason') == 'insufficient_data':
                        print(f"⚠️ Insufficient data: {notification_result.get('total_hours', 0):.2f}h < "
                              f"{notification_result.get('minimum_required_hours', 0):.2f}h required")
                    elif notification_result.get('reason') == 'cooldown_active':
                        print(f"⏳ Cooldown active for user {user_id}")
                    elif notification_result.get('reason') == 'not_sedentary':
                        print(f"✅ User {user_id} is active (not sedentary)")
            else:
                print(f"ℹ️ No activity data available for user {user_id}")
        except Exception as e:
            # Don't fail the whole request if analysis/notification fails
            print(f"⚠️ Error in sedentary processing: {e}")
        
        return sensorResponseDto.sensorResponseDto(status="success")
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))