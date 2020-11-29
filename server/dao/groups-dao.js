const {database, runInTransaction} = require('./database');

const rowMapper = row => {
    return {
        id: row.id,
        name: row.group_name
    }
};

const userLinkRowMapper = row => {
    return {
        id: row.id,
        userId: row.user_id,
        groupId: row.group_id,
        locationsKey: row.locations_key,
        placesKey: row.places_key
    }
}

const placesMapper = row => {
    return {
        id: row.id,
        description: row.description,
        groupId: row.group_id,
        latitude: row.latitude,
        longitude: row.longitude
    }
}

const readUsers = async (database, groupIds) => {
    return await database
        .query('SELECT id, user_id, group_id, locations_key, places_key FROM location_tracker.user_group_links' +
            ' WHERE group_id IN (' +
            groupIds.map((_, idx) => `$${idx + 1}`).join(',') +
            ')', groupIds)
        .then(res => res.rows.map(userLinkRowMapper))
        .then(rows => rows.reduce((map, link) => {
            let groupLinks = map[link.groupId] || [];
            groupLinks.push(link);
            map[link.groupId] = groupLinks;
            return map;
        }, {}));
}

const readPlaces = async (database, groupIds) => {
    return await database
        .query('SELECT id, group_id, description, latitude, longitude FROM location_tracker.places' +
            ' WHERE group_id IN (' +
            groupIds.map((_, idx) => `$${idx + 1}`).join(',') +
            ')', groupIds)
        .then(res => res.rows.map(placesMapper))
        .then(rows => rows.reduce((map, link) => {
            let groupPlaces = map[link.groupId] || [];
            groupPlaces.push(link);
            map[link.groupId] = groupPlaces;
            return map;
        }, {}));
}

class GroupDao {

    async getGroupsByUser(userId, groupId) {
        let query = `
            SELECT id, group_name
            FROM location_tracker.groups gr
            WHERE EXISTS(
                          SELECT 1
                          FROM location_tracker.user_group_links l
                          WHERE l.group_id = gr.id
                            AND l.user_id = $1
                      )
        `
        let args = [userId]
        if (groupId) {
            query += ' AND id = $2';
            args.push(groupId);
        }

        const groups = await database
            .query(query, args)
            .then(res => res.rows.map(rowMapper));

        const groupIds = groups.map(group => group.id);
        const usersMap = await readUsers(database, groupIds)
        const placesMap = await readPlaces(database, groupIds);

        return groups.map(group => {
            return {
                ...group,
                users: usersMap[group.id] || [],
                places: placesMap[group.id] || []
            }
        });
    }

    async createGroup(name, userId, locationsKey, placesKey) {
        return await runInTransaction(async (client) => {
            const group = await client
                .query('INSERT INTO location_tracker.groups(group_name) VALUES ($1) RETURNING *', [name])
                .then(res => rowMapper(res.rows[0]));

            const userLink = await client
                .query('INSERT INTO location_tracker.user_group_links(user_id, group_id, locations_key, places_key) VALUES ($1,$2,$3,$4) RETURNING *',
                    [userId, group.id, locationsKey, placesKey])
                .then(res => userLinkRowMapper(res.rows[0]));

            return {
                ...group,
                users: [userLink],
                places: []
            };
        });
    }

    async updateGroupById(id, name) {
        const group = await database
            .query('UPDATE location_tracker.groups SET group_name = $1 WHERE id = $2 RETURNING *', [name, id])
            .then(res => rowMapper(res.rows[0]));

        return {
            ...group,
            users: (await readUsers(database, [group.id]))[group.id] || [],
            places: (await readPlaces(database, [group.id]))[group.id] || []
        }
    }

    async deleteGroupById(id) {
        return await database
            .query('DELETE FROM location_tracker.groups WHERE id = $1', [id]);
    }
}

module.exports = new GroupDao();