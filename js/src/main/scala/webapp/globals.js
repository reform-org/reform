/// <reference types="vite/client" />

export const VITE_SELENIUM = import.meta.env.VITE_SELENIUM === "true";

export const VITE_SERVER_PROTOCOL = import.meta.env.VITE_SERVER_PROTOCOL
export const VITE_SERVER_HOST = import.meta.env.VITE_SERVER_HOST
export const VITE_SERVER_PORT = import.meta.env.VITE_SERVER_PORT

export const VITE_DISCOVERY_SERVER_PROTOCOL = import.meta.env.VITE_DISCOVERY_SERVER_PROTOCOL
export const VITE_DISCOVERY_SERVER_HOST = import.meta.env.VITE_DISCOVERY_SERVER_HOST
export const VITE_DISCOVERY_SERVER_PORT = import.meta.env.VITE_DISCOVERY_SERVER_PORT

export const VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL = import.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_PROTOCOL
export const VITE_DISCOVERY_SERVER_WEBSOCKET_HOST = import.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_HOST
export const VITE_DISCOVERY_SERVER_WEBSOCKET_PORT = import.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_PORT

export const VITE_TURN_SERVER_HOST = import.meta.env.VITE_TURN_SERVER_HOST
export const VITE_TURN_SERVER_PORT = import.meta.env.VITE_TURN_SERVER_PORT

export const VITE_ALWAYS_ONLINE_PEER_PROTOCOL = import.meta.env.VITE_ALWAYS_ONLINE_PEER_PROTOCOL
export const VITE_ALWAYS_ONLINE_PEER_HOST = import.meta.env.VITE_ALWAYS_ONLINE_PEER_HOST
export const VITE_ALWAYS_ONLINE_PEER_PORT = import.meta.env.VITE_ALWAYS_ONLINE_PEER_PORT