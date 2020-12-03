const {database, runInTransaction} = require('./database');

const rowMapper = row => {
    return {
        id: row.id,
        name: row.group_name
    }
};

const readUsers = async (database, groupIds) => {
    return await database
        .query('SELECT member_id, group_id FROM location_tracker.user_group_links' +
            ' WHERE group_id IN (' +
            groupIds.map((_, idx) => `$${idx + 1}`).join(',') +
            ')', groupIds)
        .then(res => res.rows.map(row => {
            return {
                userId: row.member_id,
                groupId: row.group_id
            }
        }))
        .then(rows => rows.reduce((map, link) => {
            let {userId, groupId} = link;
            let groupLinks = map[groupId] || [];
            groupLinks.push(userId);
            map[groupId] = groupLinks;
            return map;
        }, {}));
}

class GroupDao {

    async getGroupsByUser(userId, groupId) {
        let query = `
            SELECT id, group_name, color
            FROM location_tracker.groups gr
            WHERE owner_id = $1
        `
        let args = [userId]
        if (groupId) {
            query += ' AND id = $2';
            args.push(groupId);
        }

        const groups = await database
            .query(query, args)
            .then(res => res.rows.map(rowMapper));

        const usersMap = await readUsers(database, groups.map(group => group.id))

        let result = groups.map(group => {
            return {
                ...group,
                users: usersMap[group.id] || []
            }
        })

        return groupId ? result[0] : result;
    }

    async createGroup(name, color, userId) {
        return await runInTransaction(async (client) => {
            const group = await client
                .query('INSERT INTO location_tracker.groups(group_name, color, owner_id) VALUES ($1, $2, $3) RETURNING *', [name, color, userId])
                .then(res => rowMapper(res.rows[0]));

            return {
                ...group,
                users: []
            };
        });
    }

    async updateGroupById(id, name, color) {
        const group = await database
            .query('UPDATE location_tracker.groups SET group_name = $1, color = $2 WHERE id = $3 RETURNING *', [name, color, id])
            .then(res => rowMapper(res.rows[0]));

        return {
            ...group,
            users: (await readUsers(database, [group.id]))[group.id] || []
        }
    }

    async deleteGroupById(id) {
        return await database
            .query('DELETE FROM location_tracker.groups WHERE id = $1', [id]);
    }
}

module.exports = new GroupDao();