const express = require('express');
const router = express.Router();
const { addFavorite, getFavorites, deleteFavorite } = require('../controllers/favoriteController.js');
const { requireAuth } = require('../middleware/authMiddleware');

router.post('/', requireAuth, addFavorite);
router.get('/', requireAuth, getFavorites);
router.delete('/:id', requireAuth, deleteFavorite);

module.exports = router;