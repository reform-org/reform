export interface Event<T> {
    type: string;
    payload?: T
}

export interface Payload {}
export interface AuthPayload extends Payload {
    token: string
}
export interface TokenPayload {
    username: string
    type: string
    uuid: string
    device: string
    iat: number
    exp: number
}
export interface TransmitTokenPayload {
    connection: string
    token: string
}
export interface ConnectionPayload {
    connection: string
}
export interface SingleUserIdPayload {
    uuid: string
}

export interface EmptyPayload {}