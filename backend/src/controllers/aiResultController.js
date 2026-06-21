const AiResult = require('../models/AiResult');

const saveAiResult = async (req, res) => {
    try {
        const {
            sessionId,
            mode,
            source,
            detectedLabels,
            topLabel,
            topConfidence,
            searchQuery,
            extractedText,
            translatedText,
            processingTimeMs
        } = req.body;

        if (!sessionId || !mode) {
            return res.status(400).json({ error: 'sessionId và mode là bắt buộc' });
        }

        const aiResult = new AiResult({
            sessionId,
            mode,
            source: source || '',
            detectedLabels: detectedLabels || [],
            topLabel: topLabel || '',
            topConfidence: topConfidence || 0,
            searchQuery: searchQuery || '',
            extractedText: extractedText || '',
            translatedText: translatedText || '',
            processingTimeMs: processingTimeMs || 0
        });

        const saved = await aiResult.save();

        res.status(201).json({
            success: true,
            id: saved._id.toString(),
            message: 'AI result saved to MongoDB'
        });
    } catch (err) {
        console.error('saveAiResult error:', err);
        res.status(500).json({ error: 'Lỗi lưu AI result: ' + err.message });
    }
};

const getAiResult = async (req, res) => {
    try {
        const result = await AiResult.findById(req.params.id);
        if (!result) {
            return res.status(404).json({ error: 'Không tìm thấy AI result' });
        }
        res.json({ success: true, data: result });
    } catch (err) {
        console.error('getAiResult error:', err);
        res.status(500).json({ error: err.message });
    }
};

const getAiResultsBySession = async (req, res) => {
    try {
        const results = await AiResult.find({ sessionId: req.params.sessionId })
            .sort({ createdAt: -1 });
        res.json({ success: true, data: results });
    } catch (err) {
        console.error('getAiResultsBySession error:', err);
        res.status(500).json({ error: err.message });
    }
};

module.exports = { saveAiResult, getAiResult, getAiResultsBySession };
