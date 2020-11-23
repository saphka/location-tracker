const userDao = require('../dao/user-dao');
const bcrypt = require('bcryptjs');
const Exception = require('../util/exception');
const jwt = require('jsonwebtoken');
const {errorCodes} = require('../util/error-handler');

class UserService {

    async register(alias, publicKey, password) {
        const passwordHash = bcrypt.hashSync(password, 10);
        return await userDao.createUser(alias, publicKey, passwordHash);
    }

    async authenticate(alias, password) {
        const user = (await userDao.getUsers([alias]))[0];

        if (user && bcrypt.compareSync(password, user.password)) {
            const token = jwt.sign(
                {
                    sub: user.id,
                    name: user.alias,
                },
                process.env.JWT_SECRET,
                {
                    expiresIn: '7d',
                    issuer: process.env.JWT_ISSUER
                }
            );
            return {
                token
            };
        } else {
            throw new Exception(
                errorCodes.BAD_AUTH,
                `User or password incorrect`);
        }
    }

    async getUserById(id) {
        return await userDao.getUserById(id);
    }

    async updateUserById(id, alias, publicKey) {
        const user = await this.getUserById(id);
        if (!user) {
            throw new Exception(
                errorCodes.USER_NOT_FOUND,
                `User with id ${id} not found`
            )
        }

        return await userDao.updateUserById(id, alias, publicKey);
    }

}

module.exports = new UserService();