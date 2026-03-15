# MP Video Pro

A powerful, ad-free Android video player built with Kotlin and Jetpack Compose, featuring MX Player-like functionality with modern Material Design 3.

## Features

### Core Playback
- **Format Support**: MP4, MKV, AVI, MOV, FLV, WEBM
- **Hardware/Software Decoding**: ExoPlayer with automatic fallback
- **HD & 4K Support**: Smooth playback for high-resolution videos
- **Subtitle Support**: .srt, .ass, .ssa formats
- **Audio Track Switching**: Multiple audio tracks support
- **Playback Speed Control**: 0.25x to 2.0x speed adjustment

### Gesture Controls (MX Player-like)
- **Swipe Left/Right**: Seek video forward/backward
- **Swipe Up/Down (Left Side)**: Control brightness
- **Swipe Up/Down (Right Side)**: Control volume
- **Pinch Gesture**: Zoom video
- **Double Tap**: Play/pause
- **Long Press**: Temporary 2x speed

### User Interface
- **Material Design 3**: Modern, clean interface
- **Dark Mode**: Default dark theme
- **Minimal Player UI**: Clean controls overlay
- **Responsive Layout**: Works on all screen sizes

### File Management
- **Automatic Scanning**: Finds all videos on device
- **Grid View**: Thumbnail-based video browser
- **Sorting Options**: By name, date, duration, size
- **Recently Played**: Quick access to recent videos
- **Resume Playback**: Continue where you left off

### Advanced Features
- **Background Playback**: Continue audio when app is in background
- **Picture-in-Picture**: Floating video window
- **Lock Screen**: Prevent accidental touches
- **Playback History**: Track viewing progress
- **Settings**: Comprehensive preferences

## Architecture

### Module Structure
```
app/                 # Main application with UI
├── screens/         # Compose screens
├── components/      # Reusable UI components
├── viewmodel/       # ViewModels for state management
├── service/         # Background services
├── pip/            # Picture-in-Picture implementation
└── theme/          # Material Design 3 theme

player/             # Core video player functionality
├── VideoPlayer.kt  # ExoPlayer wrapper
├── gesture/        # Gesture control system
├── subtitle/       # Subtitle parsing and display
└── di/            # Dependency injection

storage/            # File scanning and database
├── MediaScanner.kt # Video file discovery
├── database/       # Room database
├── model/         # Data models
└── di/            # Dependency injection

common/             # Shared utilities and base classes
```

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Player Engine**: ExoPlayer
- **Database**: Room
- **Dependency Injection**: Hilt
- **Async**: Coroutines + Flow
- **Image Loading**: Coil

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- Android SDK API 24+ (Android 7.0)
- Kotlin 1.9.20+

### Building the Project

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd MP-Video-Pro
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the project directory

3. **Sync Gradle**
   - Android Studio will automatically sync the project
   - If needed, click "Sync Now" in the Gradle tab

4. **Build and Run**
   - Select an emulator or physical device
   - Click the Run button (or press Shift+F10)

### Dependencies

The project uses the following key dependencies:

**Core Android**
- `androidx.core:core-ktx`
- `androidx.lifecycle:lifecycle-*`
- `androidx.activity:activity-compose`

**Compose**
- `androidx.compose:compose-bom`
- `androidx.compose.ui:*`
- `androidx.compose.material3:*`
- `androidx.navigation:navigation-compose`

**ExoPlayer**
- `androidx.media3:media3-exoplayer`
- `androidx.media3:media3-ui`
- `androidx.media3:media3-session`

**Database**
- `androidx.room:room-*`

**Dependency Injection**
- `com.google.dagger:hilt-android`

**Image Loading**
- `io.coil-kt:coil`

## Permissions

The app requires the following permissions:

```xml
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.PICTURE_IN_PICTURE" />
```

## Key Components

### VideoPlayer.kt
Core wrapper around ExoPlayer providing:
- State management with Kotlin Flow
- Track selection (audio/subtitles)
- Playback speed control
- Error handling

### GestureController.kt
Implements MX Player-like gestures:
- Swipe detection for brightness/volume/seek
- Pinch-to-zoom
- Tap and double-tap handling
- Screen lock functionality

### MediaScanner.kt
Handles video file discovery:
- Android 10+ scoped storage support
- Thumbnail generation
- Metadata extraction
- Multiple sorting options

### PlayerScreen.kt
Main player UI with:
- Gesture integration
- Custom controls overlay
- Subtitle display
- Fullscreen support

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Troubleshooting

### Common Issues

**Build Errors**
- Ensure you have the latest Android Studio
- Update SDK tools in SDK Manager
- Clean and rebuild the project

**Permission Issues**
- Grant storage permissions when prompted
- Check app settings for permissions

**Playback Issues**
- Ensure video format is supported
- Check if hardware decoding is enabled in settings
- Try switching to software decoding

**Gesture Issues**
- Ensure gesture controls are enabled in settings
- Check if screen lock is active

### Performance Tips

- Enable hardware decoding for better performance
- Use software decoding for compatibility issues
- Close background apps for smoother playback
- Ensure sufficient storage space for thumbnails

## Roadmap

### Future Enhancements
- [ ] Network streaming support
- [ ] Chromecast integration
- [ ] Video editing features
- [ ] Cloud storage integration
- [ ] Playlist management
- [ ] Advanced subtitle customization
- [ ] Audio equalizer
- [ ] Video filters and effects

### Known Limitations
- No network streaming support yet
- Limited subtitle customization
- No playlist management
- Basic subtitle styling only

## Support

For issues and feature requests, please use the GitHub issue tracker.

---

**MP Video Pro** - A modern, powerful video player for Android.
