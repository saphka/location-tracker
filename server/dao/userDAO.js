import Database from './database'
import Exception from "../util/Exception";

class UserDAO {

    async getUser(id) {
        return Database
            .query('SELECT * FROM users WHERE id = $1', [id])
            .then(rows => {
                if (rows.length < 1) {
                    throw new Exception(
                        'USER_NOT_FOUND',
                        `User with id ${id} not found`
                    );
                }
                return rows[0];
            })
            .then(row => {
                return {
                    id: row.id,
                    alias: row.user_alias,
                    publicKey: row.public_key
                }
            })
    }

}

export default new UserDAO();