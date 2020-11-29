const errorCodes = {
    USER_NOT_FOUND: 'USER_NOT_FOUND',
    BAD_AUTH: 'BAD_AUTH',
    GROUP_NOT_FOUND: 'GROUP_NOT_FOUND'
}

const codeStatusMap = {
    USER_NOT_FOUND: 404,
    GROUP_NOT_FOUND: 404,
    BAD_AUTH: 400
}

function errorHandler(err, req, res, next) {
    if (typeof (err) === 'string') {
        // custom application error
        return res.status(400).json(
            {
                code: 'UNKNOWN_ERROR',
                message: err
            });
    }

    console.error(err);

    return res.status(codeStatusMap[err.code] || 500).json({
        code: err.code || 'INTERNAL_SERVER_ERROR',
        message: err.message
    });
}

module.exports = {
    errorHandler,
    errorCodes
}