# HARbit Mobile App

HARbit is a mobile application designed to help users monitor and improve their physical habits through smartwatch integration and activity tracking.

## Features

### Authentication & Onboarding
- **Welcome Screen**: Introduction with Google Sign-In integration
- **Profile Completion**: User data collection form
- **Privacy Policy**: Data protection information
- **Smartwatch Setup**: Instructions for optimal device placement

### Main Application
- **Activity Dashboard**: Daily activity distribution with pie chart visualization
- **Health Metrics**: Steps count and heart rate monitoring
- **Progress Tracking**: Weekly achievements and improvement suggestions
- **Profile Management**: User preferences and settings

### Detailed Views
- **Activity Distribution Details**: Historical activity breakdown
- **Heart Rate History**: Detailed cardiovascular metrics with charts
- **Steps History**: Daily step count tracking and summaries

## Project Structure

```
mobile/src/main/java/com/example/harbit/
├── MainActivity.kt                          # Main entry point
├── ui/
│   ├── theme/                              # App theming and colors
│   │   ├── Color.kt                        # Color definitions
│   │   ├── Theme.kt                        # Material Design theme
│   │   └── Type.kt                         # Typography styles
│   ├── components/                         # Reusable UI components
│   │   └── BottomNavigation.kt             # Bottom navigation bar
│   ├── navigation/                         # Navigation logic
│   │   └── AppNavigation.kt                # Navigation graph
│   └── screens/                           # All app screens
│       ├── auth/                          # Authentication flow
│       │   ├── WelcomeScreen.kt           # Landing page
│       │   ├── ProfileCompletionScreen.kt  # Profile setup
│       │   └── PrivacyPolicyScreen.kt     # Privacy policy
│       ├── onboarding/                    # Setup flow
│       │   └── SmartwatchSetupScreen.kt   # Device instructions
│       ├── dashboard/                     # Main dashboard
│       │   └── DashboardScreen.kt         # Activity overview
│       ├── details/                       # Detail screens
│       │   ├── ActivityDistributionDetailScreen.kt
│       │   ├── HeartRateDetailScreen.kt
│       │   └── StepsDetailScreen.kt
│       ├── progress/                      # Progress tracking
│       │   └── ProgressScreen.kt          # Achievements & suggestions
│       └── profile/                       # User profile
│           └── ProfileScreen.kt           # Profile management
```

## User Stories Implementation

The app implements all the user stories from the provided specification:

- **HU0001-HU0002**: Authentication via Google Sign-In
- **HU0003**: Smartwatch synchronization UI
- **HU0004-HU0005**: Daily activity and physiological data visualization
- **HU0006**: Inactivity period notifications (UI components ready)
- **HU0007-HU008**: Profile and preferences management
- **HU0009-HU0011**: Historical data visualization
- **HU0012-HU0014**: Progress tracking with achievements
- **HU015**: Daily step goal setting
- **HU016**: Smartwatch positioning instructions
- **HU017**: Privacy policy access

## Technology Stack

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern UI toolkit
- **Material Design 3**: UI design system
- **Navigation Compose**: Screen navigation
- **Play Services Wearable**: Smartwatch integration
- **Gradle**: Build system

## Design System

### Colors
- **Primary Teal**: #2E8B8B (HARbit brand color)
- **Activity Colors**: Distinct colors for different activity types
- **Status Colors**: Success (green), warning (orange), error (red)

### Typography
- Material Design 3 typography scale
- Emphasis on readability and accessibility

### Components
- Custom bottom navigation with 3 main sections
- Consistent card-based layout
- Interactive charts and visualizations

## Build Instructions

1. **Prerequisites**:
   - Android Studio with Kotlin support
   - Android SDK 30 or higher
   - Gradle 8.0+

2. **Dependencies**:
   - All dependencies are managed through `build.gradle.kts`
   - Includes Compose BOM, Navigation, Google Play Services

3. **Build**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run**:
   - Connect Android device or start emulator
   - Run from Android Studio or use `./gradlew installDebug`

## Future Enhancements

- **Backend Integration**: Connect with health data APIs
- **Real-time Notifications**: Implement push notifications for inactivity alerts
- **Data Analytics**: Advanced health metrics analysis
- **Social Features**: Community challenges and sharing
- **Machine Learning**: Personalized recommendations
- **Wear OS App**: Companion smartwatch application

## Notes

- The current implementation focuses on UI/UX with mock data
- Authentication integration requires Google Services configuration
- Smartwatch integration is prepared but requires Wear OS companion app
- Charts use simplified Canvas implementations (can be enhanced with charting libraries)