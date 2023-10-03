import express from "express";
import { db } from "../utils/db.js";
import cors from "cors";
import bodyParser from "body-parser";
import dotenv from "dotenv"
import { authRouter } from "./routes/auth.js";
import { mailRouter } from "./routes/mail.js";

dotenv.config()

db.init()

export const app = express();
export const serverPath = process.env.VITE_DISCOVERY_SERVER_PATH;

(async () => {
    
    app.use(bodyParser.json({limit: '200mb'}));
    app.use(cors({ origin: '*' }));

    app.use(await authRouter())
    app.use(await mailRouter())
    

})();
export default app