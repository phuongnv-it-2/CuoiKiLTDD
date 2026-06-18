const jwt = require('jsonwebtoken');

// Middleware bắt buộc phải đăng nhập — chặn request nếu không có/sai token
const requireAuth = (req, res, next) => {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ error: 'Thiếu token xác thực' });
    }

    const token = authHeader.split(' ')[1];

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.userId = decoded.userId;
        next();
    } catch (err) {
        return res.status(401).json({ error: 'Token không hợp lệ hoặc đã hết hạn' });
    }
};

// Middleware tùy chọn — không chặn nếu thiếu token, nhưng gắn userId nếu có
const optionalAuth = (req, res, next) => {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        req.userId = null;
        return next();
    }

    const token = authHeader.split(' ')[1];

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.userId = decoded.userId;
    } catch (err) {
        req.userId = null;
    }
    next();
};

module.exports = { requireAuth, optionalAuth }; 