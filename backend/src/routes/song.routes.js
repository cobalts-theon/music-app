const express = require('express');
const router = express.Router();
const songController = require('../controllers/song.controller');
const { protect, optionalAuth, requireAdmin } = require('../middleware/auth');

router.get('/', optionalAuth, songController.getAllSongs);
router.get('/:id', optionalAuth, songController.getSong);
router.post('/', protect, requireAdmin, songController.createSong);
router.put('/:id', protect, requireAdmin, songController.updateSong);
router.delete('/:id', protect, requireAdmin, songController.deleteSong);

module.exports = router;
