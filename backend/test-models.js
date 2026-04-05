require('dotenv').config();
const { 
  User, 
  Artist, 
  Album, 
  Song, 
  Playlist,
  syncDatabase 
} = require('./src/models');

async function testModels() {
  try {
    console.log('🧪 Testing Sequelize Models...\n');

    // 1. Test Artist
    console.log('1. Testing Artist model...');
    const artists = await Artist.findAll();
    console.log(`   ✅ Found ${artists.length} artists`);
    if (artists.length > 0) {
      console.log(`   📌 Sample: ${artists[0].name}`);
    }

    // 2. Test Album
    console.log('\n2. Testing Album model...');
    const albums = await Album.findAll({
      include: [{ model: Artist, as: 'artist' }]
    });
    console.log(`   ✅ Found ${albums.length} albums`);
    if (albums.length > 0) {
      console.log(`   📌 Sample: "${albums[0].title}" by ${albums[0].artist.name}`);
    }

    // 3. Test Song with relationships
    console.log('\n3. Testing Song model with relationships...');
    const songs = await Song.findAll({
      include: [
        { model: Artist, as: 'artist' },
        { model: Album, as: 'album' }
      ],
      limit: 5
    });
    console.log(`   ✅ Found ${songs.length} songs`);
    songs.forEach((song, index) => {
      console.log(`   ${index + 1}. "${song.title}" - ${song.artist.name} (${song.duration}s)`);
    });

    // 4. Test User
    console.log('\n4. Testing User model...');
    const users = await User.findAll();
    console.log(`   ✅ Found ${users.length} users`);
    if (users.length > 0) {
      console.log(`   Sample: ${users[0].display_name} (${users[0].email})`);
    }

    // 5. Test Playlist with songs
    console.log('\n5. Testing Playlist model...');
    const playlists = await Playlist.findAll({
      include: [
        { 
          model: Song, 
          as: 'songs',
          include: [{ model: Artist, as: 'artist' }]
        }
      ]
    });
    console.log(`   Found ${playlists.length} playlists`);
    if (playlists.length > 0) {
      const playlist = playlists[0];
      console.log(`   Playlist: "${playlist.name}" (${playlist.songs.length} songs)`);
      playlist.songs.slice(0, 3).forEach((song, index) => {
        console.log(`      ${index + 1}. ${song.title} - ${song.artist.name}`);
      });
    }

    // 6. Test search functionality
    console.log('\n6. Testing search functionality...');
    const { Op } = require('sequelize');
    const searchResults = await Song.findAll({
      where: {
        title: {
          [Op.like]: '%a%'
        }
      },
      include: [{ model: Artist, as: 'artist' }],
      limit: 3
    });
    console.log(`   ✅ Found ${searchResults.length} songs containing "a"`);

    console.log('\nAll model tests passed!\n');

  } catch (error) {
    console.error('Error testing models:', error.message);
    console.error(error);
  } finally {
    process.exit();
  }
}

testModels();
