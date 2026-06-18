const express = require('express');
const router = express.Router();
const { saveHistory, getHistory, deleteHistory, clearHistory } = require('../controllers/historyController');
const { requireAuth } = require('../middleware/authMiddleware.js');

router.post('/', requireAuth, saveHistory);
router.get('/', requireAuth, getHistory);
router.delete('/all', requireAuth, clearHistory);
router.delete('/:id', requireAuth, deleteHistory);

module.exports = router;