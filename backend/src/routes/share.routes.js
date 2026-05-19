const express = require('express');
const router = express.Router();
const shareController = require('../controllers/share.controller');

router.get('/songs/:id', shareController.shareSong);
router.get('/albums/:id', shareController.shareAlbum);
router.get('/artists/:id', shareController.shareArtist);
router.get('/users/:id', shareController.shareUser);
router.get('/playlists/:id', shareController.sharePlaylist);

module.exports = router;
