# TurfMatch ⚽🏏🏸🏀🎾

TurfMatch is a community sports turf-finder and pick-up game organizer Android application built with Java and traditional Android Views (XML layouts + Activities/Fragments).

---

## Setup & Configuration Guide

### 1. Firebase Configuration (`google-services.json`)
This project requires Firebase Authentication and Firebase Realtime Database/Firestore for user profiles, game creation, and roster joining.
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new project named **TurfMatch**.
3. Add an **Android app** with package name: `com.turfmatch.app`.
4. Download the generated `google-services.json` file.
5. Place `google-services.json` into the root of the `app/` directory (`/app/google-services.json`).
6. Enable **Email/Password Authentication** in the Firebase Console.
7. Enable **Realtime Database** (in test mode or with read/write rules).

### 2. Google Maps API Key Setup
To render the Google Maps turf locator and navigation features:
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Enable the **Maps SDK for Android**.
3. Create an API Key.
4. Open `AndroidManifest.xml` and replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your actual API key:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="AIzaSyYourActualApiKeyHere" />
   ```

### 3. Running the App
1. Open the project in **Android Studio (Hedgehog / Iguana / Jellyfish or newer)**.
2. Ensure Android SDK 34 and Build Tools are installed.
3. Sync project with Gradle files.
4. Run the app on an Android Emulator or physical device (API 24+).

---

## Syllabus Course Outcomes (CO) Mapping

- **CO1 - UI Controls & Match Creation**: Implemented in `CreateGameActivity` with `Spinner`, `RadioGroup`, `DatePickerDialog`, and `TimePickerDialog`.
- **CO2 - File Handling & Camera Intent**: Implemented in `ProfileActivity` (camera implicit intent for profile/turf photo) and `MatchHistoryActivity` (CSV/TXT file export to external app storage).
- **CO3 - Firebase CRUD & Google Maps Location**: Implemented in `FirebaseGameRepository` (Insert, Read, Update, Delete for Users and Games) and `TurfMapActivity` (Google Maps SDK, FusedLocationProviderClient, and `geo:` navigation intents).
- **CO6 - BroadcastReceiver Offline Alerts**: Implemented in `NetworkReceiver` monitoring connectivity/airplane mode changes with `AlertDialog` warnings.
