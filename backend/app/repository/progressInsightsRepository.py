from sqlalchemy.orm import Session
from sqlalchemy import and_
from typing import List, Optional
from datetime import datetime, date
import uuid
from model.entity.progressInsights import ProgressInsights


class ProgressInsightsRepository:
    def __init__(self, db: Session):
        self.db = db

    def create_insight(
        self,
        user_id: uuid.UUID,
        type: str,
        category: str,
        period_type: str,
        period_start: date,
        comparison_start: date,
        comparison_value: float,
        current_value: float,
        delta_value: float,
        delta_pct: float,
        message_title: str,
        message_body: str
    ) -> ProgressInsights:
        """Create a new progress insight"""
        insight = ProgressInsights(
            user_id=user_id,
            type=type,
            category=category,
            period_type=period_type,
            period_start=period_start,
            comparison_start=comparison_start,
            comparison_value=comparison_value,
            current_value=current_value,
            delta_value=delta_value,
            delta_pct=delta_pct,
            message_title=message_title,
            message_body=message_body
        )
        
        self.db.add(insight)
        self.db.commit()
        self.db.refresh(insight)
        
        return insight

    def get_user_insights(
        self,
        user_id: uuid.UUID,
        period_type: Optional[str] = None,
        limit: int = 50,
        offset: int = 0
    ) -> List[ProgressInsights]:
        """Get insights for a user"""
        query = self.db.query(ProgressInsights).filter(
            ProgressInsights.user_id == user_id
        )
        
        if period_type:
            query = query.filter(ProgressInsights.period_type == period_type)
        
        query = query.order_by(ProgressInsights.created_at.desc())
        query = query.offset(offset).limit(limit)
        
        return query.all()

    def get_latest_insight_for_period(
        self,
        user_id: uuid.UUID,
        period_type: str,
        period_start: date
    ) -> Optional[ProgressInsights]:
        """Get the latest insight for a specific period"""
        return (
            self.db.query(ProgressInsights)
            .filter(
                and_(
                    ProgressInsights.user_id == user_id,
                    ProgressInsights.period_type == period_type,
                    ProgressInsights.period_start == period_start
                )
            )
            .order_by(ProgressInsights.created_at.desc())
            .first()
        )

    def get_insights_created_today(self, target_date: date = None) -> List[ProgressInsights]:
        """Get all insights created on a specific date (for batch notifications)"""
        if target_date is None:
            target_date = datetime.now().date()
        
        start_of_day = datetime.combine(target_date, datetime.min.time())
        end_of_day = datetime.combine(target_date, datetime.max.time())
        
        return (
            self.db.query(ProgressInsights)
            .filter(
                and_(
                    ProgressInsights.created_at >= start_of_day,
                    ProgressInsights.created_at <= end_of_day
                )
            )
            .all()
        )

    def delete_insight(self, insight_id: uuid.UUID) -> bool:
        """Delete an insight"""
        insight = self.db.query(ProgressInsights).filter(
            ProgressInsights.id == insight_id
        ).first()
        
        if insight:
            self.db.delete(insight)
            self.db.commit()
            return True
        
        return False
