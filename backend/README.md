# Cinder's Soul Backend API

RESTful API backend for Cinder's Soul music streaming application.

## Tech Stack

- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: MySQL
- **Authentication**: JWT (JSON Web Tokens)
- **Social Auth**: Google ID Token
- **File Upload**: Multer

## Getting Started

### Prerequisites

- Node.js (v14 or higher)
- MySQL (v8.0 or higher)
- npm or yarn

### Installation

1. Install dependencies:
```bash
npm install
```

2. Configure environment variables:
```bash
cp .env.example .env
# Edit .env with your MySQL configuration
```

3. Create MySQL database:
```sql
CREATE DATABASE cinders_soul;
```

4. Run database migrations (see next section)

5. Start the server:
```bash
# Development mode with auto-reload
npm run dev

# Production mode
npm start
```

The server will start on `http://localhost:3000`

## Project Structure

```
backend/
├── src/
│   ├── config/          # Configuration files
│   │   └── database.js  # MySQL connection pool
│   ├── controllers/     # Request handlers
│   ├── middleware/      # Custom middleware
│   │   ├── auth.js      # JWT authentication
│   │   ├── errorHandler.js
│   │   ├── upload.js    # File upload configuration
│   │   └── validator.js
│   ├── models/          # Database models
│   ├── services/        # Business logic services
│   ├── routes/          # API routes
│   ├── utils/           # Shared helper utilities
│   ├── validators/      # Route validation rules
│   └── server.js        # App entry point
├── uploads/             # Uploaded files
├── .env.example         # Environment variables template
├── .gitignore
└── package.json
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/google` - Login/Register with Google ID token
- `POST /api/auth/refresh` - Refresh access token
- `GET /api/auth/me` - Get current user (protected)

### Users
- `GET /api/users/:id` - Get user by ID (protected)
- `PUT /api/users/:id` - Update user (protected)
- `DELETE /api/users/:id` - Delete user (protected)

### Songs
- `GET /api/songs` - Get all songs (with search/filter)
- `GET /api/songs/:id` - Get song by ID
- `POST /api/songs` - Create new song (protected)
- `PUT /api/songs/:id` - Update song (protected)
- `DELETE /api/songs/:id` - Delete song (protected)

### Playlists
- `GET /api/playlists` - Get user playlists (protected)
- `GET /api/playlists/:id` - Get playlist by ID (protected)
- `POST /api/playlists` - Create playlist (protected)
- `PUT /api/playlists/:id` - Update playlist (protected)
- `DELETE /api/playlists/:id` - Delete playlist (protected)
- `POST /api/playlists/:id/songs` - Add song to playlist (protected)
- `DELETE /api/playlists/:id/songs/:songId` - Remove song from playlist (protected)

### Albums
- `GET /api/albums` - Get all albums
- `GET /api/albums/:id` - Get album by ID
- `POST /api/albums` - Create album (protected)
- `PUT /api/albums/:id` - Update album (protected)
- `DELETE /api/albums/:id` - Delete album (protected)

### Artists
- `GET /api/artists` - Get all artists
- `GET /api/artists/:id` - Get artist by ID
- `POST /api/artists` - Create artist (protected)
- `PUT /api/artists/:id` - Update artist (protected)
- `DELETE /api/artists/:id` - Delete artist (protected)

### Favorites
- `GET /api/favorites` - Get user favorites (protected)
- `POST /api/favorites/:songId` - Add to favorites (protected)
- `DELETE /api/favorites/:songId` - Remove from favorites (protected)

### Upload
- `POST /api/upload/image` - Upload image file (protected)

### Health Check
- `GET /health` - API health status

## Environment Variables

Required for Google auth:
- `GOOGLE_CLIENT_ID` - One or many Google OAuth client IDs (comma-separated if multiple).

## Development

```bash
# Install dependencies
npm install

# Run in development mode with auto-reload
npm run dev

# Run in production mode
npm start
```

## License

ISC
