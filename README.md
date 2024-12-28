# MyMusicTracker

MyMusicTracker is a modern Android application where music lovers can track their listening habits, save their favorite genres and artists.

## Features

- 🎵 Music genre-based user profile
- 🔐 Multiple authentication options:
  - Email/Password registration
  - Google Sign-In
  - Phone number verification
- 🌍 Multi-language support:
  - English (default)
  - Turkish
  - Romanian
- 🎨 Modern and user-friendly interface
- 🔒 Secure data storage (Firebase)

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or higher
- JDK 11 or higher
- Android SDK API 33
- Firebase account

### Installation

1. Clone the repository:
```bash
git clone https://github.com/aydgnme/MyMusicTracker.git
```

2. Create a new project in Firebase Console and add the `google-services.json` file to the `app/` directory.

3. Enable the following providers in Firebase Authentication:
   - Email/Password
   - Google Sign-In
   - Phone Number

4. Enable Firebase Realtime Database and set up security rules.

5. Enable billing in your Firebase project (Blaze plan) for Phone Authentication.

### Building

Open the project in Android Studio and wait for Gradle synchronization. Then you can build and run the application.

## Architecture

- MVVM (Model-View-ViewModel) architectural pattern
- Firebase Authentication and Realtime Database
- Material Design 3 components
- ViewBinding
- AndroidX components

## Security

- Password requirements:
  - Minimum 8 characters
  - At least one uppercase letter
  - At least one lowercase letter
  - At least one number
  - At least one special character

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

Project Owner - [@aydgn_me](https://twitter.com/aydgn_me)

Project Link: [https://github.com/aydgnme/MyMusicTracker](https://github.com/aydgnme/MyMusicTracker) 
