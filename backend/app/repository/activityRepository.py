from sqlalchemy.orm import Session
from sqlalchemy import func, cast, Float, Numeric
from typing import List
from datetime import datetime
import uuid
from model.entity.processedActivities import ProcessedActivities


class ActivityRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_activity_distribution(
        self,
        user_id: uuid.UUID,
        start_time: datetime,
        end_time: datetime
    ) -> List[dict]:
        """
        Get activity distribution for a given user and time range.
        
        Each row represents ~2.5 seconds of activity (50% overlap).
        Returns activity label, total seconds/minutes/hours, and percentage.
        """
        # Subquery to calculate total count for percentage calculation
        total_count_subquery = (
            self.db.query(func.count().label('total'))
            .filter(
                ProcessedActivities.user_id == user_id,
                ProcessedActivities.ts_start >= start_time,
                ProcessedActivities.ts_end <= end_time
            )
            .scalar_subquery()
        )
        
        # Main query with aggregations
        query = (
            self.db.query(
                ProcessedActivities.activity_label,
                (func.count() * 2.5).label('total_seconds'),
                func.round(cast(func.count() * 2.5 / 60, Numeric), 2).label('total_minutes'),
                func.round(cast(func.count() * 2.5 / 3600, Numeric), 2).label('total_hours'),
                func.round(
                    cast(100.0 * func.count() / total_count_subquery, Numeric), 
                    2
                ).label('percentage')
            )
            .filter(
                ProcessedActivities.user_id == user_id,
                ProcessedActivities.ts_start >= start_time,
                ProcessedActivities.ts_end <= end_time
            )
            .group_by(ProcessedActivities.activity_label)
            .order_by(func.round(
                cast(100.0 * func.count() / total_count_subquery, Numeric), 
                2
            ).desc())
        )
        
        results = query.all()
        
        return [
            {
                'activity_label': row.activity_label,
                'total_seconds': float(row.total_seconds),
                'total_minutes': float(row.total_minutes),
                'total_hours': float(row.total_hours),
                'percentage': float(row.percentage)
            }
            for row in results
        ]
