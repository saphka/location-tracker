const {Pool} = require('pg')

const database = new Pool();

const runInTransaction = async (clientCallback) => {
    const client = await database.connect();
    let result;

    try {
        await client.query('BEGIN');
        result = await clientCallback(client);
        await client.query('COMMIT');
    } catch (e) {
        await client.query('ROLLBACK')
        throw e
    } finally {
        client.release();
    }
    return result;
}

module.exports = {
    database,
    runInTransaction
};