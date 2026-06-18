const { SearchHistory } = require('../models/MySQLModels');

// POST /api/history — cần đăng nhập, lấy userId từ token (req.userId do middleware gắn)
const saveHistory = async (req, res) => {
    try {
        const { sessionId, query, mode, resultCount, aiResultId } = req.body;

        if (!sessionId || !query || !mode) {
            return res.status(400).json({ error: 'sessionId, query, mode là bắt buộc' });
        }

        const history = await SearchHistory.create({
            userId: req.userId || null,
            sessionId,
            query,
            mode,
            resultCount: resultCount || 0,
            aiResultId: aiResultId || null
        });

        res.status(201).json({
            success: true,
            id: history.id,
            message: 'History saved to MySQL'
        });
    } catch (err) {
        console.error('saveHistory error:', err);
        res.status(500).json({ error: 'Lỗi lưu history: ' + err.message });
    }
};

// GET /api/history?page=1&limit=20 — chỉ trả history của user đang đăng nhập
const getHistory = async (req, res) => {
    try {
        const { page = 1, limit = 20 } = req.query;
        const offset = (parseInt(page) - 1) * parseInt(limit);

        const { count, rows } = await SearchHistory.findAndCountAll({
            where: { userId: req.userId },
            order: [['created_at', 'DESC']],
            limit: parseInt(limit),
            offset
        });

        res.json({
            success: true,
            data: rows,
            total: count,
            page: parseInt(page),
            totalPages: Math.ceil(count / parseInt(limit))
        });
    } catch (err) {
        console.error('getHistory error:', err);
        res.status(500).json({ error: err.message });
    }
};

// DELETE /api/history/:id — chỉ xoá nếu thuộc về user đang đăng nhập
const deleteHistory = async (req, res) => {
    try {
        const deleted = await SearchHistory.destroy({
            where: { id: req.params.id, userId: req.userId }
        });
        if (!deleted) return res.status(404).json({ error: 'Không tìm thấy history' });
        res.json({ success: true, message: 'Đã xoá' });
    } catch (err) {
        console.error('deleteHistory error:', err);
        res.status(500).json({ error: err.message });
    }
};

// DELETE /api/history/all — xoá toàn bộ history của user đang đăng nhập
const clearHistory = async (req, res) => {
    try {
        await SearchHistory.destroy({ where: { userId: req.userId } });
        res.json({ success: true, message: 'Đã xoá toàn bộ lịch sử' });
    } catch (err) {
        console.error('clearHistory error:', err);
        res.status(500).json({ error: err.message });
    }
};

module.exports = { saveHistory, getHistory, deleteHistory, clearHistory };