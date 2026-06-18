const express = require('express');
const router = express.Router();
const { getChatHistory, sendChatMessage, clearChatHistory } = require('../controllers/chatController');
const { requireAuth } = require('../middleware/authMiddleware');

router.get('/', requireAuth, getChatHistory);
router.post('/', requireAuth, sendChatMessage);
router.delete('/', requireAuth, clearChatHistory);

module.exports = router;