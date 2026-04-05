const express = require('express');
const router = express.Router();
const artistController = require('../controllers/artist.controller');
const { protect, optionalAuth } = require('../middleware/auth');

router.get('/', optionalAuth, artistController.getAllArtists);
router.get('/:id', optionalAuth, artistController.getArtist);
router.post('/', protect, artistController.createArtist);
router.put('/:id', protect, artistController.updateArtist);
router.delete('/:id', protect, artistController.deleteArtist);

module.exports = router;
