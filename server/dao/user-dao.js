const {database, runInTransaction} = require('./database');

const rowMapper = row => {
    return {
        id: row.id,
        alias: row.user_alias,
        publicKey: row.public_key,
        password: row.password_hash
    }
};

class UserDao {
    getUsers(aliases) {
        return database
            .query('SELECT id, user_alias, public_key, password_hash FROM location_tracker.users' +
                ' WHERE user_alias IN (' +
                aliases.map((_, idx) => `$${idx + 1}`).join(',') +
                ')', aliases)
            .then(res => res.rows.map(rowMapper));
    }

    getUserByIds(ids) {
        return database
            .query('SELECT id, user_alias, public_key, password_hash FROM location_tracker.users ' +
                'WHERE id IN (' +
                ids.map((_, idx) => `$${idx + 1}`).join(',') +
                ')', ids)
            .then(res => res.rows.map(rowMapper));
    }

    createUser(alias, publicKey, password) {
        return database
            .query('INSERT INTO location_tracker.users(user_alias, public_key, password_hash) VALUES ($1, $2, $3) RETURNING *', [
                alias,
                publicKey,
                password
            ])
            .then(res => rowMapper(res.rows[0]));
    }

    async updateUserById(id, alias, publicKey) {
        return await runInTransaction(async (client) => {
            if (publicKey !== undefined) {
                await client.query('DELETE FROM location_tracker.user_group_links WHERE user_id = $1', [id]);
                await client.query('UPDATE location_tracker.users SET public_key = $1 WHERE id = $2', [publicKey, id]);
            }

            if (alias !== undefined) {
                await client.query('UPDATE location_tracker.users SET user_alias = $1 WHERE id = $2', [alias, id]);
            }
            return await this.getUserByIds([id]).then(rows => rows[0]);
        });
    }
}

module.exports = new UserDao();