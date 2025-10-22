from repository.userRepository import UserRepository
from model.dto.request.updateProfileDto import UpdateProfileDto
from model.entity.users import Users
import uuid


class UserService:
    def __init__(self, user_repo: UserRepository):
        self.user_repo = user_repo
    
    def get_user_by_id(self, user_id: uuid.UUID) -> Users:
        """Get user by ID"""
        return self.user_repo.find_by_id(user_id)
    
    def update_profile(self, user_id: uuid.UUID, profile_data: UpdateProfileDto) -> Users:
        """Update user profile with provided data"""
        user = self.user_repo.find_by_id(user_id)
        
        if not user:
            raise ValueError(f"User with ID {user_id} not found")
        
        # Update user using repository's update_user method
        updated_user = self.user_repo.update_user(
            user_id=user_id,
            display_name=profile_data.display_name,
            picture_url=None,  # We don't update picture_url from profile form
            sex=profile_data.sex,
            birth_year=profile_data.birth_year,
            daily_step_goal=profile_data.daily_step_goal,
            timezone=profile_data.timezone
        )
        
        # Note: phone and preferred_email need to be added to update_user method
        # For now, we'll update them directly if they exist in the Users model
        if profile_data.phone is not None:
            user.phone = profile_data.phone
        
        if profile_data.preferred_email is not None:
            user.preferred_email = profile_data.preferred_email
        
        # Commit changes for fields not in update_user
        if profile_data.phone is not None or profile_data.preferred_email is not None:
            self.user_repo.db.commit()
            self.user_repo.db.refresh(user)
        
        return updated_user if updated_user else user
