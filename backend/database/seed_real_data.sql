-- Real Music Data from Firestore Backup
-- This file contains actual artists and songs data

-- Clear existing sample data
DELETE FROM `favorites`;
DELETE FROM `playlist_songs`;
DELETE FROM `playlists`;
DELETE FROM `songs`;
DELETE FROM `albums`;
DELETE FROM `artists`;
DELETE FROM `refresh_tokens`;
DELETE FROM `users`;

-- Insert real artists
INSERT INTO `artists` (`id`, `name`, `bio`, `avatar_url`) VALUES
(1, 'Sơn Tùng M-TP', 'Ca sĩ, rapper, nhạc sĩ người Việt Nam', 'https://yt3.googleusercontent.com/c-Z7mIlntSpG6VyQ5ZqaPggqkZRhaySr-H5ZEazFN2iR1pP4eD1UGekwu0y--c4CSVhJJ1A4QT8=s900-c-k-c0x00ffffff-no-rj'),
(2, 'San Holo', 'Dutch DJ, musician, and record producer known for future bass and electronic music', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQXqtuc3xozWVmJwSOiFZXfd5Ni_rvTzcFxfA&s');

-- Insert real albums
INSERT INTO `albums` (`id`, `title`, `artist_id`, `cover_url`, `release_date`) VALUES
(1, 'Sky Tour', 1, 'https://upload.wikimedia.org/wikipedia/vi/b/bf/Sky_Tour.jpg', '2020-06-12'),
(2, 'Album I', 2, 'https://i.scdn.co/image/ab67616d0000b27383c8aaf6a46cdcb229ef2c8f', '2019-09-13');

-- Insert real songs
INSERT INTO `songs` (`id`, `title`, `artist_id`, `album_id`, `audio_url`, `duration`, `cover_url`, `genre`) VALUES
(1, 'Hãy Trao Cho Anh', 1, 1, 'https://github.com/cobalts-theon/music-data/raw/refs/heads/main/song_001.mp3', 240, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSAWde4SYTNZdLqtAcRCPs8pEsp2azAEN3jgA&s', 'Pop'),
(2, 'I WANNA SHOW YOU', 2, 2, 'https://github.com/cobalts-theon/music-data/raw/refs/heads/main/song_002.mp3', 210, 'https://i.scdn.co/image/ab67616d0000b27383c8aaf6a46cdcb229ef2c8f', 'Electronic');

-- Insert sample users for testing
-- Password for all users: password123
-- Note: You need to generate actual bcrypt hashes using Node.js
-- Run: node -e "console.log(require('bcryptjs').hashSync('password123', 10))"
INSERT INTO `users` (`id`, `email`, `password_hash`, `display_name`, `avatar_url`) VALUES
(1, 'test@example.com', '$2a$10$rYqZVpF0rNj0EKkqM7NqLO7QC9YwOZjF5cHFwIR1qKQqFX8hMKGwK', 'Test User', NULL),
(2, 'admin@example.com', '$2a$10$rYqZVpF0rNj0EKkqM7NqLO7QC9YwOZjF5cHFwIR1qKQqFX8hMKGwK', 'Admin User', NULL);

-- Insert sample playlists
INSERT INTO `playlists` (`id`, `user_id`, `name`, `description`, `is_public`) VALUES
(1, 1, 'My Favorites', 'My favorite Vietnamese and international songs', TRUE),
(2, 1, 'Electronic Vibes', 'Best electronic and future bass tracks', TRUE);

-- Add songs to playlists
INSERT INTO `playlist_songs` (`playlist_id`, `song_id`, `position`) VALUES
(1, 1, 1),
(1, 2, 2),
(2, 2, 1);

-- Add some favorites
INSERT INTO `favorites` (`user_id`, `song_id`) VALUES
(1, 1),
(1, 2),
(2, 1);
