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
1. Clone the repo:  
   `git clone https://github.com/utap-s25/BookBuddy.git`
2. Open in Android Studio (Giraffe or newer)
3. Sync Gradle and Build Project  
4. Run the app on emulator or device  

> Note: The required Firebase setup (Firestore collections, Storage, Auth) is already configured. You **do not** need to provide a new `google-services.json` — it’s included in the repo.

## Credits  
Created by Max Burt  
UT Austin | CS 371M Spring 2025
