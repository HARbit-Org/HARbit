from fastapi import APIRouter, Depends, HTTPException, Query
import uuid
from typing import Annotated

from api.di import get_progress_service
from api.v1.authController import get_current_user
from model.dto.progressInsightDto import ProgressInsightDto
from service.progressService import ProgressService

router = APIRouter(prefix="/progress", tags=["progress"])

@router.get("/insights", response_model=list[ProgressInsightDto])
async def get_all_progress_insights_for_user(
    current_user: dict = Depends(get_current_user),
    progress_service: Annotated[ProgressService, Depends(get_progress_service)] = None,
):
    user_id = uuid.UUID(current_user['user_id'])

    try:
        insights = progress_service.getProgressInsightsForUser(user_id)
        print(f"🔍 DEBUG: Got {len(insights)} insights")  # ✅ DEBUG
        
        if insights:
            print(f"🔍 DEBUG: First insight type: {type(insights[0])}")  # ✅ DEBUG
            print(f"🔍 DEBUG: First insight: {insights[0]}")  # ✅ DEBUG
        
        return insights
    
    except Exception as e:
        import traceback
        print(f"❌ ERROR: {str(e)}")  # ✅ DEBUG
        print(f"❌ TRACEBACK:\n{traceback.format_exc()}")  # ✅ DEBUG
        
        raise HTTPException(
            status_code=500,
            detail=f"Failed to get progress insights: {str(e)}"
        )