import { createServer } from "https";
import { readFileSync } from "fs";
import { createServer as createHttpServer } from "http";
import { WebSocket, WebSocketServer } from "ws";
import { AuthPayload, Event, ConnectionPayload, Payload, TokenPayload, TransmitTokenPayload, SingleUserIdPayload, EmptyPayload } from "./events.js";
import jwt from "jsonwebtoken";
import { db } from "../utils/db.js";
import { ping } from "./helpers.js";
import { ConnectionManager } from "./connectionManager.js";
import { Peer } from "./peer.js";
import { createUser, User, UserTypes } from "./user.js";

export const webSocketServer = process.env.HTTPS === "TRUE" ? createServer({
    cert: readFileSync(process.env.CERT_PATH),
    key: readFileSync(process.env.KEY_PATH)
}) : createHttpServer();

const wss = new WebSocketServer({ noServer: true });

const connections = new ConnectionManager()

db.instance.exec("DELETE FROM devices")

wss.on("connection", (ws: WebSocket) => {
    ws.on("error", console.error);

    ws.on("close", async () => {
        connections.removePeer(ws)
        await connections.broadcastConnectionInfo()
    });

    ws.on("message", function (message) {
        try {
            const event: Event<Payload> = JSON.parse(message.toString());
            if (event.type === "pong" || event.type === "authenticate") {
                this.emit(event.type, event.payload);
            } else if (connections.getPeer(ws)) {
                this.emit(event.type, event.payload, connections.getPeer(ws))
            }
        } catch (err) {
            console.log('not an event', err);
        }
    })
        .on("pong", () => {
            connections.getPeer(ws).registerPong()
        })
        .on("authenticate", (payload: AuthPayload) => {
            jwt.verify(payload.token, process.env.JWT_KEY, async (err, tokenPayload: TokenPayload) => {
                if (err) {
                    ws.close();
                    return;
                };

                const user = await (createUser(tokenPayload.type === "SSO" ? UserTypes.SSO : UserTypes.Classic)).fromID(tokenPayload.uuid)
                if(!user) {
                    ws.close();
                    return;
                }

                const peer = new Peer(tokenPayload.device, user, ws)
                connections.addPeer(peer)

                // send information to client about all connections that should happen automatically now
                const connectableUsers = await user.getConnectableUsers()
                for (let client of connectableUsers) {
                    connections.getPeers(client).forEach(p => {
                        connections.connect(p, peer)
                    })
                }

                await connections.broadcastConnectionInfo()
            });
        })
        .on("connect_to", async (payload: SingleUserIdPayload, peer: Peer) => {
            connections.getPeersById(payload.uuid).forEach(p => {
                connections.connect(p, peer)
            })
        })
        .on("connection_closed", async (payload: ConnectionPayload) => {
            connections.removeConnection(payload.connection)
        })
        .on("host_token", (payload: TransmitTokenPayload) => {
            const connection = connections.getConnection(payload.connection)
            connection.host.token = payload.token
            connection.requestClientToken()
        })
        .on("client_token", (payload: TransmitTokenPayload) => {
            const connection = connections.getConnection(payload.connection)
            connection.client.token = payload.token

            connection.requestClientFinishConnection()
            connection.requestHostFinishConnection()
        })
        .on("finish_connection", (payload: ConnectionPayload, peer: Peer) => {
            connections.getConnection(payload.connection).finish(peer)
        })
        .on("request_available_clients", async (payload: EmptyPayload, peer: Peer) => {
            await peer.sendConnectionInfo(connections.getAllPeers())
        })
        .on("whitelist_add", async (payload: SingleUserIdPayload, peer: Peer) => {
            const userToTrust = await (createUser(payload.uuid.length === 8 ? UserTypes.SSO : UserTypes.Classic)).fromID(payload.uuid)
            if (!userToTrust) return;

            peer.user.trust(userToTrust)

            // connect to the new user if he is trusted
            const connectableUsers = await peer.user.getConnectableUsers()
            for (let client of connectableUsers.filter(p => p.id === userToTrust.id)) {
                connections.getPeers(client).forEach(p => {
                    connections.connect(p, peer)
                })
            }

           await connections.broadcastConnectionInfo()
        })
        .on("whitelist_del", async (payload: SingleUserIdPayload, peer: Peer) => {
            const userToUntrust = await (createUser(payload.uuid.length === 8 ? UserTypes.SSO : UserTypes.Classic)).fromID(payload.uuid)
            if (!userToUntrust) return;

            peer.user.withdrawTrust(userToUntrust)

            const connectionsToClose = connections.getConnections(peer.user).filter(p =>
                (p.client.user.id === peer.user.id && p.host.user.id === userToUntrust.id)
                || (p.host.user.id === peer.user.id && p.client.user.id === userToUntrust.id))
            connectionsToClose.forEach(connection => connection.close(peer))

            await connections.broadcastConnectionInfo()
        })
});

webSocketServer.on('upgrade', (req, res, head) => {
    wss.handleUpgrade(req, res, head, (ws) => {
        wss.emit("connection", ws);
    });
});

export default webSocketServer;