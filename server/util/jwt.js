const expressJwt = require('express-jwt');
const userService = require('../service/user-service');

function jwt() {
    const secret = process.env.JWT_SECRET;
    const issuer = process.env.JWT_ISSUER;
    return expressJwt({
        secret,
        issuer,
        algorithms: ['HS256'],
        isRevoked
    }).unless({
        path: [
            // public routes that don't require authentication
            '/users/auth',
            '/users/register'
        ]
    });
}

async function isRevoked(req, payload, done) {
    const user = await userService.getUserById(payload.sub);

    // revoke token if user no longer exists
    if (!user) {
        return done(null, true);
    }

    done();
}

module.exports = jwt;