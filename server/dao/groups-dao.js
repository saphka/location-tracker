const {database, runInTransaction} = require('./database');

const rowMapper = row => {
    return {
        id: row.id,
        name: row.group_name
    }
};

class GroupDao {
    getGroups(ids) {
        return database
            .query('SELECT id, group_name FROM location_tracker.groups WHERE id IN (' +
                ids.map((id, idx) => `$${idx + 1}`).join(',') +
                ')', ids)
            .then(res => res.rows.map(rowMapper));
    }

    async createGroup(name, userId, locationKey, placesKey) {
        return await runInTransaction(async (client) => {
            const group = await client
                .query('INSERT INTO location_tracker.groups(group_name) VALUES ($1) RETURNING *', [name])
                .then(res => rowMapper(res.rows[0]));

            await client
                .query('INSERT INTO location_tracker.user_group_links(user_id, group_id, location_key, places_key) VALUES ($1,$2,$3,$4) RETURNING *',
                    [userId, group.id, locationKey, placesKey]);

            return group;
        });
    }

    async updateGroupById(id, name) {
        await database
            .query('UPDATE location_tracker.groups SET group_name = $1 WHERE id = $2 RETURNING *', [name, id])
            .then(res => rowMapper(res.rows[0]));
    }
}

module.exports = new GroupDao();