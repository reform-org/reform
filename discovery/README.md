# Discovery Server

## Installation
Install with `npm install`

## User managment
Create users with running `npm run user:add` and provide username and password as you are prompted. This is an intermediate Solution until the SSO is working with the discovery server.
Currently users cannot be deleted without executing SQL directly on the database.

## Start the server
Start the server with `npm start`. Two servers will be started, one webserver for the API and one websocket server for handling the ws connections to the frontend. The Ports can be configured in the .env file, with the defaults being 3000 for the API server and 7071 for the WS Server.

In order for the jwt stuff to work you need to provide a key to sign the tokens, the key should preferably be longer than 64 characters.
Please take a look in the .env.demo to see how the key and the ports are provided.
