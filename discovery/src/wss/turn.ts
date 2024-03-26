import crypto from "crypto";
import * as Globals from "../utils/globals.js";

export const generateTurnKey = (): TurnKey => {
    const username = (Date.now() / 1000 + 12 * 3600).toString();
    const hmac = crypto.createHmac("sha1", Globals.TURN_SECRET);
    hmac.setEncoding("base64");
    hmac.write(username);
    hmac.end();
    const credential = hmac.read();
    return {
        username,
        credential,
    };
};

export interface TurnKey {
    username: string
    credential: String
}