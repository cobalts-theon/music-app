const express = require('express');
const router = express.Router();
const playlistController = require('../controllers/playlist.controller');
const { protect, optionalAuth } = require('../middleware/auth');

router.get('/', protect, playlistController.getUserPlaylists);
router.get('/:id', optionalAuth, playlistController.getPlaylist);
router.post('/', protect, playlistController.createPlaylist);
router.put('/:id', protect, playlistController.updatePlaylist);
router.delete('/:id', protect, playlistController.deletePlaylist);
router.post('/:id/songs', protect, playlistController.addSongToPlaylist);
router.delete('/:id/songs/:songId', protect, playlistController.removeSongFromPlaylist);

module.exports = router;
