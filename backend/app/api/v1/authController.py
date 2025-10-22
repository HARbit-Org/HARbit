from fastapi import APIRouter, HTTPException, Depends, Header, Request
from fastapi.security import HTTPBearer
from typing import Annotated
from service.authService import AuthService
from api.di import get_auth_service
from model.dto.request.googleAuthRequestDto import GoogleAuthRequestDto
from model.dto.request.refreshTokenRequestDto import RefreshTokenRequestDto
from model.dto.response.authResponseDto import AuthResponseDto

router = APIRouter(prefix="/auth", tags=["Authentication"])
security = HTTPBearer()


# Dependency for protected routes
async def get_current_user(
    authorization: str = Header(None),
    auth_service: Annotated[AuthService, Depends(get_auth_service)] = None
):
    """Get current authenticated user - use this in protected endpoints"""
    if not authorization or not authorization.startswith('Bearer '):
        raise HTTPException(
            status_code=401,
            detail="Missing or invalid authorization header"
        )
    
    token = authorization.split(' ')[1]
    try:
        user_info = await auth_service.verify_access_token(token)
        user_info['auth_service'] = auth_service  # Pass service for logout
        return user_info
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=401, detail=str(e))


@router.post("/google", response_model=AuthResponseDto)
async def google_auth(
    request: Request,
    auth_request: GoogleAuthRequestDto,
    auth_service: Annotated[AuthService, Depends(get_auth_service)]
):
    """Google OAuth authentication endpoint"""
    try:
        # Get client info
        user_agent = request.headers.get("User-Agent")
        ip_address = request.client.host if request.client else None
        
        result = await auth_service.authenticate_with_google(
            auth_request,
            user_agent=user_agent,
            ip_address=ip_address
        )
        return result
    except HTTPException:
        raise
    except Exception as e:
        import traceback
        print(f"ERROR in google_auth: {str(e)}")
        print(f"Traceback: {traceback.format_exc()}")
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/refresh", response_model=AuthResponseDto)
async def refresh_token(
    refresh_request: RefreshTokenRequestDto,
    auth_service: Annotated[AuthService, Depends(get_auth_service)]
):
    """Refresh access token using refresh token"""
    try:
        result = await auth_service.refresh_token(refresh_request.refresh_token)
        return result
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/logout")
async def logout(
    current_user: dict = Depends(get_current_user)
):
    """Logout user and invalidate session"""
    try:
        auth_service = current_user['auth_service']
        success = await auth_service.logout(current_user['session_id'])
        if success:
            return {"message": "Logged out successfully"}
        else:
            raise HTTPException(status_code=400, detail="Logout failed")
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

