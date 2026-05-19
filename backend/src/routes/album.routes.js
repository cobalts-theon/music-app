const express = require('express');
const router = express.Router();
const albumController = require('../controllers/album.controller');
const { protect, optionalAuth, requireAdmin } = require('../middleware/auth');

router.get('/', optionalAuth, albumController.getAllAlbums);
router.get('/:id', optionalAuth, albumController.getAlbum);
router.post('/', protect, requireAdmin, albumController.createAlbum);
router.put('/:id', protect, requireAdmin, albumController.updateAlbum);
router.delete('/:id', protect, requireAdmin, albumController.deleteAlbum);

module.exports = router;
