/// <reference types="vite/client" />

export const isSelenium = import.meta.env.VITE_SELENIUM == "true";

export const discoveryServerURL = import.meta.env.VITE_DISCOVERY_SERVER_URL || "https://discovery.lukasschreiber.com"

export const discoveryServerWebsocketURL = import.meta.env.VITE_DISCOVERY_SERVER_WEBSOCKET_URL || "wss://wss.discovery.lukasschreiber.com"

export const turnServerURL = import.meta.env.VITE_TURN_SERVER || "turn:lukasschreiber.com:41720"

export const alwaysOnlinePeerURL = import.meta.env.VITE_ALWAYS_ONLINE_PEER_URL
