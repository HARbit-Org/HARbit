from fastapi import APIRouter, Depends, HTTPException
# from app.schemas.user import UserCreate, UserRead
# from app.services.user_service import UserService
# from api.di import get_user_service

# router = APIRouter(prefix="/users", tags=["users"])

# @router.post("", response_model=UserRead, status_code=201)
# def create_user(body: UserCreate, svc: UserService = Depends(get_user_service)):
#     try:
#         user = svc.register(email=body.email, full_name=body.full_name)
#         return user
#     except ValueError as e:
#         raise HTTPException(status_code=400, detail=str(e))