from datetime import datetime, timedelta, timezone, date
from typing import Optional, Dict, Any, List
import uuid
from repository.progressInsightsRepository import ProgressInsightsRepository
from repository.activityRepository import ActivityRepository
from repository.userRepository import UserRepository
from model.entity.users import Users
from service.notificationService import NotificationService
from model.dto.progressInsightDto import ProgressInsightDto


class ProgressService:
    """
    Service for calculating and managing user progress insights.
    
    Handles:
    - Weekly progress calculation (sedentary vs active time)
    - Progress insights generation
    - Comparison between current and previous periods
    """
    
    # Insight types
    TYPE_PROGRESS = "progress"
    TYPE_IMPROVEMENT = "improvement_opportunity"
    TYPE_OTHER = "other"
    
    # Period types
    PERIOD_WEEK = "week"
    PERIOD_DAY = "day"
    PERIOD_MONTH = "month"
    
    # Activity classifications (aligned with ActivityService)
    SEDENTARY_ACTIVITIES = {'sit', 'type', 'eat', 'write'}
    ACTIVE_ACTIVITIES = {'walk', 'workouts'}
    OTHER_ACTIVITIES = {'others'}
    
    # Category for weekly activity analysis
    CATEGORY_ACTIVITY = "activity"
    CATEGORY_SEDENTARY = "sedentary"
    
    def __init__(
        self,
        progress_insights_repo: ProgressInsightsRepository,
        activity_repo: ActivityRepository,
        user_repo: UserRepository,
        notification_service: NotificationService
    ):
        self.progress_insights_repo = progress_insights_repo
        self.activity_repo = activity_repo
        self.user_repo = user_repo
        self.notification_service = notification_service

    def calculate_weekly_progress_for_all_users(self) -> List[Dict[str, Any]]:
        """
        Calculate weekly progress for all users.
        
        This method should be called by a cron job every Sunday.
        Compares current week vs previous week for:
        - Active time (walk, workouts, others)
        - Sedentary time (sit, type, eat, write)
        
        Returns:
            List of results with user_id and insight_id for each user processed
        """
        print("📊 Starting weekly progress calculation for all users...")
        
        # Get all users (you might want to add pagination for large user bases)
        users = self._get_all_active_users()
        
        results = []
        for user in users:
            try:
                result = self.calculate_weekly_progress(user.id)
                if result:
                    results.append({
                        'user_id': str(user.id),
                        'status': 'success',
                        'insights_created': len(result) if isinstance(result, list) else 1
                    })
            except Exception as e:
                print(f"❌ Error calculating progress for user {user.id}: {e}")
                # Rollback the session to recover from errors
                self.progress_insights_repo.db.rollback()
                results.append({
                    'user_id': str(user.id),
                    'status': 'failed',
                    'error': str(e)
                })
        
        print(f"✅ Completed weekly progress calculation. Processed {len(users)} users.")
        return results

    def calculate_weekly_progress(self, user_id: uuid.UUID) -> Optional[List[Any]]:
        """
        Calculate weekly progress for a specific user.
        
        Compares current week (Mon-Sun) vs previous week.
        Generates progress insight based on activity improvement.
        
        Args:
            user_id: UUID of the user
            
        Returns:
            List of ProgressInsight entities if created, None otherwise
        """
        # Define time ranges (current week and previous week)
        now = datetime.now(timezone.utc)
        
        # Current week: Monday to Sunday
        current_week_start = (now - timedelta(days=now.weekday())).replace(
            hour=0, minute=0, second=0, microsecond=0
        )
        current_week_end = current_week_start + timedelta(days=7)
        
        # Previous week
        previous_week_start = current_week_start - timedelta(days=7)
        previous_week_end = current_week_start
        
        # Get activity distribution for both weeks
        current_distribution = self.activity_repo.get_activity_distribution(
            user_id=user_id,
            start_time=current_week_start,
            end_time=current_week_end
        )
        
        previous_distribution = self.activity_repo.get_activity_distribution(
            user_id=user_id,
            start_time=previous_week_start,
            end_time=previous_week_end
        )
        
        # Calculate metrics
        current_metrics = self._calculate_activity_metrics(current_distribution)
        previous_metrics = self._calculate_activity_metrics(previous_distribution)
        
        # Determine insight type and message
        insight_data_list = self._determine_insight_type(
            current_metrics=current_metrics,
            previous_metrics=previous_metrics,
            user_id=user_id
        )
        
        if not insight_data_list or len(insight_data_list) == 0:
            print(f"ℹ️ No insights generated for user {user_id}")
            return None
        
        insights = []
        
        # Create progress insight(s)
        for insight in insight_data_list:
            saved_insight = self.progress_insights_repo.create_insight(
                user_id=user_id,
                type=insight['type'],
                category=insight.get('category', self.CATEGORY_ACTIVITY),
                period_type=self.PERIOD_WEEK,
                period_start=current_week_start.date(),
                comparison_start=previous_week_start.date(),
                comparison_value=insight['comparison_value'],
                current_value=insight['current_value'],
                delta_value=insight['delta_value'],
                delta_pct=insight['delta_pct'],
                message_title=insight['message_title'],
                message_body=insight['message_body']
            )

            insights.append(saved_insight)

        notification_result = self.notification_service.send_progress_notification(
            user_id=user_id,
            progress_data = {
                'type': 'progress',
                'message_title': 'Progreso Semanal',
                'message_body': 'Tenemos nuevas actualizaciones sobre tu actividad semanal. ¡Revisa tu progreso!'
            }
        )
        
        print(f"✅ Created {len(insights)} insight(s) for user {user_id}")
        return insights
    
    def getProgressInsightsForUser(self, user_id: uuid.UUID) -> List[Any]:
        """
        Retrieve all progress insights for a specific user.
        
        Args:
            user_id: UUID of the user
        Returns:
            List of ProgressInsight DTOs
        """

        insights_data = self.progress_insights_repo.get_insights_by_user(user_id=user_id)
        
        result = []
        
        for insight in insights_data:
            result.append(
                ProgressInsightDto(
                    id = str(insight.id),
                    userId = str(insight.user_id),
                    type = insight.type,
                    category = insight.category,
                    message_title = insight.message_title,
                    message_body = insight.message_body,
                    created_at = insight.created_at.isoformat()
                )
            )
        
        return result


    def _calculate_activity_metrics(self, distribution: List[dict]) -> Dict[str, float]:
        """
        Calculate activity metrics from distribution data.
        
        Args:
            distribution: Activity distribution from repository
            
        Returns:
            Dict with active_minutes, sedentary_minutes, total_minutes
        """
        active_minutes = 0.0
        sedentary_minutes = 0.0
        other_minutes = 0.0
        
        for activity in distribution:
            activity_label = activity['activity_label'].lower()
            minutes = activity['total_minutes']
            
            if activity_label in self.ACTIVE_ACTIVITIES:
                active_minutes += minutes
            elif activity_label in self.SEDENTARY_ACTIVITIES:
                sedentary_minutes += minutes
            elif activity_label in self.OTHER_ACTIVITIES:
                other_minutes += minutes

        total_minutes = active_minutes + sedentary_minutes + other_minutes

        return {
            'active_minutes': round(active_minutes, 2),
            'sedentary_minutes': round(sedentary_minutes, 2),
            'other_minutes': round(other_minutes, 2),
            'total_minutes': round(total_minutes, 2)
        }

    def _determine_insight_type(
        self,
        current_metrics: Dict[str, float],
        previous_metrics: Dict[str, float],
        user_id: uuid.UUID
    ) -> List[Dict[str, Any]]:
        """
        Determine insight type based on activity comparison.
        
        Logic:
        - If current week has NO activity: "No has realizado actividades..."
        - If previous week had NO data: "Felicitaciones por mejorar..." (TYPE_OTHER)
        - If active time increased OR sedentary time decreased: TYPE_PROGRESS
        - Otherwise: TYPE_IMPROVEMENT
        
        Args:
            current_metrics: Current week metrics
            previous_metrics: Previous week metrics
            user_id: User ID for logging
            
        Returns:
            List of dicts with insight data
        """
        current_total = current_metrics['total_minutes']
        previous_total = previous_metrics['total_minutes']
        current_active = current_metrics['active_minutes']
        previous_active = previous_metrics['active_minutes']
        current_sedentary = current_metrics['sedentary_minutes']
        previous_sedentary = previous_metrics['sedentary_minutes']
        
        # Case 1: No activity this week (and no previous activity)
        if current_total == 0 and previous_total == 0:
            return [{
                'type': self.TYPE_OTHER,
                'category': self.CATEGORY_ACTIVITY,
                'comparison_value': 0.0,
                'current_value': 0.0,
                'delta_value': 0.0,
                'delta_pct': 0.0,
                'message_title': 'Te extrañamos',
                'message_body': 'No has realizado actividades esta semana 😢. Usa HARbit para mejorar tu bienestar físico.'
            }]
        
        # Case 2: First week with data (no previous data)
        if previous_total == 0 and current_total > 0:
            hours = int(current_total // 60)
            minutes = int(current_total % 60)
            time_str = f"{hours}h {minutes}min" if hours > 0 else f"{minutes} minutos"
            
            return [{
                'type': self.TYPE_OTHER,
                'category': self.CATEGORY_ACTIVITY,
                'comparison_value': 0.0,
                'current_value': current_total,
                'delta_value': current_total,
                'delta_pct': 100.0,
                'message_title': '¡Enhorabuena!',
                'message_body': f'¡Felicitaciones por mejorar tu actividad con HARbit! Esta semana registraste {time_str} de pura acción.'
            }]
        
        # Case 3: Compare activity levels
        # Progress if: active time increased OR sedentary time decreased
        delta_active = current_active - previous_active
        delta_sedentary = current_sedentary - previous_sedentary
        
        # Calculate percentage changes
        delta_active_pct = (delta_active / previous_active * 100) if previous_active > 0 else 0
        delta_sedentary_pct = (delta_sedentary / previous_sedentary * 100) if previous_sedentary > 0 else 0

        data = []
        
        hours_delta_active = int(abs(delta_active) // 60)
        minutes_delta_active = int(abs(delta_active) % 60)
        time_str_active = f"{hours_delta_active}h {minutes_delta_active}min" if hours_delta_active > 0 else f"{minutes_delta_active} minutos"

        # Only create active insight if there's actual active time
        if previous_active > 0 or current_active > 0:
            if delta_active > 0:
                data.append({
                    'type': self.TYPE_PROGRESS,
                    'category': self.CATEGORY_ACTIVITY,
                    'comparison_value': previous_active,
                    'current_value': current_active,
                    'delta_value': delta_active,
                    'delta_pct': round(delta_active_pct, 2),
                    'message_title': '¡Lograste aumentar tu tiempo activo!',
                    'message_body': f'Aumentaste tu tiempo activo en {time_str_active} ({abs(delta_active_pct):.0f}%) esta semana. ¡Sigue así!'
                })
            elif delta_active < 0:
                data.append({
                    'type': self.TYPE_IMPROVEMENT,
                    'category': self.CATEGORY_ACTIVITY,
                    'comparison_value': previous_active,
                    'current_value': current_active,
                    'delta_value': delta_active,
                    'delta_pct': round(delta_active_pct, 2),
                    'message_title': 'Tu tiempo activo ha disminuido',
                    'message_body': f'Tu actividad disminuyó {time_str_active} ({abs(delta_active_pct):.0f}%) esta semana. ¡Puedes remontar!'
                })

        hours_delta_sedentary = int(abs(delta_sedentary) // 60)
        minutes_delta_sedentary = int(abs(delta_sedentary) % 60)
        time_str_sedentary = f"{hours_delta_sedentary}h {minutes_delta_sedentary}min" if hours_delta_sedentary > 0 else f"{minutes_delta_sedentary} minutos"

        # Only create sedentary insight if there's actual sedentary time
        if previous_sedentary > 0 or current_sedentary > 0:
            if delta_sedentary < 0:
                data.append({
                    'type': self.TYPE_PROGRESS,
                    'category': self.CATEGORY_SEDENTARY,
                    'comparison_value': previous_sedentary,
                    'current_value': current_sedentary,
                    'delta_value': delta_sedentary,
                    'delta_pct': round(delta_sedentary_pct, 2),
                    'message_title': '¡Lograste reducir tu sedentarismo!',
                    'message_body': f'Redujiste tu tiempo sentado en {time_str_sedentary} ({abs(delta_sedentary_pct):.0f}%) esta semana. ¡Genial!'
                })
            elif delta_sedentary > 0:
                data.append({
                    'type': self.TYPE_IMPROVEMENT,
                    'category': self.CATEGORY_SEDENTARY,
                    'comparison_value': previous_sedentary,
                    'current_value': current_sedentary,
                    'delta_value': delta_sedentary,
                    'delta_pct': round(delta_sedentary_pct, 2),
                    'message_title': 'Aumentó tu tiempo sentado',
                    'message_body': f'Has estado sentado {time_str_sedentary} más ({abs(delta_sedentary_pct):.0f}%) esta semana. Intenta caminar 5 minutos cada media hora.'
                })

        return data

    def _get_all_active_users(self) -> List[Any]:
        """
        Get all active users for progress calculation.
        
        In the future, you might want to:
        - Filter by users who have recent activity
        - Paginate for large user bases
        - Filter by user preferences (opt-in for weekly reports)
        
        Returns:
            List of User entities
        """
        # For now, get all users from the database
        # You might want to add filters or pagination here
        return self.user_repo.db.query(Users).all()
