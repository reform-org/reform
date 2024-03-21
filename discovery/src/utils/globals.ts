import dotenv from "dotenv";

dotenv.config({ path: '../.env' });

const Env = {
    get(name: string): string {
        if (!process.env.hasOwnProperty(name)) {
            throw new Error(`Environment variable ${name} must be set. (Did you source .env)?`);
        }

        return process.env[name];
    }
}

export const JWT_KEY = Env.get("JWT_KEY");
export const TURN_SECRET = Env.get("TURN_SECRET");

export const SMTP_HOST = Env.get("SMTP_HOST");
export const SMTP_PASSWORD = Env.get("SMTP_PASSWORD");
export const SMTP_PORT = Env.get("SMTP_PORT");
export const SMTP_SENDER = Env.get("SMTP_SENDER");
export const SMTP_USERNAME = Env.get("SMTP_USERNAME");

export const SSO_CLIENT_ID = Env.get("SSO_CLIENT_ID");
export const SSO_CLIENT_SECRET = Env.get("SSO_CLIENT_SECRET");
export const SSO_REDIRECT_URL = Env.get("SSO_REDIRECT_URL");

export const VITE_DISCOVERY_SERVER_HOST = Env.get("VITE_DISCOVERY_SERVER_HOST");
export const VITE_DISCOVERY_SERVER_LISTEN_PORT = Env.get("VITE_DISCOVERY_SERVER_LISTEN_PORT");
export const VITE_DISCOVERY_SERVER_PATH = Env.get("VITE_DISCOVERY_SERVER_PATH");
export const VITE_DISCOVERY_SERVER_PROTOCOL = Env.get("VITE_DISCOVERY_SERVER_PROTOCOL");
export const VITE_DISCOVERY_SERVER_WEBSOCKET_LISTEN_PORT = Env.get("VITE_DISCOVERY_SERVER_WEBSOCKET_LISTEN_PORT");