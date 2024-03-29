import express from "express";
import { db } from "../utils/db.js";
import cors from "cors";
import bodyParser from "body-parser";
import { authRouter } from "./routes/auth.js";
import { mailRouter } from "./routes/mail.js";
import * as Globals from "../utils/globals.js";

db.init()

export const app = express();
export const serverPath = Globals.VITE_DISCOVERY_SERVER_PATH;

(async () => {
    
    app.use(bodyParser.json({limit: '200mb'}));
    app.use(cors({ origin: '*' }));

    app.use(await authRouter())
    app.use(await mailRouter())

    app.use("/", express.static(process.cwd() + "/public"));
})();
export default app