# Sequelize Models Documentation

## Cấu trúc Models

Tất cả các models được định nghĩa bằng Sequelize ORM cho MySQL database.

### Models Available

1. **User** - Người dùng hệ thống
2. **Artist** - Nghệ sĩ
3. **Album** - Album nhạc
4. **Song** - Bài hát
5. **Playlist** - Playlist của user
6. **PlaylistSong** - Quan hệ giữa playlist và bài hát
7. **Favorite** - Bài hát yêu thích của user
8. **RefreshToken** - JWT refresh tokens

## Cách sử dụng

### Import Models

```javascript
// Import tất cả models
const { 
  User, 
  Artist, 
  Album, 
  Song, 
  Playlist, 
  Favorite 
} = require('./models');

// Hoặc import riêng lẻ
const User = require('./models/User');
const Song = require('./models/Song');
```

### Các thao tác cơ bản

#### 1. Tạo mới (Create)

```javascript
// Tạo user mới
const newUser = await User.create({
  email: 'user@example.com',
  password_hash: hashedPassword,
  display_name: 'John Doe'
});

// Tạo song mới
const newSong = await Song.create({
  title: 'Bài hát mới',
  artist_id: 1,
  album_id: 1,
  audio_url: 'https://example.com/song.mp3',
  duration: 240,
  genre: 'Pop'
});
```

#### 2. Tìm kiếm (Find)

```javascript
// Tìm user theo ID
const user = await User.findByPk(1);

// Tìm user theo email
const user = await User.findOne({ where: { email: 'test@example.com' } });

// Tìm tất cả songs
const songs = await Song.findAll();

// Tìm songs với điều kiện
const popSongs = await Song.findAll({
  where: { genre: 'Pop' },
  limit: 10,
  order: [['play_count', 'DESC']]
});
```

#### 3. Sử dụng Relationships (Eager Loading)

```javascript
// Lấy song kèm artist và album
const song = await Song.findByPk(1, {
  include: [
    { model: Artist, as: 'artist' },
    { model: Album, as: 'album' }
  ]
});

console.log(song.title);
console.log(song.artist.name);
console.log(song.album.title);

// Lấy playlist kèm songs
const playlist = await Playlist.findByPk(1, {
  include: [
    { 
      model: Song, 
      as: 'songs',
      through: { attributes: ['position'] } // Lấy thêm position từ junction table
    }
  ]
});

// Lấy artist kèm tất cả albums và songs
const artist = await Artist.findByPk(1, {
  include: [
    { 
      model: Album, 
      as: 'albums',
      include: [{ model: Song, as: 'songs' }]
    },
    { model: Song, as: 'songs' }
  ]
});
```

#### 4. Cập nhật (Update)

```javascript
// Cập nhật user
await User.update(
  { display_name: 'New Name' },
  { where: { id: 1 } }
);

// Hoặc update instance
const song = await Song.findByPk(1);
song.play_count += 1;
await song.save();
```

#### 5. Xóa (Delete)

```javascript
// Xóa theo điều kiện
await Song.destroy({ where: { id: 1 } });

// Hoặc xóa instance
const song = await Song.findByPk(1);
await song.destroy();
```

#### 6. Thêm bài hát vào playlist

```javascript
const { PlaylistSong } = require('./models');

// Thêm song vào playlist
await PlaylistSong.create({
  playlist_id: 1,
  song_id: 5,
  position: 1
});

// Hoặc dùng magic method
const playlist = await Playlist.findByPk(1);
const song = await Song.findByPk(5);
await playlist.addSong(song, { through: { position: 1 } });
```

#### 7. Thêm bài hát yêu thích

```javascript
const { Favorite } = require('./models');

// Thêm favorite
await Favorite.create({
  user_id: 1,
  song_id: 5
});

// Xóa favorite
await Favorite.destroy({
  where: {
    user_id: 1,
    song_id: 5
  }
});
```

#### 8. Search và Filter

```javascript
const { Op } = require('sequelize');

// Tìm kiếm bài hát theo tên
const results = await Song.findAll({
  where: {
    title: {
      [Op.like]: '%trao%'
    }
  },
  include: [
    { model: Artist, as: 'artist' }
  ]
});

// Lọc theo nhiều điều kiện
const songs = await Song.findAll({
  where: {
    genre: 'Pop',
    duration: {
      [Op.between]: [180, 300]
    }
  }
});
```

#### 9. Aggregation và Counting

```javascript
// Đếm số lượng songs
const songCount = await Song.count();

// Đếm songs theo genre
const popSongCount = await Song.count({ where: { genre: 'Pop' } });

// Đếm albums của artist
const artist = await Artist.findByPk(1);
const albumCount = await artist.countAlbums();
```

## Relationships Cheat Sheet

```
User
├── hasMany Playlist (user.playlists)
├── hasMany Favorite (user.favorites)
└── hasMany RefreshToken (user.refreshTokens)

Artist
├── hasMany Album (artist.albums)
└── hasMany Song (artist.songs)

Album
├── belongsTo Artist (album.artist)
└── hasMany Song (album.songs)

Song
├── belongsTo Artist (song.artist)
├── belongsTo Album (song.album)
├── belongsToMany Playlist through PlaylistSong (song.playlists)
└── hasMany Favorite (song.favorites)

Playlist
├── belongsTo User (playlist.user)
└── belongsToMany Song through PlaylistSong (playlist.songs)
```

## Best Practices

1. **Luôn sử dụng try-catch** khi làm việc với database
2. **Sử dụng transactions** cho các thao tác phức tạp
3. **Eager loading** để tránh N+1 query problem
4. **Indexes** đã được định nghĩa trong models, đảm bảo performance tốt
5. **Validation** có thể thêm trong model definition

## Example Controller

```javascript
const { Song, Artist, Album } = require('../models');

// Get all songs with artist and album info
exports.getAllSongs = async (req, res) => {
  try {
    const songs = await Song.findAll({
      include: [
        { model: Artist, as: 'artist', attributes: ['id', 'name', 'avatar_url'] },
        { model: Album, as: 'album', attributes: ['id', 'title', 'cover_url'] }
      ],
      order: [['created_at', 'DESC']]
    });

    res.json({
      success: true,
      data: songs
    });
  } catch (error) {
    console.error('Error fetching songs:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch songs'
    });
  }
};
```
