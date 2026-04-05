const express = require('express');
const router = express.Router();
const uploadController = require('../controllers/upload.controller');
const { protect } = require('../middleware/auth');
const upload = require('../middleware/upload');

router.post('/image', protect, upload.single('image'), uploadController.uploadImage);

module.exports = router;
