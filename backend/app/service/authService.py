from google.auth.transport import requests
from google.oauth2 import id_token
from typing import Optional, Dict
import jwt
import secrets
import uuid
from datetime import datetime, timedelta
from fastapi import HTTPException, status
from repository.userRepository import UserRepository
from repository.sessionRepository import SessionRepository
from model.dto.request.googleAuthRequestDto import GoogleAuthRequestDto
from model.dto.response.authResponseDto import AuthResponseDto
from model.dto.response.userDto import UserDto
from model.entity.users import Users
import os


class AuthService:
    def __init__(
        self,
        user_repo: UserRepository,
        session_repo: SessionRepository
    ):
        self.user_repo = user_repo
        self.session_repo = session_repo
        
        # Google OAuth client IDs - from environment variables
        self.google_client_ids = {
            "android": os.getenv("GOOGLE_ANDROID_CLIENT_ID", "297962456687-36tcjh8fkjk7ggfdi7o5d78aq4i64k9r.apps.googleusercontent.com"),
            "web": os.getenv("GOOGLE_WEB_CLIENT_ID", "297962456687-1b01sulb6ndfjn83sneor62qqdt1qpum.apps.googleusercontent.com")
        }
        
        # JWT secret - MUST be in environment variable in production
        self.jwt_secret = os.getenv("JWT_SECRET_KEY", "your-secret-key-change-this-in-production")
        self.jwt_algorithm = "HS256"
        self.access_token_expire_hours = 1
        self.refresh_token_expire_days = 30

    async def verify_google_token(
        self,
        id_token_str: str,
        client_type: str
    ) -> Dict[str, any]:
        """Verify Google ID token and return user info"""
        try:
            # For Android apps, we verify with the web client ID
            # because that's the serverClientId used in the mobile app
            if client_type == "android":
                client_id = self.google_client_ids.get("web")
            else:
                client_id = self.google_client_ids.get(client_type)
                
            if not client_id:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Invalid client_type: {client_type}"
                )

            # Verify the token with Google
            id_info = id_token.verify_oauth2_token(
                id_token_str,
                requests.Request(),
                client_id
            )

            # Verify the issuer
            if id_info['iss'] not in ['accounts.google.com', 'https://accounts.google.com']:
                raise ValueError('Wrong issuer.')

            return {
                'provider_user_id': id_info['sub'],
                'email': id_info['email'],
                'display_name': id_info.get('name'),
                'picture_url': id_info.get('picture'),
                'email_verified': id_info.get('email_verified', False)
            }

        except ValueError as e:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail=f"Invalid Google token: {str(e)}"
            )
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail=f"Token verification failed: {str(e)}"
            )

    async def authenticate_with_google(
        self,
        auth_request: GoogleAuthRequestDto,
        user_agent: Optional[str] = None,
        ip_address: Optional[str] = None
    ) -> AuthResponseDto:
        """Complete Google OAuth authentication flow"""
        
        # 1. Verify Google token
        google_user = await self.verify_google_token(
            auth_request.id_token,
            auth_request.client_type
        )

        # 2. Find or create user
        user = await self.find_or_create_user(google_user)

        # 3. Create session
        session = self.create_user_session(
            user.id,
            user_agent=user_agent,
            ip_address=ip_address
        )

        # 4. Return auth response
        return AuthResponseDto(
            access_token=session['access_token'],
            refresh_token=session['refresh_token'],
            user=self._user_to_dto(user).model_dump(),
            expires_in=self.access_token_expire_hours * 3600
        )

    async def find_or_create_user(self, google_user: Dict[str, any]) -> Users:
        """Find existing user or create new one"""
        
        # Check if user exists by provider
        existing_user = self.user_repo.find_by_provider(
            provider="google",
            provider_user_id=google_user['provider_user_id']
        )

        if existing_user:
            # Update user info if needed
            self.user_repo.update_user(
                user_id=existing_user.id,
                display_name=google_user.get('display_name'),
                picture_url=google_user.get('picture_url')
            )
            return existing_user

        # Check if user exists by email (linking accounts)
        existing_user = self.user_repo.find_by_email(google_user['email'])
        
        if existing_user:
            # Link Google provider to existing user
            self.user_repo.create_user_provider(
                user_id=existing_user.id,
                provider="google",
                provider_user_id=google_user['provider_user_id']
            )
            return existing_user

        # Create new user
        new_user = self.user_repo.create_user(
            email=google_user['email'],
            display_name=google_user.get('display_name'),
            picture_url=google_user.get('picture_url')
        )

        # Link Google provider
        self.user_repo.create_user_provider(
            user_id=new_user.id,
            provider="google",
            provider_user_id=google_user['provider_user_id']
        )

        return new_user

    def create_user_session(
        self,
        user_id,
        user_agent: Optional[str] = None,
        ip_address: Optional[str] = None
    ) -> Dict[str, str]:
        """Create new session with JWT tokens"""
        
        # Generate access token (JWT)
        access_token = self.generate_jwt_token(
            user_id=user_id,
            expires_in_hours=self.access_token_expire_hours
        )

        # Generate refresh token (random string)
        refresh_token = secrets.token_urlsafe(32)

        # Calculate expiration
        expires_at = datetime.utcnow() + timedelta(hours=self.access_token_expire_hours)

        # Save session to database
        session = self.session_repo.create_session(
            user_id=user_id,
            access_token=access_token,
            refresh_token=refresh_token,
            expires_at=expires_at,
            user_agent=user_agent,
            ip_address=ip_address
        )

        return {
            'access_token': access_token,
            'refresh_token': refresh_token,
            'session_id': str(session.id)
        }

    def generate_jwt_token(self, user_id, expires_in_hours: int = 1) -> str:
        """Generate JWT access token"""
        payload = {
            'user_id': str(user_id),
            'exp': datetime.utcnow() + timedelta(hours=expires_in_hours),
            'iat': datetime.utcnow(),
            'type': 'access'
        }
        return jwt.encode(payload, self.jwt_secret, algorithm=self.jwt_algorithm)

    async def verify_access_token(self, token: str) -> Dict[str, any]:
        """Verify JWT token and return user info"""
        try:
            # Decode JWT
            payload = jwt.decode(
                token,
                self.jwt_secret,
                algorithms=[self.jwt_algorithm]
            )

            # Check if session exists and is valid
            session = self.session_repo.find_valid_session(token)
            if not session:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Session not found or expired"
                )

            # Get user
            user = self.user_repo.find_by_id(session.user_id)
            if not user:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="User not found"
                )

            # Update last used timestamp
            self.session_repo.update_last_used(session.id)

            return {
                'user_id': str(user.id),
                'session_id': str(session.id),
                'email': user.email,
                'display_name': user.display_name,
                'user': user
            }

        except jwt.ExpiredSignatureError:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token expired"
            )
        except jwt.InvalidTokenError:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token"
            )

    async def refresh_token(self, refresh_token: str) -> AuthResponseDto:
        """Refresh access token using refresh token"""
        
        # Find session by refresh token
        session = self.session_repo.find_by_refresh_token(refresh_token)
        if not session:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid refresh token"
            )

        # Get user
        user = self.user_repo.find_by_id(session.user_id)
        if not user:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="User not found"
            )

        # Delete old session
        self.session_repo.delete_session(session.id)

        # Create new session
        new_session = self.create_user_session(user.id)

        return AuthResponseDto(
            access_token=new_session['access_token'],
            refresh_token=new_session['refresh_token'],
            user=self._user_to_dto(user).model_dump(),
            expires_in=self.access_token_expire_hours * 3600
        )

    async def logout(self, session_id: str) -> bool:
        """Logout user and delete session"""
        return self.session_repo.delete_session(uuid.UUID(session_id))

    def _user_to_dto(self, user: Users) -> UserDto:
        """Convert User entity to UserDto"""
        return UserDto(
            id=user.id,
            email=user.email,
            display_name=user.display_name,
            picture_url=user.picture_url,
            sex=user.sex,
            birth_year=user.birth_year,
            daily_step_goal=user.daily_step_goal or 10000,
            timezone=user.timezone or "America/Lima"
        )
