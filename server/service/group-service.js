const groupDao = require('../dao/groups-dao');
const userService = require('./user-service');

const groupMapper = (group) => {
    return group;
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

class GroupService {

    async createGroup(newGroupName, userId) {
        const user = await userService.getUserById(userId);
        const locationsKey = '';
        const placesKey = '';
        const encryptedName = newGroupName;

        const {id, name} = await groupDao.createGroup(encryptedName, userId, locationsKey, placesKey);

        return {
            id,
            name,
            users: [{
                id: user.id,
                alias: user.alias,
                userPublicKey: user.publicKey,
                locationsKey,
                placesKey
            }],
            places: []
        }
    }

    async updateGroupById(id, name) {
        return await groupDao.updateGroupById(id, name);
    }

}

module.exports = new GroupService();