const express = require('express');
const router = express.Router();
const { saveAiResult, getAiResult, getAiResultsBySession } = require('../controllers/aiResultController');

router.post('/', saveAiResult);
router.get('/session/:sessionId', getAiResultsBySession);
router.get('/:id', getAiResult);

module.exports = router;
