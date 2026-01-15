# HARbit - Human Activity Recognition & Health Tracking Platform

HARbit is a comprehensive activity tracking platform that combines Android mobile and Wear OS applications with a robust backend API to help users monitor and improve their physical habits through real-time activity recognition and health metrics analysis.

## 🎯 Overview

HARbit uses smartwatch sensors (accelerometer) to continuously monitor user activities, providing insights into sedentary behavior, active time, and overall health patterns. The system offers personalized feedback, progress tracking, and actionable recommendations to promote healthier lifestyles.

## 🏗️ Architecture

The project consists of three main components:

### 1. **Mobile App** (Android - Kotlin/Jetpack Compose)
User-facing application for viewing activity insights, and managing profile.

### 2. **Wear OS App** (Kotlin)
Companion smartwatch application for continuous sensor data collection and real-time activity monitoring.

### 3. **Backend API** (Python/FastAPI)
RESTful API handling authentication, data processing, activity classification, and analytics.

## ✨ Features

### Authentication & User Management
- 🔐 **Google Sign-In Integration**: Secure OAuth2 authentication
- 👤 **Profile Management**: Complete user profile with health metrics (height, weight, step goals)
- 🔒 **Privacy Controls**: Comprehensive privacy policy and data protection
- 🔑 **Session Management**: Secure token-based authentication with refresh tokens

### Activity Tracking & Recognition
- 📊 **Real-time Activity Classification**: Automatic recognition of activities (Sit, Walk, Type, Eat, Write, Workouts)
- ⌚ **Wear OS Integration**: Continuous sensor data collection at 20Hz
- 📈 **Activity Distribution**: Visual breakdown of daily activities with pie charts
- 🎯 **Smart Batching**: Efficient data transmission (~50KB batches) to minimize battery impact

### Progress & Insights
- 🏆 **Weekly Progress Reports**: Automated insights on activity patterns
- 💡 **Improvement Suggestions**: Recommendations for healthier habits
- 📝 **Motivational Messages**: Dynamic, personalized encouragement

### Data Visualization
- 📊 **Interactive Charts**: Activity distribution pie charts
- 🎨 **Color-coded Activities**: Distinct visual representation for each activity type
- 📅 **Historical Views**: Detailed history screens for all metrics
- 🔍 **Activity Legends**: Clear explanations with info popups

### Sensor Service Features
- 🔋 **Partial WakeLock**: Maintains CPU activity for consistent 20Hz sampling
- 📦 **Smart Batching**: Buffers sensor data before transmission
- 🔄 **Keep-Alive Mechanism**: Prevents process throttling
- 🎯 **Dual Sensor Support**: Accelerometer and gyroscope data collection
- 📡 **Wearable MessageClient**: Efficient data transmission to phone

### Backend Features
- 🔐 **JWT Authentication**: Secure token-based auth with refresh tokens
- 🗄️ **PostgreSQL Database**: Robust data persistence with SQLAlchemy ORM
- ⏰ **Scheduled Jobs**: Automated weekly progress calculation
- 🎯 **Activity Classification**: ML-ready activity recognition pipeline
- 📊 **Aggregation Queries**: Efficient data summarization for visualizations
- 🔄 **CORS Support**: Configured for mobile app integration

## 🛠️ Technology Stack

### Mobile App
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Hilt dependency injection
- **Navigation**: Navigation Compose
- **Networking**: Retrofit + OkHttp with interceptors
- **Async**: Kotlin Coroutines + Flow
- **Local Storage**: DataStore for token management
- **Auth**: Google Play Services Auth
- **Wearable**: Google Play Services Wearable API

### Wear OS App
- **Language**: Kotlin
- **UI**: Jetpack Compose for Wear OS
- **Sensors**: Android Sensor Framework (20Hz sampling)
- **Background**: Foreground Service with WakeLock
- **Communication**: Wearable MessageClient for phone sync

### Backend
- **Language**: Python 3.11+
- **Framework**: FastAPI with Pydantic validation
- **Database**: PostgreSQL with SQLAlchemy ORM
- **Authentication**: OAuth2 with JWT (PyJWT)
- **CORS**: FastAPI CORS middleware
- **Environment**: python-dotenv for configuration
