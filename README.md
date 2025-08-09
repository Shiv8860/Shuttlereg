# 🏸 ShuttleReg - Badminton Tournament Registration App

A comprehensive Android application for badminton tournament registration featuring automated age-based event selection, photo capture, payment integration, PDF generation, and real-time tournament updates.

## 🚀 Features

### ✅ Core Functionality
- **Age-Based Event Selection**: Automatic eligibility calculation (U-9 through Open categories)
- **Multi-Event Registration**: Singles, Doubles, Mixed Doubles with partner management
- **Photo Management**: Camera/gallery capture, compression, Firebase Storage upload
- **Payment Integration**: Razorpay gateway with receipt generation
- **PDF Generation**: Auto-filled tournament forms with photos
- **Real-time Updates**: Live draws, scores, and announcements
- **Admin Dashboard**: Separate app for tournament organizers

### 🎯 Age Categories Logic
```
Under 9: born on or after 01.01.2016
Under 11: born on or after 01.01.2014
Under 13: born on or after 01.01.2012
Under 15: born on or after 01.01.2010
Under 17: born on or after 01.01.2008
Under 19: born on or after 01.01.2006
Men's/Women's Open: 18+ years
```

## 🛠️ Technical Stack

- **Frontend**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture + MVVM
- **Backend**: Firebase (Auth, Firestore, Storage, Messaging)
- **Payment**: Razorpay Integration
- **PDF**: iText7 Library
- **Photos**: CameraX + Coil
- **Local Storage**: Room Database
- **Export**: Apache POI (Excel)

## 📁 Project Structure

```
ShuttleReg/
├── 📄 ARCHITECTURE.md          # Complete system architecture
├── 📄 IMPLEMENTATION_GUIDE.md  # Core feature implementations
├── 📄 PHOTO_IMPLEMENTATION.md  # Photo management system
├── 📄 PROJECT_SUMMARY.md       # Project overview document
├── 📱 app/                     # Main Android application
│   ├── src/main/java/com/shuttlereg/
│   │   ├── ui/                 # Jetpack Compose UI components
│   │   ├── data/              # Repository & data sources
│   │   ├── domain/            # Business logic & use cases
│   │   └── di/                # Dependency injection
│   └── src/main/res/          # Resources (layouts, strings, etc.)
├── 🎨 Design Assets/          # UI mockups and icons
└── 📋 build.gradle.kts        # Project configuration
```

## 🚦 Getting Started

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin 1.9.0+

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Shiv8860/Shuttlereg.git
   ```

2. Open in Android Studio

3. Set up Firebase:
   - Create a new Firebase project
   - Add `google-services.json` to `app/` directory
   - Enable Authentication, Firestore, and Storage

4. Configure Razorpay:
   - Get API keys from Razorpay dashboard
   - Add keys to `local.properties`

5. Build and run the project

## 📖 Documentation

- **[ARCHITECTURE.md](ARCHITECTURE.md)**: Complete system architecture and design patterns
- **[IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md)**: Core feature implementations and code examples
- **[PHOTO_IMPLEMENTATION.md](PHOTO_IMPLEMENTATION.md)**: Detailed photo management system
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)**: Comprehensive project overview

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact

**Shiv Kumar** - [@Shiv8860](https://github.com/Shiv8860)

Project Link: [https://github.com/Shiv8860/Shuttlereg](https://github.com/Shiv8860/Shuttlereg)

---

**Note**: This is a comprehensive badminton tournament management system designed for real-world tournament operations with robust photo handling, payment processing, and administrative features.