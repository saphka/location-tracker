const groupDao = require('../dao/groups-dao');
const Exception = require("../util/exception");
const {errorCodes} = require("../util/error-handler");

const groupMapper = group => {
    const {id, name, users} = group;

    return {
        id,
        name,
        users
    }
}

class GroupService {

    async createGroup(name, color, userId) {
        return await groupDao.createGroup(name, color, userId);
    }

    async updateGroupById(groupId, name, color, userId) {
        //check exists
        await this.getGroupsByIdAndUser(groupId, userId);

        const group = await groupDao.updateGroupById(groupId, name, color);

        return groupMapper(group);
    }

    async getGroupsByUser(userId) {
        const groups = await groupDao.getGroupsByUser(userId);

        return groups.map(group => groupMapper(group));
    }

    async getGroupsByIdAndUser(groupId, userId) {
        const group = await groupDao.getGroupsByUser(userId, groupId);

        if (!group) {
            throw new Exception(
                errorCodes.GROUP_NOT_FOUND,
                `Group with id ${groupId} not found`
            )
        }

        return groupMapper(group);
    }

    async deleteGroupById(groupId, userId) {
        //check exists
        await this.getGroupsByIdAndUser(groupId, userId);

        return await groupDao.deleteGroupById(groupId);
    }

}

module.exports = new GroupService();