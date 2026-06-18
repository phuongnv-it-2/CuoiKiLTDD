const { Favorite } = require('../models/MySQLModels');

// POST /api/favorites
const addFavorite = async (req, res) => {
    try {
        const { aiResultId, label } = req.body;
        if (!aiResultId) {
            return res.status(400).json({ error: 'aiResultId là bắt buộc' });
        }

        const favorite = await Favorite.create({
            userId: req.userId,
            aiResultId,
            label: label || ''
        });

        res.status(201).json({ success: true, id: favorite.id });
    } catch (err) {
        console.error('addFavorite error:', err);
        res.status(500).json({ error: err.message });
    }
};

// GET /api/favorites
const getFavorites = async (req, res) => {
    try {
        const favorites = await Favorite.findAll({
            where: { userId: req.userId },
            order: [['created_at', 'DESC']]
        });
        res.json({ success: true, data: favorites });
    } catch (err) {
        console.error('getFavorites error:', err);
        res.status(500).json({ error: err.message });
    }
};

// DELETE /api/favorites/:id
const deleteFavorite = async (req, res) => {
    try {
        const deleted = await Favorite.destroy({
            where: { id: req.params.id, userId: req.userId }
        });
        if (!deleted) return res.status(404).json({ error: 'Không tìm thấy favorite' });
        res.json({ success: true, message: 'Đã xoá' });
    } catch (err) {
        console.error('deleteFavorite error:', err);
        res.status(500).json({ error: err.message });
    }
};

module.exports = { addFavorite, getFavorites, deleteFavorite };