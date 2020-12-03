const groupDao = require('../dao/groups-dao');
const userService = require('../service/user-service');
const crypto = require('crypto');

const algorithm = 'aes-256-cbc';

async function createGroup(newGroupName, userId) {
    const user = await userService.getUserById(userId);

    const locationsKey = crypto.randomBytes(32);
    const placesKey = crypto.randomBytes(32);

    const iv = crypto.randomBytes(16);
    const cipher = crypto.createCipheriv(algorithm, locationsKey, iv);

    const encryptedName = `${iv.toString('base64')}:${cipher.update(newGroupName, 'utf8', 'base64')}${cipher.final('base64')}`;
    const locationsKeyPublic = crypto.publicEncrypt({
        key: `-----BEGIN PUBLIC KEY-----\n${user.publicKey}\n-----END PUBLIC KEY-----`,
        padding: crypto.constants.RSA_PKCS1_OAEP_PADDING,
        oaepHash: "sha256"
    }, locationsKey).toString('base64');
    const placesKeyPublic = crypto.publicEncrypt({
        key: `-----BEGIN PUBLIC KEY-----\n${user.publicKey}\n-----END PUBLIC KEY-----`,
        padding: crypto.constants.RSA_PKCS1_OAEP_PADDING,
        oaepHash: "sha256"
    }, placesKey).toString('base64');

    const {id, name} = await groupDao.createGroup(encryptedName, userId, locationsKeyPublic, placesKeyPublic);

    return {
        id,
        name,
        users: [{
            id: user.id,
            alias: user.alias,
            userPublicKey: user.publicKey,
            locationsKeyPublic,
            placesKeyPublic
        }],
        places: []
    }
}