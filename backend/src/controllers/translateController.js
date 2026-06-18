const { translate } = require("google-translate-api-x");

exports.translateText = async (req, res) => {

    try {

        const { text } = req.body;

        if (!text) {

            return res.status(400).json({
                error: "Thiếu văn bản"
            });
        }

        const result = await translate(
            text,
            {
                to: "vi"
            }
        );

        res.json({

            translatedText: result.text

        });

    } catch (e) {

        res.status(500).json({

            error: e.message

        });
    }
};