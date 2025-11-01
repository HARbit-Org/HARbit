"""
Test script for Activity Analysis and Notification Services

This script tests the sedentary behavior analysis and notification system.

The system is divided into two services:
1. ActivityService - Analyzes user activity patterns (sedentarism, progress, etc.)
2. NotificationService - Sends notifications via FCM and manages notification records

Usage:
    python test_notification_service.py

Requirements:
    - Backend server running on http://localhost:8000
    - Valid JWT access token
    - User with recent activity data (last 2 hours)
"""

import requests
import json
from datetime import datetime, timedelta, timezone

# Configuration
BASE_URL = "http://localhost:8000"
ACCESS_TOKEN = "your_access_token_here"  # Replace with a valid token

def test_sedentary_detection_on_batch():
    """
    Test sedentary detection by sending a sensor data batch.
    The backend should automatically evaluate sedentary behavior after processing.
    """
    
    print("=" * 70)
    print("Testing Sedentary Detection on Sensor Batch Upload")
    print("=" * 70)
    
    url = f"{BASE_URL}/sensor-data"
    headers = {
        "Authorization": f"Bearer {ACCESS_TOKEN}",
        "Content-Type": "application/json"
    }
    
    # Sample sensor batch (this is just to trigger the evaluation)
    # In reality, you'd send real sensor data from the mobile app
    payload = {
        "userId": "00000000-0000-0000-0000-000000000000",  # Will be replaced by JWT user_id
        "readings": [
            {
                "timestamp": datetime.now(timezone.utc).isoformat(),
                "accelX": 0.1,
                "accelY": 0.2,
                "accelZ": 9.8,
                "gyroX": 0.0,
                "gyroY": 0.0,
                "gyroZ": 0.0
            }
        ]
    }
    
    print(f"\nURL: {url}")
    print(f"Sending sample batch to trigger sedentary evaluation...")
    print(f"Token (first 50 chars): {ACCESS_TOKEN[:50]}...")
    
    try:
        response = requests.post(url, headers=headers, json=payload)
        
        print(f"\nStatus Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        
        if response.status_code == 201:
            print("\n✅ SUCCESS: Batch processed!")
            print("\nBackend process flow:")
            print("1. ✅ Sensor batch processed and classified")
            print("2. ✅ ActivityService.analyze_sedentary_behavior() called")
            print("3. ✅ NotificationService.send_sedentary_alert() called (if sedentary)")
            print("\nCheck backend logs for analysis and notification results.")
            print("If user has been sitting >70% in last 2 hours, notification will be sent.")
        else:
            print(f"\n❌ FAILED: {response.json().get('detail', 'Unknown error')}")
            
    except requests.exceptions.ConnectionError:
        print("\n❌ ERROR: Could not connect to backend server.")
        print("Make sure the backend is running on http://localhost:8000")
    except Exception as e:
        print(f"\n❌ ERROR: {str(e)}")


def print_sedentary_config():
    """Print sedentary detection configuration"""
    print("\n" + "=" * 70)
    print("Sedentary Detection Configuration")
    print("=" * 70)
    print(f"✓ Evaluation Window: Last 2 hours of activity data")
    print(f"✓ Sedentary Threshold: 70% sitting")
    print(f"✓ Activity Window: Each record = 2.5 seconds")
    print(f"✓ Cooldown Period: 3 hours between notifications")
    print(f"✓ Notification Type: sedentary_alert")
    print(f"✓ Trigger: Automatic on every sensor batch upload")
    print()
    print(f"📊 Architecture:")
    print(f"   1. ActivityService.analyze_sedentary_behavior()")
    print(f"      → Analyzes activity data and returns analysis results")
    print(f"   2. NotificationService.send_sedentary_alert(analysis_result)")
    print(f"      → Checks cooldown, creates DB record, sends FCM push")
    print("=" * 70)


def print_test_scenarios():
    """Print test scenarios"""
    print("\n" + "=" * 70)
    print("Test Scenarios")
    print("=" * 70)
    
    print("\n📊 Scenario 1: User has been sitting > 70% (last 2 hours)")
    print("   Expected: Notification sent via FCM")
    print("   Result: 'is_sedentary': true, 'notification_sent': true")
    
    print("\n📊 Scenario 2: User has been sitting < 70% (last 2 hours)")
    print("   Expected: No notification")
    print("   Result: 'is_sedentary': false, 'reason': 'not_sedentary'")
    
    print("\n📊 Scenario 3: Notification already sent in last 3 hours")
    print("   Expected: No notification (cooldown)")
    print("   Result: 'notification_sent': false, 'reason': 'cooldown_active'")
    
    print("\n📊 Scenario 4: Insufficient activity data (< 1.8 hours)")
    print("   Expected: No notification (unreliable)")
    print("   Result: 'notification_sent': false, 'reason': 'insufficient_data'")
    print("   Note: Requires at least 90% of evaluation period (1.8h of 2h)")
    
    print("\n📊 Scenario 5: No activity data in last 2 hours")
    print("   Expected: No evaluation")
    print("   Result: None")
    
    print("=" * 70)


def print_notification_payload_example():
    """Print example notification payload"""
    print("\n" + "=" * 70)
    print("Example Notification Payload (stored in DB)")
    print("=" * 70)
    
    example = {
        "sit_percentage": 75.5,
        "sit_hours": 1.51,
        "total_hours": 2.0,
        "evaluation_period_hours": 2,
        "timestamp": "2025-10-30T15:30:00Z"
    }
    
    print(json.dumps(example, indent=2))
    print("=" * 70)


def print_fcm_notification_example():
    """Print example FCM notification"""
    print("\n" + "=" * 70)
    print("Example FCM Push Notification")
    print("=" * 70)
    
    print("\n📱 Notification:")
    print("   Title: ⏰ ¡Hora de moverte!")
    print("   Body: Has estado sentado 1.5h (76%). ¡Levántate y camina un poco! 🚶")
    
    print("\n📦 Data Payload:")
    example_data = {
        "type": "sedentary_alert",
        "notification_id": "123",
        "sit_hours": "1.51",
        "sit_percentage": "75.5",
        "timestamp": "2025-10-30T15:30:00Z"
    }
    print(json.dumps(example_data, indent=2))
    
    print("\n🎨 Android Config:")
    print("   Priority: high")
    print("   Icon: ic_notification")
    print("   Color: #FF5722 (Orange)")
    print("   Channel: sedentary_alerts")
    
    print("=" * 70)


if __name__ == "__main__":
    print_sedentary_config()
    print_test_scenarios()
    print_notification_payload_example()
    print_fcm_notification_example()
    
    if ACCESS_TOKEN == "your_access_token_here":
        print("\n⚠️  WARNING: Please replace ACCESS_TOKEN with a valid JWT token")
        print("\nTo get a token:")
        print("1. Login via the mobile app")
        print("2. Copy the access token from the response")
        print("3. Update the ACCESS_TOKEN variable in this script")
    else:
        print("\n")
        test_sedentary_detection_on_batch()
        
    print("\n📝 Next Steps:")
    print("1. Ensure you have activity data in the last 2 hours")
    print("2. Make sure FCM token is registered for your user")
    print("3. Send a sensor batch to trigger evaluation")
    print("4. Check backend logs for evaluation results")
    print("5. Check your mobile device for push notifications")
