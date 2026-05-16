-- Migration for the Room/MySQL split.
-- MySQL keeps shared online data: users, artists, albums, songs, playlists.
-- Android Room keeps personal offline data: favorites, listening history, downloads.

DROP TABLE IF EXISTS `favorites`;

UPDATE `playlists`
SET
  `name` = 'Vietnamese Hits',
  `description` = 'Vietnamese and international songs synced from MySQL'
WHERE `name` IN ('My Favorites', 'Favorite', 'Favorites');
