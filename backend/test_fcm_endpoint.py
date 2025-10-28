"""
Test script for FCM token endpoint

This script tests the FCM token update endpoint.
Make sure the backend server is running before executing this script.

Usage:
    python test_fcm_endpoint.py

Requirements:
    - Backend server running on http://localhost:8000
    - Valid JWT access token
"""

import requests
import json

# Configuration
BASE_URL = "http://localhost:8000"
ACCESS_TOKEN = "your_access_token_here"  # Replace with a valid token

# Test FCM token (fake for testing)
TEST_FCM_TOKEN = "dQw4w9WgXcQ:APA91bF_test_token_1234567890_abcdefghijklmnopqrstuvwxyz"

def test_update_fcm_token():
    """Test the PATCH /users/fcm-token endpoint"""
    
    print("=" * 60)
    print("Testing FCM Token Update Endpoint")
    print("=" * 60)
    
    url = f"{BASE_URL}/users/fcm-token"
    headers = {
        "Authorization": f"Bearer {ACCESS_TOKEN}",
        "Content-Type": "application/json"
    }
    
    payload = {
        "fcmToken": TEST_FCM_TOKEN
    }
    
    print(f"\nURL: {url}")
    print(f"Payload: {json.dumps(payload, indent=2)}")
    print(f"Token (first 50 chars): {ACCESS_TOKEN[:50]}...")
    
    try:
        print("\nSending request...")
        response = requests.patch(url, headers=headers, json=payload)
        
        print(f"\nStatus Code: {response.status_code}")
        print(f"Response: {json.dumps(response.json(), indent=2)}")
        
        if response.status_code == 200:
            print("\n✅ SUCCESS: FCM token updated successfully!")
        else:
            print(f"\n❌ FAILED: {response.json().get('detail', 'Unknown error')}")
            
    except requests.exceptions.ConnectionError:
        print("\n❌ ERROR: Could not connect to backend server.")
        print("Make sure the backend is running on http://localhost:8000")
    except Exception as e:
        print(f"\n❌ ERROR: {str(e)}")

if __name__ == "__main__":
    if ACCESS_TOKEN == "your_access_token_here":
        print("⚠️  WARNING: Please replace ACCESS_TOKEN with a valid JWT token")
        print("\nTo get a token:")
        print("1. Login via the mobile app or web")
        print("2. Copy the access token from the response")
        print("3. Update the ACCESS_TOKEN variable in this script")
    else:
        test_update_fcm_token()
