# Database Setup Guide

## Prerequisites

- MySQL Server 8.0 or higher installed
- MySQL command-line client or MySQL Workbench

## Setup Instructions

### 1. Create Database

Open MySQL client and run:

```sql
CREATE DATABASE cinders_soul CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Run Schema Migration

From the `backend` directory:

```bash
# Using MySQL command line
mysql -u root -p cinders_soul < database/schema.sql

# Or using MySQL Workbench
# 1. Open MySQL Workbench
# 2. Connect to your server
# 3. File > Run SQL Script
# 4. Select database/schema.sql
```

### 3. (Optional) Load Sample Data

To load sample data for testing:

```bash
mysql -u root -p cinders_soul < database/seed.sql
```

**Note**: You need to update the password hashes in `seed.sql` with actual bcrypt hashes before using.

### 4. Configure Backend

Update your `.env` file with your MySQL credentials:

```env
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=your_mysql_password
DB_NAME=cinders_soul
DB_PORT=3306
```

### 5. Verify Connection

Start the backend server:

```bash
npm run dev
```

You should see:
```
✅ Database connected successfully
🚀 Server is running on port 3000
```

## Database Schema Overview

### Tables

1. **users** - User accounts with authentication
2. **refresh_tokens** - JWT refresh tokens for session management
3. **artists** - Music artists
4. **albums** - Music albums
5. **songs** - Individual songs with metadata
6. **playlists** - User-created playlists
7. **playlist_songs** - Junction table for playlist-song relationships
8. **favorites** - User favorite songs

### Relationships

```
users
├─ playlists (one-to-many)
├─ favorites (one-to-many)
└─ refresh_tokens (one-to-many)

artists
├─ albums (one-to-many)
└─ songs (one-to-many)

albums
└─ songs (one-to-many)

playlists
└─ playlist_songs (one-to-many)

songs
├─ playlist_songs (one-to-many)
└─ favorites (one-to-many)
```

## Common MySQL Commands

```sql
-- Show all tables
SHOW TABLES;

-- Describe table structure
DESCRIBE users;

-- Check data
SELECT * FROM users LIMIT 10;

-- Drop database (CAUTION: deletes all data)
DROP DATABASE cinders_soul;

-- Backup database
mysqldump -u root -p cinders_soul > backup.sql

-- Restore database
mysql -u root -p cinders_soul < backup.sql
```

## Troubleshooting

### Connection Failed

1. Check MySQL service is running
2. Verify credentials in `.env`
3. Ensure database exists: `SHOW DATABASES;`

### Permission Denied

Grant privileges to your user:

```sql
GRANT ALL PRIVILEGES ON cinders_soul.* TO 'your_user'@'localhost';
FLUSH PRIVILEGES;
```

### Character Encoding Issues

Ensure database uses UTF8MB4:

```sql
ALTER DATABASE cinders_soul CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
```
