# 🔥 Firebase Setup Guide for ShuttleReg

## 📋 **Prerequisites**
- Google account
- Android Studio with ShuttleReg project open
- Firebase CLI (optional but recommended)

## 🚀 **Step 1: Create Firebase Project**

1. **Go to [Firebase Console](https://console.firebase.google.com)**
2. **Click "Add project"**
3. **Project Setup:**
   - **Project name**: `ShuttleReg-Production` (or your preferred name)
   - **Google Analytics**: Enable (recommended for user tracking)
   - **Analytics location**: Select your country
   - **Create project**

## 🔧 **Step 2: Add Android App to Firebase**

1. **In Firebase Console, click "Add app" → Android**
2. **App Configuration:**
   - **Package name**: `com.example.shuttlereg`
   - **App nickname**: `ShuttleReg Android App`
   - **Debug signing certificate SHA-1**: 
     ```bash
     # Run this command in Android Studio terminal:
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
3. **Download `google-services.json`**
4. **Replace the existing file** in `/app/google-services.json`

## 🔐 **Step 3: Configure Firebase Authentication**

### **Enable Authentication Providers:**

1. **Go to Authentication → Sign-in method**
2. **Enable the following providers:**

   **Email/Password:**
   - ✅ Enable Email/Password
   - ✅ Enable Email link (passwordless sign-in) - Optional
   
   **Google Sign-In:** (Recommended)
   - ✅ Enable Google
   - **Web SDK configuration**: Auto-generated
   
   **Phone:** (Optional for future)
   - Enable if you want phone-based registration

### **Configure Authentication Settings:**
```javascript
// Authorized domains (add your domains here):
// - localhost (for testing)
// - your-app-domain.com (for production)
// - shuttlereg.web.app (if using Firebase Hosting)
```

## 🗄️ **Step 4: Set up Firestore Database**

### **Create Database:**
1. **Go to Firestore Database → Create database**
2. **Security Rules**: Start in **test mode** (for development)
3. **Location**: Choose closest region to your users

### **Database Structure:**
```javascript
// Firestore Collections Structure
tournaments/
  ├── {tournamentId}/
      ├── name: string
      ├── description: string
      ├── startDate: timestamp
      ├── endDate: timestamp
      ├── venue: string
      ├── availableEvents: array
      ├── eventPrices: map
      └── isActive: boolean

users/
  ├── {userId}/
      ├── fullName: string
      ├── email: string
      ├── phone: string
      ├── dateOfBirth: timestamp
      ├── gender: string
      ├── clubName: string
      ├── profilePhotoUrl: string
      └── eligibleEvents: array

registrations/
  ├── {registrationId}/
      ├── userId: string
      ├── tournamentId: string
      ├── selectedEvents: array
      ├── totalAmount: number
      ├── paymentStatus: string
      ├── paymentId: string
      ├── registrationDate: timestamp
      └── status: string

matches/
  ├── {matchId}/
      ├── tournamentId: string
      ├── eventCategory: string
      ├── eventType: string
      ├── player1Id: string
      ├── player2Id: string
      ├── scheduledDateTime: timestamp
      ├── courtNumber: number
      ├── status: string
      ├── finalScore: string
      └── result: string
```

### **Security Rules (Initial - For Development):**
```javascript
// Go to Firestore → Rules and replace with:
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read/write access to all documents
    // TODO: Implement proper security rules for production
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 📁 **Step 5: Configure Firebase Storage**

### **Set up Storage:**
1. **Go to Storage → Get started**
2. **Security Rules**: Start in **test mode**
3. **Location**: Same as Firestore

### **Storage Structure:**
```
gs://your-project.appspot.com/
├── profile-photos/
│   └── {userId}/
│       └── profile.jpg
├── tournament-banners/
│   └── {tournamentId}/
│       └── banner.jpg
└── registration-pdfs/
    └── {registrationId}/
        └── registration-form.pdf
```

### **Storage Security Rules:**
```javascript
// Go to Storage → Rules and replace with:
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /profile-photos/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /tournament-banners/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null; // Only authenticated users can upload
    }
    match /registration-pdfs/{registrationId}/{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## 🔑 **Step 6: Configure API Keys & Environment**

### **Update `local.properties`:**
```properties
# Add to /local.properties
sdk.dir=/opt/android-sdk

# Firebase Configuration
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_API_KEY=your-web-api-key
FIREBASE_AUTH_DOMAIN=your-project.firebaseapp.com
FIREBASE_STORAGE_BUCKET=your-project.appspot.com

# Razorpay Configuration (Get from Razorpay Dashboard)
RAZORPAY_KEY_ID=rzp_test_your_key_id
RAZORPAY_KEY_SECRET=your_key_secret
```

### **Create `secrets.xml` for API Keys:**
```xml
<!-- Create /app/src/main/res/values/secrets.xml -->
<resources>
    <!-- These will be replaced by build system -->
    <string name="razorpay_key_id">YOUR_RAZORPAY_KEY_ID</string>
    <string name="firebase_web_api_key">YOUR_FIREBASE_WEB_API_KEY</string>
</resources>
```

## 📱 **Step 7: Test Firebase Integration**

### **Test Authentication:**
1. **Run the app**
2. **Try signup with email/password**
3. **Check Firebase Console → Authentication → Users**
4. **Verify user appears in the list**

### **Test Firestore:**
1. **Complete user registration**
2. **Check Firestore Console**
3. **Verify user document is created**

### **Test Storage:**
1. **Upload a profile photo (when feature is ready)**
2. **Check Storage Console**
3. **Verify file upload**

## 🔒 **Step 8: Production Security Rules**

### **Firestore Production Rules:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Anyone can read tournaments
    match /tournaments/{tournamentId} {
      allow read: if true;
      allow write: if request.auth != null && isAdmin();
    }
    
    // Users can read/write their own registrations
    match /registrations/{registrationId} {
      allow read, write: if request.auth != null && 
        (resource.data.userId == request.auth.uid || isAdmin());
    }
    
    // Matches are readable by participants and admins
    match /matches/{matchId} {
      allow read: if request.auth != null;
      allow write: if isAdmin();
    }
    
    function isAdmin() {
      return request.auth != null && 
        exists(/databases/$(database)/documents/admins/$(request.auth.uid));
    }
  }
}
```

## 🎯 **Step 9: Add Sample Data**

### **Sample Tournament Data:**
```json
{
  "id": "tournament_2024_summer",
  "name": "Summer Badminton Championship 2024",
  "description": "Annual badminton tournament featuring all age categories",
  "startDate": "2024-08-15T00:00:00Z",
  "endDate": "2024-08-17T00:00:00Z",
  "venue": "City Sports Complex, Main Hall",
  "availableEvents": ["U9", "U11", "U13", "U15", "U17", "U19", "MENS_OPEN", "WOMENS_OPEN"],
  "eventPrices": {
    "U9_SINGLES": 300,
    "U9_DOUBLES": 500,
    "U11_SINGLES": 400,
    "U11_DOUBLES": 600,
    "U13_SINGLES": 500,
    "U13_DOUBLES": 700,
    "U15_SINGLES": 600,
    "U15_DOUBLES": 800,
    "U17_SINGLES": 700,
    "U17_DOUBLES": 900,
    "U19_SINGLES": 800,
    "U19_DOUBLES": 1000,
    "MENS_OPEN_SINGLES": 1000,
    "MENS_OPEN_DOUBLES": 1200,
    "MENS_OPEN_MIXED_DOUBLES": 1200,
    "WOMENS_OPEN_SINGLES": 1000,
    "WOMENS_OPEN_DOUBLES": 1200,
    "WOMENS_OPEN_MIXED_DOUBLES": 1200
  },
  "registrationDeadline": "2024-08-10T23:59:59Z",
  "maxParticipants": 200,
  "currentParticipants": 0,
  "isActive": true
}
```

## ✅ **Step 10: Verification Checklist**

- [ ] Firebase project created
- [ ] Android app added to Firebase
- [ ] `google-services.json` updated
- [ ] Authentication enabled (Email/Password)
- [ ] Firestore database created
- [ ] Storage bucket configured
- [ ] Security rules updated
- [ ] API keys configured
- [ ] Sample data added
- [ ] App builds successfully
- [ ] Authentication test passed
- [ ] Firestore read/write test passed

## 🚨 **Important Notes**

1. **Security**: Update security rules before production deployment
2. **Billing**: Monitor Firebase usage to avoid unexpected charges
3. **Backup**: Set up regular Firestore backups
4. **Performance**: Enable Performance Monitoring in Firebase
5. **Analytics**: Review Analytics data for user insights

---

**Next Step**: After completing Firebase setup, we'll integrate Razorpay payment processing and add comprehensive testing!