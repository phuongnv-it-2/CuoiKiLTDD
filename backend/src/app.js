require('dotenv').config();
const express = require('express');
const cors = require('cors');
const morgan = require('morgan');

const connectMongo = require('./config/mongo');
const { connectMySQL } = require('./config/mysql');

const aiResultsRouter = require('./routes/aiResults');
const historyRouter = require('./routes/history');
const authRouter = require('./routes/auth');
const favoritesRouter = require('./routes/favorites');
const translateRouter = require('./routes/translate');

const app = express();

// Middleware
app.use(cors());
app.use(morgan('dev'));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Routes
app.use('/api/auth', authRouter);                // Đăng ký / đăng nhập (MySQL)
app.use('/api/ai-results', aiResultsRouter);      // MongoDB
app.use('/api/history', historyRouter);           // MySQL, cần đăng nhập
app.use('/api/favorites', favoritesRouter);       // MySQL, cần đăng nhập
app.use('/api/translate', translateRouter);

// Health check
app.get('/api/health', (req, res) => {
    res.json({
        status: 'ok',
        mongo: 'connected',
        mysql: 'connected',
        timestamp: new Date().toISOString()
    });
});

// 404
app.use((req, res) => {
    res.status(404).json({ error: `Route ${req.method} ${req.path} không tồn tại` });
});

// Global error handler
app.use((err, req, res, next) => {
    console.error('Unhandled error:', err);
    res.status(500).json({ error: 'Internal server error' });
});

// Start
const PORT = process.env.PORT || 3000;
const startServer = async () => {
    await connectMongo();
    await connectMySQL();
    app.listen(PORT, '0.0.0.0', () => {
        console.log(`GG Lens backend running on port ${PORT}`);
        console.log(`Health check: http://localhost:${PORT}/api/health`);
    });
};

startServer();