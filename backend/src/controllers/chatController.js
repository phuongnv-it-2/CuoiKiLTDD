const { User, SearchHistory } = require('../models/MySQLModels');
const ChatMessage = require('../models/ChatMessage');

const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const GEMINI_URL = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${GEMINI_API_KEY}`;


const systemPromptCache = new Map();

const buildSystemPrompt = async (userId) => {

    const cached = systemPromptCache.get(userId);
    if (cached && cached.expiredAt > Date.now()) {
        return cached.prompt;
    }

    const user = await User.findByPk(userId);
    const recentSearches = await SearchHistory.findAll({
        where: { userId },
        order: [['created_at', 'DESC']],
        limit: 5
    });

    const name = user?.displayName || 'bạn';
    const queries = recentSearches.map(s => s.query).filter(Boolean);

    let context = `Bạn là trợ lý AI thân thiện trong app GG Lens. Người dùng tên "${name}".`;
    if (queries.length > 0) {
        context += ` Gần đây quét: ${queries.join(', ')}.`;
    }
    context += ' Trả lời ngắn gọn, tiếng Việt, thân thiện.';

    systemPromptCache.set(userId, {
        prompt: context,
        expiredAt: Date.now() + 5 * 60 * 1000
    });

    return context;
};

const getChatHistory = async (req, res) => {
    try {
        const messages = await ChatMessage.find({ userId: req.userId })
            .sort({ createdAt: 1 })
            .limit(200);
        res.json({ success: true, data: messages });
    } catch (err) {
        console.error('getChatHistory error:', err);
        res.status(500).json({ error: err.message });
    }
};


const sendChatMessage = async (req, res) => {

    const { message } = req.body;
    if (message.length > 500) {
        return res.status(400).json({ error: 'Tin nhắn quá dài (tối đa 500 ký tự)' });
    }

    try {

        console.log('User ID:', req.userId);

        const { message } = req.body;

        console.log('Request body:', req.body);

        if (!message || !message.trim()) {

            console.log('Message rỗng');

            return res.status(400).json({
                error: 'message là bắt buộc'
            });
        }

        console.log('Message:', message);

        // ---------- SYSTEM PROMPT ----------

        const systemPrompt = await buildSystemPrompt(req.userId);

        console.log('\n=== SYSTEM PROMPT ===');

        console.log(systemPrompt);

        // ---------- HISTORY ----------

        const recentDocs = await ChatMessage.find({
            userId: req.userId
        })
            .sort({ createdAt: -1 })
            .limit(6);

        console.log('\n=== HISTORY ===');

        console.log('History count:', recentDocs.length);

        let history = recentDocs.reverse();

        if (
            history.length > 0 &&
            history[0].role !== 'user'
        ) {
            history = history.slice(1);
        }

        console.log(
            history.map(h => ({
                role: h.role,
                content: h.content
            }))
        );

        // ---------- CONTENTS ----------

        const contents = history.map(m => ({
            role: m.role,
            parts: [{ text: m.content }]
        }));

        contents.push({
            role: 'user',
            parts: [{ text: message }]
        });

        console.log('\n=== CONTENTS GỬI GEMINI ===');

        console.log(
            JSON.stringify(contents, null, 2)
        );

        // ---------- GỌI GEMINI ----------

        console.log('\n=== CALL GEMINI ===');

        console.log(GEMINI_URL);

        const geminiRes = await fetch(
            GEMINI_URL,
            {
                method: 'POST',

                headers: {
                    'Content-Type': 'application/json'
                },

                body: JSON.stringify({

                    contents,

                    systemInstruction: {

                        parts: [
                            {
                                text: systemPrompt
                            }
                        ]

                    }

                })

            }
        );

        console.log(
            'Gemini status:',
            geminiRes.status
        );

        const data = await geminiRes.json();

        console.log(
            '\n=== GEMINI RESPONSE ==='
        );

        console.log(
            JSON.stringify(data, null, 2)
        );

        if (!geminiRes.ok) {

            console.log(
                '\n=== GEMINI ERROR ==='
            );

            console.log(
                JSON.stringify(data, null, 2)
            );

            return res.status(502).json({
                error: 'Lỗi gọi Gemini API'
            });
        }

        const reply =
            data?.candidates?.[0]
                ?.content?.parts?.[0]
                ?.text
            || 'Xin lỗi, mình chưa hiểu';

        console.log('\n=== AI REPLY ===');

        console.log(reply);

        // ---------- SAVE MONGO ----------

        await ChatMessage.create({

            userId: req.userId,

            role: 'user',

            content: message

        });

        await ChatMessage.create({

            userId: req.userId,

            role: 'model',

            content: reply

        });

        console.log('Đã lưu DB');

        res.json({

            success: true,

            reply

        });

    } catch (err) {

        console.log(
            '\n=== SERVER ERROR ==='
        );

        console.error(err);

        res.status(500).json({

            error: err.message

        });

    }

};

const clearChatHistory = async (req, res) => {
    try {
        await ChatMessage.deleteMany({ userId: req.userId });
        res.json({ success: true, message: 'Đã xoá lịch sử chat' });
    } catch (err) {
        console.error('clearChatHistory error:', err);
        res.status(500).json({ error: err.message });
    }
};

module.exports = { getChatHistory, sendChatMessage, clearChatHistory };