from fastapi import APIRouter, Depends, HTTPException
from typing import Annotated
import uuid
from service.userService import UserService
from api.di import get_user_service
from api.v1.authController import get_current_user
from model.dto.request.updateProfileDto import UpdateProfileDto
from model.dto.request.updateFcmTokenRequestDto import UpdateFcmTokenRequestDto
from model.dto.response.userDto import UserDto
from model.dto.response.updateFcmTokenResponseDto import UpdateFcmTokenResponseDto

router = APIRouter(prefix="/users", tags=["users"])


@router.get("/me", response_model=UserDto)
async def get_current_user_profile(
    current_user: dict = Depends(get_current_user),
    user_service: Annotated[UserService, Depends(get_user_service)] = None
):
    """Get current authenticated user's profile"""
    user_id = uuid.UUID(current_user['user_id'])
    user = user_service.get_user_by_id(user_id)
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    return UserDto(
        id=user.id,
        email=user.email,
        display_name=user.display_name,
        preferred_email=user.preferred_email,
        phone=user.phone,
        height=user.height,
        weight=user.weight,
        picture_url=user.picture_url,
        sex=user.sex,
        birth_year=user.birth_year,
        daily_step_goal=user.daily_step_goal or 10000,
        timezone=user.timezone or "America/Lima"
    )


@router.patch("/me", response_model=UserDto)
async def update_profile(
    profile_data: UpdateProfileDto,
    current_user: dict = Depends(get_current_user),
    user_service: Annotated[UserService, Depends(get_user_service)] = None
):
    """Update current user's profile"""
    user_id = uuid.UUID(current_user['user_id'])
    
    try:
        print(f"DEBUG: Updating profile for user {user_id}")
        print(f"DEBUG: Profile data: {profile_data}")
        
        updated_user = user_service.update_profile(user_id, profile_data)
        
        print(f"DEBUG: Profile updated successfully")
        
        return UserDto(
            id=updated_user.id,
            email=updated_user.email,
            display_name=updated_user.display_name,
            preferred_email=updated_user.preferred_email,
            phone=updated_user.phone,
            height=updated_user.height,
            weight=updated_user.weight,
            picture_url=updated_user.picture_url,
            sex=updated_user.sex,
            birth_year=updated_user.birth_year,
            daily_step_goal=updated_user.daily_step_goal or 10000,
            timezone=updated_user.timezone or "America/Lima"
        )
    except Exception as e:
        print(f"ERROR updating profile: {str(e)}")
        import traceback
        print(f"Traceback: {traceback.format_exc()}")
        raise HTTPException(status_code=400, detail=str(e))


@router.patch("/fcm-token", response_model=UpdateFcmTokenResponseDto)
async def update_fcm_token(
    fcm_data: UpdateFcmTokenRequestDto,
    current_user: dict = Depends(get_current_user),
    user_service: Annotated[UserService, Depends(get_user_service)] = None
):
    """Update current user's FCM token for push notifications"""
    user_id = uuid.UUID(current_user['user_id'])
    
    try:
        print(f"DEBUG: Updating FCM token for user {user_id}")
        print(f"DEBUG: FCM token (first 20 chars): {fcm_data.fcm_token[:20]}...")
        
        user_service.update_fcm_token(user_id, fcm_data.fcm_token)
        
        print(f"DEBUG: FCM token updated successfully")
        
        return UpdateFcmTokenResponseDto(
            status="success",
            message="FCM token updated successfully"
        )
    except Exception as e:
        print(f"ERROR updating FCM token: {str(e)}")
        import traceback
        print(f"Traceback: {traceback.format_exc()}")
        raise HTTPException(status_code=500, detail=f"Failed to update FCM token: {str(e)}")
