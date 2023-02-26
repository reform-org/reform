/// <reference types="vite/client" />

export const VITE_SELENIUM = import.meta.env.VITE_SELENIUM === "true";
export const VITE_DISCOVERY_SERVER_PROTOCOL = import.meta.env.VITE_DISCOVERY_SERVER_PROTOCOL
export const VITE_DISCOVERY_SERVER_HOST = import.meta.env.VITE_DISCOVERY_SERVER_HOST
export const VITE_DISCOVERY_SERVER_PORT = import.meta.env.VITE_DISCOVERY_SERVER_PORT
export const VITE_DISCOVERY_SERVER_WEBSOCKET_URL = import.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_URL
export const VITE_TURN_SERVER = import.meta.env.VITE_TURN_SERVER
export const VITE_ALWAYS_ONLINE_PEER_URL = import.meta.env.VITE_ALWAYS_ONLINE_PEER_URL
