const express = require('express');
const userService = require('../service/user-service');

const userController = express.Router();

userController.post('/auth', (req, res, next) => {
    const {alias, password} = req.body;
    userService
        .authenticate(alias, password)
        .then(token => res.status(202).json(token))
        .catch(err => next(err))
});

userController.post('/register', (req, res, next) => {
    const {alias, publicKey, password} = req.body;
    userService
        .register(alias, publicKey, password)
        .then(user => res.status(201).json(user))
        .catch(err => next(err))
});

userController.get('/me', (req, res, next) => {
    userService
        .getUserById(req.user.sub)
        .then(user => res.status(200).json(user))
        .catch(err => next(err))
});

module.exports = userController;