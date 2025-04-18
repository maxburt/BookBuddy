# BookBuddy 📚  
A no-nonsense ePub reader for Android that lets users browse, download, and read books with synced preferences and reviews.

## Demo Video  
👉 [Watch Demo Video](https://your-demo-video-link.com)  
(Replace with your actual demo video link)

## Features  
- Firebase login system  
- Browse and download books from a shared cloud store  
- Leave and view reviews with live average rating updates  
- Customize reading experience: font size, font type, light/dark mode  
- Offline EPUB reading support via WebView and `epub.js`  
- Simple top navigation between Library and Store  

## Tech Stack  
- **Language:** Kotlin  
- **Backend:** Firebase Firestore & Storage  
- **UI:** Jetpack ViewModel, LiveData, Navigation Component, WebView  
- **Reader:** Custom `reader.html` with `epub.js` integration  
- **Auth:** FirebaseUI

## How to Build

1. Clone this repository.
2. Open the project in Android Studio.
3. Add your own `google-services.json` file to the `app/` directory.  
   - You can generate this from your Firebase Console by registering an Android app.
4. Sync Gradle and build the project.
5. Run the app on an emulator or physical device.

> 🔐 This project does not include `google-services.json` for security reasons.

## Credits  
Created by Max Burt  
UT Austin | CS 371M Spring 2025
