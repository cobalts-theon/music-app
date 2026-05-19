const express = require('express');
const router = express.Router();
const artistController = require('../controllers/artist.controller');
const { protect, optionalAuth, requireAdmin } = require('../middleware/auth');

router.get('/', optionalAuth, artistController.getAllArtists);
router.get('/:id', optionalAuth, artistController.getArtist);
router.post('/', protect, requireAdmin, artistController.createArtist);
router.put('/:id', protect, requireAdmin, artistController.updateArtist);
router.delete('/:id', protect, requireAdmin, artistController.deleteArtist);

module.exports = router;
