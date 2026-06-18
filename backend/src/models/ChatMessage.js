const mongoose = require('mongoose');

const ChatMessageSchema = new mongoose.Schema({
    userId: {
        type: Number,
        required: true,
        index: true
    },
    role: {
        type: String,
        enum: ['user', 'model'],
        required: true
    },
    content: {
        type: String,
        required: true
    },
    createdAt: {
        type: Date,
        default: Date.now
    }
});

module.exports = mongoose.model('ChatMessage', ChatMessageSchema);