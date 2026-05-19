const express = require('express');
const router = express.Router();
const libraryController = require('../controllers/library.controller');
const { protect } = require('../middleware/auth');

router.use(protect);

router.get('/songs', libraryController.getLibrarySongs);
router.post('/songs', libraryController.addSongToLibrary);
router.delete('/songs/:songId', libraryController.removeSongFromLibrary);

module.exports = router;
