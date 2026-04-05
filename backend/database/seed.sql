-- Sample Data for Cinder's Soul Database
-- This file contains sample data for testing and development

-- Insert sample users (password is 'password123' hashed with bcrypt)
INSERT INTO `users` (`email`, `password_hash`, `display_name`, `avatar_url`) VALUES
('john@example.com', '$2a$10$YourHashedPasswordHere', 'John Doe', NULL),
('jane@example.com', '$2a$10$YourHashedPasswordHere', 'Jane Smith', NULL),
('test@example.com', '$2a$10$YourHashedPasswordHere', 'Test User', NULL);

-- Insert sample artists
INSERT INTO `artists` (`name`, `bio`, `avatar_url`) VALUES
('The Weeknd', 'Canadian singer, songwriter, and record producer', NULL),
('Taylor Swift', 'American singer-songwriter', NULL),
('Ed Sheeran', 'English singer-songwriter', NULL),
('Billie Eilish', 'American singer and songwriter', NULL),
('Drake', 'Canadian rapper, singer, and songwriter', NULL);

-- Insert sample albums
INSERT INTO `albums` (`title`, `artist_id`, `cover_url`, `release_date`) VALUES
('After Hours', 1, NULL, '2020-03-20'),
('1989', 2, NULL, '2014-10-27'),
('Divide', 3, NULL, '2017-03-03'),
('Happier Than Ever', 4, NULL, '2021-07-30'),
('Certified Lover Boy', 5, NULL, '2021-09-03');

-- Insert sample songs
INSERT INTO `songs` (`title`, `artist_id`, `album_id`, `audio_url`, `duration`, `cover_url`, `genre`) VALUES
('Blinding Lights', 1, 1, 'https://github.com/yourusername/music/raw/main/blinding-lights.mp3', 200, NULL, 'Pop'),
('Save Your Tears', 1, 1, 'https://github.com/yourusername/music/raw/main/save-your-tears.mp3', 215, NULL, 'Pop'),
('Shake It Off', 2, 2, 'https://github.com/yourusername/music/raw/main/shake-it-off.mp3', 219, NULL, 'Pop'),
('Shape of You', 3, 3, 'https://github.com/yourusername/music/raw/main/shape-of-you.mp3', 233, NULL, 'Pop'),
('Happier Than Ever', 4, 4, 'https://github.com/yourusername/music/raw/main/happier.mp3', 298, NULL, 'Alternative'),
('Bad Guy', 4, NULL, 'https://github.com/yourusername/music/raw/main/bad-guy.mp3', 194, NULL, 'Alternative'),
('Way 2 Sexy', 5, 5, 'https://github.com/yourusername/music/raw/main/way2sexy.mp3', 257, NULL, 'Hip-Hop');

-- Insert sample playlists (for user_id = 1)
INSERT INTO `playlists` (`user_id`, `name`, `description`, `is_public`) VALUES
(1, 'My Favorites', 'My all-time favorite songs', TRUE),
(1, 'Workout Mix', 'High energy songs for workout', TRUE),
(2, 'Chill Vibes', 'Relaxing music for studying', FALSE);

-- Insert songs into playlists
INSERT INTO `playlist_songs` (`playlist_id`, `song_id`, `position`) VALUES
(1, 1, 1),
(1, 3, 2),
(1, 4, 3),
(2, 1, 1),
(2, 7, 2),
(3, 5, 1),
(3, 6, 2);

-- Insert sample favorites (for user_id = 1)
INSERT INTO `favorites` (`user_id`, `song_id`) VALUES
(1, 1),
(1, 3),
(1, 4),
(1, 6),
(2, 1),
(2, 5);
