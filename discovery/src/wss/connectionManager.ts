import { WebSocket } from "ws";
import { db } from "../utils/db.js";
import { Connection } from "./connection.js";
import { Peer } from "./peer.js";
import { User } from "./user.js";

export class ConnectionManager{
    private connections: Array<Connection> = []
    private peers: Array<Peer> = []

    public size() {
        return this.connections.length
    }

    /**
     * Adds a new connection and has no further side effects
     * @param connection the connection will be added to the list of connections
     */
    public addConnection(connection: Connection) {
        this.connections.push(connection)
    }

    /**
     * Removes a connection and has no further side effects
     * @param connection the connection will be removed from the list of connections
     */
    public removeConnection(id: string) {
        this.connections = this.connections.filter(p => p.id !== id)
        this.broadcastConnectionInfo() 
    }

    /**
     * Returns a single connection
     * @param id the id of a connection
     * @returns null if no connection has been found with the given id, the connection object which can be edited inplace, e.g. all changes to the returned object are reflected in the datastructure
     */
    public getConnection(id: string) {
        const index = this.connections.findIndex(p => p.id === id)
        if(index < 0) return null
        return this.connections[index]
    }

    /**
     * Returns an array of connections where the user is either host or client
     * @param user 
     * @returns an array of connections where the user is either host or client or an empty array if there are non such connections
     */
    public getConnections(user: User) {
        return this.connections.filter(p => p.client.user.id === user.id || p.host.user.id === user.id)
    }

    /**
     * Establishes a connection between two peers, where one peer is labelled host and one is labelled client
     * - The connection is only established if the two peers are not already connected
     * - loopback connections are not allowed
     * 
     * The connection will be added to the list of connections and has the initial state of Pending.
     * A request_host_token message is sent to the host to initiate the connection flow.
     * 
     * @param host 
     * @param client 
     */
    public connect(host: Peer, client: Peer) {
        if(this.checkConnected(host, client)) return;
        if(!this.checkConnectionIsAllowed(host, client)) return;
        if(host.socket === client.socket) return;
        const connection = new Connection(host, client)
        this.addConnection(connection)
        connection.requestHostToken()
    }

    /**
     * Checks if two peers are already connected, the state is ignored and therefore Pending connections are counted as well
     * @param a 
     * @param b 
     * @returns true if the two peers are connected
     */
    private checkConnected(a: Peer, b: Peer): Boolean {
        return this.connections.filter(p => (p.client === a && p.host === b) || (p.client === b && p.host === a)).length > 0
    }

    private async checkConnectionIsAllowed(a: Peer, b: Peer): Promise<Boolean> {
        return (await a.user.getConnectableUsers()).filter(p => p.id === b.user.id).length > 0
    }


    public addPeer(peer: Peer) {
        this.peers.push(peer)
        db.instance.run("INSERT OR REPLACE INTO devices VALUES(?, ?)", peer.user.id, peer.device);
    }

    public removePeer(ws: WebSocket) {
        const peer = this.getPeer(ws);
        if(!peer) return;
        db.instance.run("DELETE FROM devices WHERE device_uuid = ?", peer.device);
        this.peers = this.peers.filter(p => p.socket !== ws)
        const openConnections = this.connections.filter(p => p.client.socket === ws || p.host.socket === ws)
        openConnections.forEach(connection => connection.close(peer))
        this.connections = this.connections.filter(p => !(p.client.socket === ws || p.host.socket === ws))
    }

    public getPeer(ws: WebSocket) {
        return this.peers.find(p => p.socket == ws)
    }

    public getPeers(user: User) {
        return this.peers.filter(p => p.user.id === user.id)
    }

    public getPeersById(userId: string) {
        return this.peers.filter(p => p.user.id === userId)
    }

    public getAllPeers() {
        return this.peers
    }

    public async broadcastConnectionInfo() {
        await Promise.all(this.peers.map(peer => peer.sendConnectionInfo(this.peers)))
    }
}