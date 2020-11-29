const express = require('express');
const groupService = require('../service/group-service');

const groupController = express.Router();

groupController.get('/', (req, res, next) => {
    const userId = req.user.sub;
    groupService
        .getGroupsByUser(userId)
        .then(groups => res.status(200).json(groups))
        .catch(err => next(err))
});

groupController.get('/:groupId', (req, res, next) => {
    const userId = req.user.sub;
    let {groupId} = req.params;
    groupService
        .getGroupsByUserAndId(userId, groupId)
        .then(group => res.status(200).json(group))
        .catch(err => next(err))
});

groupController.post('/', (req, res, next) => {
    const {name} = req.body;
    const userId = req.user.sub;
    groupService
        .createGroup(name, userId)
        .then(group => res.status(201).json(group))
        .catch(err => next(err))
});

groupController.patch('/:groupId', (req, res, next) => {
    const {name} = req.body;
    const userId = req.user.sub;
    let {groupId} = req.params;
    groupService
        .updateGroupById(groupId, userId, name)
        .then(group => res.status(202).json(group))
        .catch(err => next(err))
});

groupController.delete('/:groupId', (req, res, next) => {
    const userId = req.user.sub;
    const {groupId} = req.params;
    groupService
        .deleteGroupById(groupId, userId)
        .then(() => res.status(204))
        .catch(err => next(err))
})

module.exports = groupController;
