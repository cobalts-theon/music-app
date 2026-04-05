const express = require('express');
const router = express.Router();
const albumController = require('../controllers/album.controller');
const { protect, optionalAuth } = require('../middleware/auth');

router.get('/', optionalAuth, albumController.getAllAlbums);
router.get('/:id', optionalAuth, albumController.getAlbum);
router.post('/', protect, albumController.createAlbum);
router.put('/:id', protect, albumController.updateAlbum);
router.delete('/:id', protect, albumController.deleteAlbum);

module.exports = router;
