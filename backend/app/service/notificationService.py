from datetime import datetime, timedelta, timezone
from typing import Optional, Dict, Any
import uuid
from repository.notificationRepository import NotificationRepository
from repository.userRepository import UserRepository
import firebase_admin
from firebase_admin import credentials, messaging


class NotificationService:
    """
    Service for sending notifications to users.
    
    This service is responsible ONLY for:
    - Storing notification records in database
    - Sending push notifications via FCM
    - Managing notification delivery status
    - Cooldown period management
    
    It does NOT perform analysis - that's handled by ActivityService.
    """
    
    # Notification types
    TYPE_SEDENTARY_ALERT = "sedentary_alert"
    TYPE_PROGRESS = "progress"
    TYPE_IMPROVEMENT = "improvement_opportunity"
    
    # Cooldown configuration
    SEDENTARY_COOLDOWN_MINUTES = 30  # Don't send same alert within 30 minutes
    
    def __init__(
        self,
        notification_repo: NotificationRepository,
        user_repo: UserRepository
    ):
        self.notification_repo = notification_repo
        self.user_repo = user_repo
        self._firebase_initialized = False

    def _initialize_firebase(self):
        """Initialize Firebase Admin SDK (lazy initialization)"""
        if not self._firebase_initialized:
            try:
                # Check if already initialized
                firebase_admin.get_app()
            except ValueError:
                # Not initialized, so initialize it
                cred = credentials.Certificate("C:\\Users\\andre\\Documents\\HARbit-Org\\HARbit\\backend\\harbit-app-firebase-adminsdk-fbsvc-31a0b86f1f.json")
                firebase_admin.initialize_app(cred)
            
            self._firebase_initialized = True

    def send_sedentary_alert(
        self,
        user_id: uuid.UUID,
        analysis_result: Dict[str, Any]
    ) -> Optional[Dict[str, Any]]:
        """
        Send a sedentary alert notification to the user.
        
        This method:
        1. Validates analysis result has sufficient data
        2. Checks if notification should be sent (cooldown)
        3. Creates notification record in database
        4. Sends FCM push notification
        5. Returns result with notification info
        
        Args:
            user_id: UUID of the user
            analysis_result: Analysis data from ActivityService.analyze_sedentary_behavior()
                Must contain: is_sedentary, sit_percentage, sit_hours, has_sufficient_data, etc.
            
        Returns:
            Dict with notification result:
            {
                'notification_sent': bool,
                'notification_id': int (if sent),
                'push_sent': bool (if sent),
                'reason': str (if not sent)
            }
            Or None if analysis_result is invalid
        """
        # Validate analysis result exists
        if not analysis_result:
            return {
                'notification_sent': False,
                'reason': 'no_analysis_data'
            }
        
        # Check if there's sufficient data for reliable analysis
        if not analysis_result.get('has_sufficient_data', True):
            return {
                'notification_sent': False,
                'reason': 'insufficient_data',
                'total_hours': analysis_result.get('total_hours', 0),
                'minimum_required_hours': analysis_result.get('minimum_required_hours', 0)
            }
        
        # Check if user is actually sedentary
        if not analysis_result.get('is_sedentary'):
            return {
                'notification_sent': False,
                'reason': 'not_sedentary'
            }
        
        # Check if we already sent a notification recently (cooldown)
        last_notification = self.notification_repo.get_last_notification_by_type(
            user_id=user_id,
            notification_type=self.TYPE_SEDENTARY_ALERT,
            hours_ago=int(self.SEDENTARY_COOLDOWN_MINUTES / 60)  # Convert minutes to hours for repository query
        )
        
        if last_notification:
            # Check if cooldown period (in minutes) has passed
            time_since_last = datetime.now(timezone.utc) - last_notification.ts.replace(tzinfo=timezone.utc)
            minutes_since_last = time_since_last.total_seconds() / 60
            
            if minutes_since_last < self.SEDENTARY_COOLDOWN_MINUTES:
                return {
                    'notification_sent': False,
                    'reason': 'cooldown_active',
                    'last_notification_at': last_notification.ts.isoformat(),
                    'minutes_since_last': round(minutes_since_last, 1),
                    'cooldown_minutes': self.SEDENTARY_COOLDOWN_MINUTES
                }
        
        # Create notification payload
        notification_payload = {
            'sedentary_percentage': analysis_result['sedentary_percentage'],
            'sedentary_hours': round(analysis_result['sedentary_hours'], 2),
            'total_hours': round(analysis_result['total_hours'], 2),
            'evaluation_period_minutes': analysis_result['evaluation_period_minutes'],
            'sedentary_breakdown': analysis_result.get('sedentary_breakdown', {}),
            'timestamp': analysis_result['timestamp']
        }
        
        # Create notification record
        notification = self.notification_repo.create_notification(
            user_id=user_id,
            notification_type=self.TYPE_SEDENTARY_ALERT,
            payload=notification_payload
        )
        
        # Send push notification
        push_sent = self._send_sedentary_push(
            user_id=user_id,
            sedentary_hours=analysis_result['sedentary_hours'],
            sedentary_percentage=analysis_result['sedentary_percentage'],
            notification_id=notification.id
        )
        
        return {
            'notification_sent': True,
            'notification_id': notification.id,
            'push_sent': push_sent
        }

    def _send_sedentary_push(
        self,
        user_id: uuid.UUID,
        sedentary_hours: float,
        sedentary_percentage: float,
        notification_id: int
    ) -> bool:
        """
        Send push notification via FCM for sedentary alert.
        
        Args:
            user_id: UUID of the user
            sedentary_hours: Hours spent in sedentary activities
            sedentary_percentage: Percentage of time in sedentary activities
            notification_id: ID of the notification record
            
        Returns:
            True if notification was sent successfully, False otherwise
        """
        try:
            # Get user's FCM token
            user = self.user_repo.find_by_id(user_id)
            if not user or not user.fcm_token:
                print(f"No FCM token for user {user_id}")
                return False
            
            # Initialize Firebase if not already done
            self._initialize_firebase()
            
            # Create notification message
            title = "⏰ ¡Hora de moverte!"
            # body = f"Has estado en actividades sedentarias {sedentary_hours:.1f}h ({sedentary_percentage:.0f}%). ¡Levántate y camina un poco! 🚶"
            body = f"Has estado sentado los últimos 30 minutos. ¡Es momento de moverse! 🚶"
            
            message = messaging.Message(
                notification=messaging.Notification(
                    title=title,
                    body=body,
                ),
                data={
                    'type': self.TYPE_SEDENTARY_ALERT,
                    'notification_id': str(notification_id),
                    'sedentary_hours': str(sedentary_hours),
                    'sedentary_percentage': str(sedentary_percentage),
                    'timestamp': datetime.now(timezone.utc).isoformat()
                },
                token=user.fcm_token,
                android=messaging.AndroidConfig(
                    priority='high',
                    notification=messaging.AndroidNotification(
                        icon='ic_notification',
                        color='#FF5722',  # Orange for alert
                        channel_id='sedentary_alerts'
                    )
                )
            )
            
            # Send the message
            response = messaging.send(message)
            print(f"✅ Sedentary notification sent to user {user_id}: {response}")
            
            # Mark as delivered
            self.notification_repo.mark_as_delivered(notification_id)
            
            return True
            
        except Exception as e:
            print(f"❌ Error sending sedentary notification to user {user_id}: {e}")
            return False

    def send_progress_notification(
        self,
        user_id: uuid.UUID,
        progress_data: Dict[str, Any]
    ) -> Optional[int]:
        """
        Send a progress notification to the user.
        
        Args:
            user_id: UUID of the user
            progress_data: Dict with progress information
            
        Returns:
            Notification ID if created, None otherwise
        """
        # TODO: Implement progress notification logic
        notification = self.notification_repo.create_notification(
            user_id=user_id,
            notification_type=self.TYPE_PROGRESS,
            payload=progress_data
        )
        
        return notification.id

    def send_improvement_notification(
        self,
        user_id: uuid.UUID,
        improvement_data: Dict[str, Any]
    ) -> Optional[int]:
        """
        Send an improvement opportunity notification to the user.
        
        Args:
            user_id: UUID of the user
            improvement_data: Dict with improvement opportunity information
            
        Returns:
            Notification ID if created, None otherwise
        """
        # TODO: Implement improvement opportunity notification logic
        notification = self.notification_repo.create_notification(
            user_id=user_id,
            notification_type=self.TYPE_IMPROVEMENT,
            payload=improvement_data
        )
        
        return notification.id

    def get_user_notifications(
        self,
        user_id: uuid.UUID,
        limit: int = 50,
        offset: int = 0
    ):
        """Get all notifications for a user"""
        return self.notification_repo.get_user_notifications(
            user_id=user_id,
            limit=limit,
            offset=offset
        )

    def mark_notification_as_read(self, notification_id: int):
        """Mark a notification as read"""
        return self.notification_repo.mark_as_read(notification_id)
