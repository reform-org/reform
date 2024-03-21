import app from './rest/server.js';
import webSocketServer from './wss/socket.js';
import { db } from "./utils/db.js";
import { Mailer } from './rest/mailer.js';
import * as Globals from "./utils/globals.js";

db.init();

(async () => {
  if(Mailer.isSetup()) {
    const mailer = Mailer.getInstance()
    await mailer.createConnection()
  }
})();

app.listen(Globals.VITE_DISCOVERY_SERVER_LISTEN_PORT || 3000, () => {
  console.log(`REST server listening on port ${Globals.VITE_DISCOVERY_SERVER_LISTEN_PORT || 3000}`);
});

webSocketServer.listen(Globals.VITE_DISCOVERY_SERVER_WEBSOCKET_LISTEN_PORT || 7071, () => {
  console.log(`WSS server listening on port ${Globals.VITE_DISCOVERY_SERVER_WEBSOCKET_LISTEN_PORT || 7071}`);
});
