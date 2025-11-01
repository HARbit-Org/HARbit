from sqlalchemy.orm import Session
from sqlalchemy import func, and_
from typing import Optional, List
from datetime import datetime, timedelta
import uuid
from model.entity.notifications import Notifications


class NotificationRepository:
    def __init__(self, db: Session):
        self.db = db

    def create_notification(
        self,
        user_id: uuid.UUID,
        notification_type: str,
        payload: dict = None
    ) -> Notifications:
        """Create a new notification"""
        notification = Notifications(
            user_id=user_id,
            type=notification_type,
            payload=payload,
            delivered_push=False
        )
        
        self.db.add(notification)
        self.db.commit()
        self.db.refresh(notification)
        
        return notification

    def mark_as_delivered(self, notification_id: int) -> Optional[Notifications]:
        """Mark notification as delivered"""
        notification = self.db.query(Notifications).filter(
            Notifications.id == notification_id
        ).first()
        
        if notification:
            notification.delivered_push = True
            notification.delivered_at = datetime.utcnow()
            self.db.commit()
            self.db.refresh(notification)
        
        return notification

    def mark_as_read(self, notification_id: int) -> Optional[Notifications]:
        """Mark notification as read"""
        notification = self.db.query(Notifications).filter(
            Notifications.id == notification_id
        ).first()
        
        if notification:
            notification.read_at = datetime.utcnow()
            self.db.commit()
            self.db.refresh(notification)
        
        return notification

    def get_last_notification_by_type(
        self,
        user_id: uuid.UUID,
        notification_type: str,
        hours_ago: int = 24
    ) -> Optional[Notifications]:
        """Get the last notification of a specific type for a user within the last X hours"""
        cutoff_time = datetime.utcnow() - timedelta(hours=hours_ago)
        
        return self.db.query(Notifications).filter(
            and_(
                Notifications.user_id == user_id,
                Notifications.type == notification_type,
                Notifications.ts >= cutoff_time
            )
        ).order_by(Notifications.ts.desc()).first()

    def get_user_notifications(
        self,
        user_id: uuid.UUID,
        limit: int = 50,
        offset: int = 0
    ) -> List[Notifications]:
        """Get all notifications for a user"""
        return self.db.query(Notifications).filter(
            Notifications.user_id == user_id
        ).order_by(Notifications.ts.desc()).limit(limit).offset(offset).all()

    def get_unread_notifications(
        self,
        user_id: uuid.UUID
    ) -> List[Notifications]:
        """Get all unread notifications for a user"""
        return self.db.query(Notifications).filter(
            and_(
                Notifications.user_id == user_id,
                Notifications.read_at.is_(None)
            )
        ).order_by(Notifications.ts.desc()).all()
