const express = require('express');
const router = express.Router();
const songController = require('../controllers/song.controller');
const { protect, optionalAuth } = require('../middleware/auth');

router.get('/', optionalAuth, songController.getAllSongs);
router.get('/:id', optionalAuth, songController.getSong);
router.post('/', protect, songController.createSong);
router.put('/:id', protect, songController.updateSong);
router.delete('/:id', protect, songController.deleteSong);

module.exports = router;
