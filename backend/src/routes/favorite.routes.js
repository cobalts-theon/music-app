const express = require('express');
const router = express.Router();
const favoriteController = require('../controllers/favorite.controller');
const { protect } = require('../middleware/auth');

router.use(protect);

router.get('/', favoriteController.getFavorites);
router.post('/:songId', favoriteController.addFavorite);
router.delete('/:songId', favoriteController.removeFavorite);

module.exports = router;
