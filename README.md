<h1 align="center">Cinder's Soul</h1>

<p align="center">
  A music streaming Android application with a Node.js and MySQL backend.
</p>

<p align="center">
  <img src="./backend/src/assets/CindersSoulslogo.png?raw=true" alt="Cinder's Soul logo" width="160" />
</p>

## Overview

Cinder's Soul is a music app built for browsing, streaming, saving, and managing songs, albums, artists, playlists, and user libraries. The Android app uses Jetpack Compose for the interface and ExoPlayer for playback. The backend provides REST APIs for authentication, music data, playlists, uploads, and admin management.

## Features

- Browse songs, artists, albums, genres, and curated collections.
- Search music from the Android app.
- Stream audio with Media3 ExoPlayer.
- Play, pause, seek, skip, shuffle, repeat, and control playback from notification controls.
- Create, edit, delete, and share playlists.
- Save favorite songs to a personal library.
- Download songs for local access.
- Sign in with email/password or Google.
- Reset password with email OTP.
- Update profile information and avatar.
- Admin panel for managing users, songs, artists, and albums.
- Offline cache for music metadata, downloads, and listening history.

## Tech Stack

### Android

- Kotlin
- Jetpack Compose
- Material 3
- Retrofit and OkHttp
- Room
- Media3 ExoPlayer
- Coil
- Kotlin Coroutines

### Backend

- Node.js
- Express.js
- Sequelize
- MySQL
- JWT authentication
- Google ID token authentication
- Multer file upload
- Nodemailer

## Project Structure

```text
.
├── app/                         Android application
│   └── src/main/java/com/example/cinderssoul/
│       ├── admin/               Admin screens and ViewModel
│       ├── local/               Room database and DAO
│       ├── models/              Android domain models
│       ├── network/             Retrofit client, API service, DTOs
│       ├── repository/          Data repositories
│       ├── ui/                  Compose UI screens and components
│       ├── MainActivity.kt      Main app entry point
│       └── MusicViewModel.kt    Main app state, player, and business logic
│
├── backend/                     Node.js backend
│   ├── src/
│   │   ├── controllers/         Request handlers
│   │   ├── middleware/          Auth, upload, validation, error handling
│   │   ├── models/              Sequelize models and relationships
│   │   ├── routes/              REST API routes
│   │   ├── services/            Auth, Google, and email services
│   │   └── server.js            Backend entry point
│   └── uploads/                 Uploaded files
│
├── gradle/                      Gradle wrapper files
├── build.gradle.kts             Root Gradle config
└── settings.gradle.kts          Gradle project settings
```

## Main Application Flow

```text
User action
  -> Jetpack Compose screen
  -> MusicViewModel or AdminViewModel
  -> Repository
  -> Retrofit ApiService
  -> Express route
  -> Controller / service
  -> Sequelize model
  -> MySQL
```

Playback state is handled separately through `MusicViewModel`, Media3 ExoPlayer, MediaSession, Android notifications, SharedPreferences, and Room cache.

## Backend Setup

Requirements:

- Node.js
- MySQL
- npm

Install dependencies:

```bash
cd backend
npm install
```

Create a MySQL database:

```sql
CREATE DATABASE cinders_soul;
```

Create `backend/.env` and configure the required values:

```env
PORT=3000
NODE_ENV=development

DB_HOST=localhost
DB_PORT=3306
DB_NAME=cinders_soul
DB_USER=root
DB_PASSWORD=your_password

JWT_SECRET=your_jwt_secret
JWT_REFRESH_SECRET=your_refresh_secret

GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_SERVER_CLIENT_ID=your_google_server_client_id

MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USER=your_email@example.com
MAIL_PASS=your_email_password
MAIL_FROM="Cinder's Soul" <your_email@example.com>
```

Start the backend:

```bash
npm run dev
```

Health check:

```bash
curl http://localhost:3000/health
```

## Android Setup

Requirements:

- Android Studio
- JDK 11 or newer
- Android SDK
- Running backend server

Build debug APK:

```bash
./gradlew assembleDebug
```

On Windows:

```bash
gradlew.bat assembleDebug
```

If the Android emulator needs to connect to a backend running on the host machine, use:

```env
BACKEND_BASE_URL=http://10.0.2.2:3000
```

For a real device, use the LAN IP address of the machine running the backend:

```env
BACKEND_BASE_URL=http://<your-lan-ip>:3000
```

These values can be placed in `backend/.env`, Gradle properties, or environment variables. The Android build reads backend-related values such as `BACKEND_BASE_URL`, `SHARE_BASE_URL`, `GOOGLE_CLIENT_ID`, and `GOOGLE_SERVER_CLIENT_ID`.

## Important API Areas

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/google`
- `POST /api/auth/refresh`
- `GET /api/auth/me`
- `GET /api/songs`
- `GET /api/artists`
- `GET /api/albums`
- `GET /api/playlists`
- `POST /api/playlists`
- `GET /api/library/songs`
- `POST /api/library/songs`
- `POST /api/upload/image`
- `GET /api/admin/summary`

## Notes

- The app supports guest usage and account-based libraries.
- Account library data is stored in MySQL through the backend.
- Playback cache, downloaded songs, and listening history are stored locally with Room.
- Admin access depends on the authenticated user's `role`.
- Uploaded files are served from the backend `/uploads` path.

## License

This project is for academic and development use.
