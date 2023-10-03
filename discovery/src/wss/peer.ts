import { ping, send } from "./helpers.js"
import { generateTurnKey, TurnKey } from "./turn.js"
import { AvailableUser, User } from "./user.js"
import { WebSocket } from "ws";

export class Peer {
    device: string
    turnKey: TurnKey
    user: User
    socket: WebSocket
    token: string
    private pingCount: number = 0
    private lastConnectionInfo: Array<User> = null

    public constructor(device: string, user: User, socket: WebSocket) {
        this.device = device
        this.user = user
        this.socket = socket
        this.turnKey = generateTurnKey() 

        this.initPingSequence()
    }

    public ping(): void {
        ping(this.socket)
        this.pingCount++;
    }

    public registerPong(): void {
        this.pingCount = 0
    }

    private initPingSequence() {
        setInterval(() => {
            if (this.pingCount >= 2) {
                this.socket.close();
            } else {
                this.ping();
            }
        }, 10000);
    }

    public async sendConnectionInfo(peers: Peer[]) {
        const availableUsers = await this.user.getAvailableUsers()
        const connectionInfo: ConnectionInfo[] = []
        for(let peer of peers) {
            let user = availableUsers.find(p => p.id === peer.user.id)
            if(user) {
                connectionInfo.push({device: peer.device, ...user})
            }
        }

        if(this.lastConnectionInfo !== null && JSON.stringify(connectionInfo) === JSON.stringify(this.lastConnectionInfo)) return
        send(this.socket, {type: "available_clients", payload: {clients: connectionInfo}})
    }

    public toJSON() {
        return {
            device: this.device,
            turnKey: this.turnKey,
            user: this.user,
            token: this.token
        }
    }
}

interface ConnectionInfo extends AvailableUser {
    device: string
}