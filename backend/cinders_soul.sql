-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: localhost:3307
-- Thời gian đã tạo: Th4 20, 2026 lúc 04:29 PM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `cinders_soul`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `albums`
--

CREATE TABLE `albums` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `artist_id` int(11) NOT NULL,
  `cover_url` varchar(512) DEFAULT NULL,
  `release_date` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `albums`
--

INSERT INTO `albums` (`id`, `title`, `artist_id`, `cover_url`, `release_date`, `created_at`, `updated_at`) VALUES
(1, 'Sky Tour', 1, 'https://upload.wikimedia.org/wikipedia/vi/b/bf/Sky_Tour.jpg', '2020-06-12', '2026-04-04 15:14:28', '2026-04-04 15:14:28'),
(2, 'Album I', 2, 'https://i.scdn.co/image/ab67616d0000b27383c8aaf6a46cdcb229ef2c8f', '2019-09-13', '2026-04-04 15:14:28', '2026-04-04 15:14:28');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `artists`
--

CREATE TABLE `artists` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `bio` text DEFAULT NULL,
  `avatar_url` varchar(512) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `artists`
--

INSERT INTO `artists` (`id`, `name`, `bio`, `avatar_url`, `created_at`, `updated_at`) VALUES
(1, 'Sơn Tùng M-TP', 'Ca sĩ, rapper, nhạc sĩ người Việt Nam', 'https://yt3.googleusercontent.com/c-Z7mIlntSpG6VyQ5ZqaPggqkZRhaySr-H5ZEazFN2iR1pP4eD1UGekwu0y--c4CSVhJJ1A4QT8=s900-c-k-c0x00ffffff-no-rj', '2026-04-04 15:14:28', '2026-04-04 15:14:28'),
(2, 'San Holo', 'Dutch DJ, musician, and record producer known for future bass and electronic music', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQXqtuc3xozWVmJwSOiFZXfd5Ni_rvTzcFxfA&s', '2026-04-04 15:14:28', '2026-04-04 15:14:28');

--
-- Cấu trúc bảng cho bảng `playlists`
--

CREATE TABLE `playlists` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `cover_url` varchar(512) DEFAULT NULL,
  `is_public` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `playlists`
--

INSERT INTO `playlists` (`id`, `user_id`, `name`, `description`, `cover_url`, `is_public`, `created_at`, `updated_at`) VALUES
(1, 1, 'Vietnamese Hits', 'Vietnamese and international songs synced from MySQL', NULL, 1, '2026-04-04 15:14:28', '2026-04-04 15:14:28'),
(2, 1, 'Electronic Vibes', 'Best electronic and future bass tracks', NULL, 1, '2026-04-04 15:14:28', '2026-04-04 15:14:28');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `playlist_songs`
--

CREATE TABLE `playlist_songs` (
  `playlist_id` int(11) NOT NULL,
  `song_id` int(11) NOT NULL,
  `position` int(11) NOT NULL,
  `added_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `playlist_songs`
--

INSERT INTO `playlist_songs` (`playlist_id`, `song_id`, `position`, `added_at`) VALUES
(1, 1, 1, '2026-04-04 15:14:28'),
(1, 2, 2, '2026-04-04 15:14:28'),
(2, 2, 1, '2026-04-04 15:14:28');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `refresh_tokens`
--

CREATE TABLE `refresh_tokens` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `token` varchar(512) NOT NULL,
  `expires_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `refresh_tokens`
--

INSERT INTO `refresh_tokens` (`id`, `user_id`, `token`, `expires_at`, `created_at`) VALUES
(1, 6, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NiwiZW1haWwiOiJ0aGVvbkBleGFtcGxlLmNvbSIsImlhdCI6MTc3NTM1NTY1MywiZXhwIjoxNzc1OTYwNDUzfQ.ktBxaorEsyZSp0ZUUToRdgtDkPdL8mkTeNLdSEfi8BQ', '2026-04-12 02:20:53', '2026-04-05 02:20:53'),
(2, 6, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NiwiZW1haWwiOiJ0aGVvbkBleGFtcGxlLmNvbSIsImlhdCI6MTc3NTM1NjA0MCwiZXhwIjoxNzc1OTYwODQwfQ.il5HTe02uBFCX8mnHUlrMKcZrgOS_aXuc4ecYuCx5Rc', '2026-04-12 02:27:20', '2026-04-05 02:27:20'),
(3, 6, 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NiwiZW1haWwiOiJ0aGVvbkBleGFtcGxlLmNvbSIsImlhdCI6MTc3NjU2ODIwNywiZXhwIjoxNzc3MTczMDA3fQ.VIDiafxYS188W1oDPnZJkzKy-du4gzdSppgx1bChuVo', '2026-04-26 03:10:07', '2026-04-19 03:10:07');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `songs`
--

CREATE TABLE `songs` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `artist_id` int(11) NOT NULL,
  `album_id` int(11) DEFAULT NULL,
  `audio_url` varchar(512) NOT NULL,
  `duration` int(11) NOT NULL COMMENT 'Duration in seconds',
  `cover_url` varchar(512) DEFAULT NULL,
  `genre` varchar(100) DEFAULT NULL,
  `lyrics` text DEFAULT NULL,
  `play_count` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `songs`
--

INSERT INTO `songs` (`id`, `title`, `artist_id`, `album_id`, `audio_url`, `duration`, `cover_url`, `genre`, `lyrics`, `play_count`, `created_at`, `updated_at`) VALUES
(1, 'Hãy Trao Cho Anh', 1, 1, 'https://github.com/cobalts-theon/music-data/raw/refs/heads/main/song_001.mp3', 240, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSAWde4SYTNZdLqtAcRCPs8pEsp2azAEN3jgA&s', 'Pop', NULL, 2, '2026-04-04 15:14:28', '2026-04-19 03:14:00'),
(2, 'I WANNA SHOW YOU', 2, 2, 'https://github.com/cobalts-theon/music-data/raw/refs/heads/main/song_002.mp3', 210, 'https://i.scdn.co/image/ab67616d0000b27383c8aaf6a46cdcb229ef2c8f', 'Electronic', NULL, 3, '2026-04-04 15:14:28', '2026-04-19 03:13:52');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `avatar_url` varchar(512) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Đang đổ dữ liệu cho bảng `users`
--

INSERT INTO `users` (`id`, `email`, `password_hash`, `display_name`, `avatar_url`, `created_at`, `updated_at`) VALUES
(1, 'test@example.com', '$2a$10$rYqZVpF0rNj0EKkqM7NqLO7QC9YwOZjF5cHFwIR1qKQqFX8hMKGwK', 'Test User', NULL, '2026-04-04 15:14:28', '2026-04-04 15:14:28'),
(2, 'admin@example.com', '$2a$10$rYqZVpF0rNj0EKkqM7NqLO7QC9YwOZjF5cHFwIR1qKQqFX8hMKGwK', 'Admin User', NULL, '2026-04-04 15:14:28', '2026-04-04 15:14:28'),
(6, 'theon@example.com', '$2a$10$d4o3q677LJ4GZRhHuAj.IOxAMoKUUvjC/XublRq7WwcuCEoG7VfKy', 'cobalt', NULL, '2026-04-05 02:20:53', '2026-04-05 02:42:32');

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `albums`
--
ALTER TABLE `albums`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_artist_id` (`artist_id`),
  ADD KEY `idx_title` (`title`);

--
-- Chỉ mục cho bảng `artists`
--
ALTER TABLE `artists`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_name` (`name`);

-- Chỉ mục cho bảng `playlists`
--
ALTER TABLE `playlists`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_name` (`name`);

--
-- Chỉ mục cho bảng `playlist_songs`
--
ALTER TABLE `playlist_songs`
  ADD PRIMARY KEY (`playlist_id`,`song_id`),
  ADD KEY `idx_playlist_id` (`playlist_id`),
  ADD KEY `idx_song_id` (`song_id`);

--
-- Chỉ mục cho bảng `refresh_tokens`
--
ALTER TABLE `refresh_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `token` (`token`),
  ADD KEY `idx_token` (`token`),
  ADD KEY `idx_user_id` (`user_id`);

--
-- Chỉ mục cho bảng `songs`
--
ALTER TABLE `songs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_artist_id` (`artist_id`),
  ADD KEY `idx_album_id` (`album_id`),
  ADD KEY `idx_title` (`title`),
  ADD KEY `idx_genre` (`genre`);
ALTER TABLE `songs` ADD FULLTEXT KEY `ft_title` (`title`);

--
-- Chỉ mục cho bảng `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_email` (`email`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `albums`
--
ALTER TABLE `albums`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `artists`
--
ALTER TABLE `artists`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `playlists`
--
ALTER TABLE `playlists`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `refresh_tokens`
--
ALTER TABLE `refresh_tokens`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT cho bảng `songs`
--
ALTER TABLE `songs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT cho bảng `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `albums`
--
ALTER TABLE `albums`
  ADD CONSTRAINT `albums_ibfk_1` FOREIGN KEY (`artist_id`) REFERENCES `artists` (`id`) ON DELETE CASCADE;

-- Các ràng buộc cho bảng `playlists`
--
ALTER TABLE `playlists`
  ADD CONSTRAINT `playlists_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `playlist_songs`
--
ALTER TABLE `playlist_songs`
  ADD CONSTRAINT `playlist_songs_ibfk_1` FOREIGN KEY (`playlist_id`) REFERENCES `playlists` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `playlist_songs_ibfk_2` FOREIGN KEY (`song_id`) REFERENCES `songs` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `refresh_tokens`
--
ALTER TABLE `refresh_tokens`
  ADD CONSTRAINT `refresh_tokens_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Các ràng buộc cho bảng `songs`
--
ALTER TABLE `songs`
  ADD CONSTRAINT `songs_ibfk_1` FOREIGN KEY (`artist_id`) REFERENCES `artists` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `songs_ibfk_2` FOREIGN KEY (`album_id`) REFERENCES `albums` (`id`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
