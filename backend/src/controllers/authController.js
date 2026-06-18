const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { User } = require('../models/MySQLModels');

const generateToken = (userId) => {
    return jwt.sign(
        { userId },
        process.env.JWT_SECRET,
        { expiresIn: process.env.JWT_EXPIRES_IN || '7d' }
    );
};

// POST /api/auth/register
const register = async (req, res) => {
    try {
        const { email, password, displayName } = req.body;

        if (!email || !password) {
            return res.status(400).json({ error: 'Email và password là bắt buộc' });
        }
        if (password.length < 6) {
            return res.status(400).json({ error: 'Password phải có ít nhất 6 ký tự' });
        }

        const existing = await User.findOne({ where: { email } });
        if (existing) {
            return res.status(409).json({ error: 'Email đã được sử dụng' });
        }

        const passwordHash = await bcrypt.hash(password, 10);

        const user = await User.create({
            email,
            passwordHash,
            displayName: displayName || email.split('@')[0]
        });

        const token = generateToken(user.id);

        res.status(201).json({
            success: true,
            token,
            user: {
                id: user.id,
                email: user.email,
                displayName: user.displayName
            }
        });
    } catch (err) {
        console.error('register error:', err);
        res.status(500).json({ error: 'Lỗi đăng ký: ' + err.message });
    }
};

// POST /api/auth/login
const login = async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ error: 'Email và password là bắt buộc' });
        }

        const user = await User.findOne({ where: { email } });
        if (!user || !user.passwordHash) {
            return res.status(401).json({ error: 'Email hoặc password không đúng' });
        }

        const isValid = await bcrypt.compare(password, user.passwordHash);
        if (!isValid) {
            return res.status(401).json({ error: 'Email hoặc password không đúng' });
        }

        const token = generateToken(user.id);

        res.json({
            success: true,
            token,
            user: {
                id: user.id,
                email: user.email,
                displayName: user.displayName
            }
        });
    } catch (err) {
        console.error('login error:', err);
        res.status(500).json({ error: 'Lỗi đăng nhập: ' + err.message });
    }
};

// GET /api/auth/me — lấy thông tin user hiện tại (cần token)
const getMe = async (req, res) => {
    try {
        const user = await User.findByPk(req.userId, {
            attributes: ['id', 'email', 'displayName', 'createdAt']
        });
        if (!user) {
            return res.status(404).json({ error: 'Không tìm thấy user' });
        }
        res.json({ success: true, user });
    } catch (err) {
        console.error('getMe error:', err);
        res.status(500).json({ error: err.message });
    }
};

module.exports = { register, login, getMe };