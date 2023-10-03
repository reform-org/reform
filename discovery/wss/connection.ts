import { randomUUID } from "crypto";
import { send } from "./helpers.js";
import { Peer } from "./peer.js";

export enum ConnectionState {
    Pending,
    Connected,
}

export class Connection{
    host: Peer
    client: Peer
    id: string
    state: ConnectionState = ConnectionState.Pending
    private hostConnected: boolean = false
    private clientConnected: boolean = false

    public constructor(host: Peer, client: Peer) {
        this.client = client
        this.host = host
        this.id = randomUUID()
    }

    public requestHostToken(): void {
        send(this.host.socket, {type: "request_host_token", payload: this})
    }

    public requestClientToken(): void {
        send(this.client.socket, {type: "request_client_token", payload: this})
    }

    public requestClientFinishConnection(): void {
        send(this.client.socket, {type: "request_client_finish_connection", payload: this})
    }

    public requestHostFinishConnection(): void {
        send(this.host.socket, {type: "request_host_finish_connection", payload: this})
    }

    public finish(peer: Peer): void {
        if(this.host.socket === peer.socket) this.hostConnected = true
        if(this.client.socket === peer.socket) this.clientConnected = true
        if(this.hostConnected && this.clientConnected) this.state = ConnectionState.Connected
    }

    public close(initiator: Peer): void {
        const reviever = this.client.socket === initiator.socket ? this.host : this.client
        send(reviever.socket, {type: "connection_closed", payload: {id: this.id}})
    }
}