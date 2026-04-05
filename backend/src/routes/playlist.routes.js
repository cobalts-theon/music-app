const express = require('express');
const router = express.Router();
const playlistController = require('../controllers/playlist.controller');
const { protect } = require('../middleware/auth');

router.use(protect);

router.get('/', playlistController.getUserPlaylists);
router.get('/:id', playlistController.getPlaylist);
router.post('/', playlistController.createPlaylist);
router.put('/:id', playlistController.updatePlaylist);
router.delete('/:id', playlistController.deletePlaylist);
router.post('/:id/songs', playlistController.addSongToPlaylist);
router.delete('/:id/songs/:songId', playlistController.removeSongFromPlaylist);

module.exports = router;
