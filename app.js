const express = require('express');
const jwt = require('./server/util/jwt');
const {errorHandler} = require("./server/util/error-handler");
const bodyParser = require('body-parser');

const userController = require('./server/controller/user-controller');

const app = express();
const port = process.env.PORT || 3000;

require('dotenv').config();

app.use(bodyParser.urlencoded({extended: false}))
app.use(bodyParser.json());

app.use(jwt());

app.use('/users', userController);

app.use(errorHandler);

app.listen(port);
console.log(`Web server listening on port ${port}`);