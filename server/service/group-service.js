const groupDao = require('../dao/groups-dao');
const userService = require('./user-service');
const Exception = require("../util/exception");
const {errorCodes} = require("../util/error-handler");
const crypto = require('crypto');

const groupMapper = (group, userMap) => {
    const {id, name, users, places} = group;

    return {
        id,
        name,
        users: users.map(user => userLinkMapper(user, userMap)),
        places: places.map(placesMapper)
    }
}

const userLinkMapper = (userLink, userMap) => {
    const {userId, locationsKey, placesKey} = userLink;

    return {
        id: userId,
        alias: userMap[userId].alias,
        userPublicKey: userMap[userId].publicKey,
        locationsKey,
        placesKey
    }
}

const userReducer = (users) => users.reduce((userMap, user) => {
    userMap[user.id] = user;
    return userMap;
}, {})

const placesMapper = (place) => {
    const {id, description, latitude, longitude} = place;

    return {id, description, latitude, longitude};
}

const algorithm = 'aes-256-cbc';

class GroupService {

    async createGroup(newGroupName, userId) {
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

    async updateGroupById(groupId, userId, name) {
        const groupRead = (await groupDao.getGroupsByUser(userId, groupId))[0];

        if (!groupRead) {
            throw new Exception(
                errorCodes.GROUP_NOT_FOUND,
                `Group with id ${groupId} not found`
            )
        }

        const group = await groupDao.updateGroupById(groupId, name);

        const userMap = await userService
            .getUserByIds(group.users.map(link => link.userId))
            .then(users => userReducer(users));

        return groupMapper(group, userMap);
    }

    async getGroupsByUser(userId) {
        const groups = await groupDao.getGroupsByUser(userId);

        const userMap = await userService
            .getUserByIds(groups.flatMap(group => group.users.map(link => link.userId)))
            .then(users => userReducer(users));

        return groups.map(group => groupMapper(group, userMap));
    }

    async getGroupsByUserAndId(userId, groupId) {
        const group = (await groupDao.getGroupsByUser(userId, groupId))[0];

        if (!group) {
            throw new Exception(
                errorCodes.GROUP_NOT_FOUND,
                `Group with id ${groupId} not found`
            )
        }

        const userMap = await userService
            .getUserByIds(group.users.map(link => link.userId))
            .then(users => userReducer(users));

        return groupMapper(group, userMap);
    }

    async deleteGroupById(groupId, userId) {
        const group = (await groupDao.getGroupsByUser(userId, groupId))[0];

        if (!group) {
            throw new Exception(
                errorCodes.GROUP_NOT_FOUND,
                `Group with id ${groupId} not found`
            )
        }

        return await groupDao.deleteGroupById(groupId);
    }

}

module.exports = new GroupService();