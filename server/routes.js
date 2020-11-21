export default app => {
    app.get('/user', (req, res) => {
        res.json({
            data: 'Hello, world!'
        })
    });
};