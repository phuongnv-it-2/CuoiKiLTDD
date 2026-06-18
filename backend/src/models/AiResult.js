const mongoose = require('mongoose');

// Schema lưu kết quả AI từ Cloud Vision / ML Kit / Gemini
const AiResultSchema = new mongoose.Schema({
    sessionId: {
        type: String,
        required: true,
        index: true
    },
    mode: {
        type: String,
        enum: ['SEARCH', 'SHOPPING', 'TRANSLATE', 'TEXT', 'QR'],
        required: true
    },
    source: {
        type: String, // 'Google Cloud Vision', 'ML Kit', 'Gemini', etc.
        default: ''
    },
    // Kết quả nhận diện nhãn (SEARCH / SHOPPING)
    detectedLabels: [
        {
            text: String,
            confidence: Number
        }
    ],
    topLabel: {
        type: String,
        default: ''
    },
    topConfidence: {
        type: Number,
        default: 0
    },
    searchQuery: {
        type: String,
        default: ''
    },
    // Kết quả dịch thuật (TRANSLATE)
    extractedText: {
        type: String,
        default: ''
    },
    translatedText: {
        type: String,
        default: ''
    },
    // Metadata
    processingTimeMs: {
        type: Number,
        default: 0
    },
    createdAt: {
        type: Date,
        default: Date.now
    }
});

module.exports = mongoose.model('AiResult', AiResultSchema);
