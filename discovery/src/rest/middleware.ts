import { NextFunction, Request, Response } from "express";
import jwt, { VerifyErrors } from "jsonwebtoken";
import { TokenPayload } from "../wss/events.js";
import { createUser, User, UserTypes } from "../wss/user.js";

declare global {
    namespace Express {
        interface Request {
            user: User
        }
    }
}

export const authenticateToken = async (req: Request, res: Response, next: NextFunction) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (token == null) return res.sendStatus(401);

    jwt.verify(token, process.env.JWT_KEY, async (err: VerifyErrors, user: TokenPayload) => {
        if (err) return res.sendStatus(403);

        req.user = await (createUser(user.type === "SSO" ? UserTypes.SSO : UserTypes.Classic)).fromID(user.uuid)

        next();
    });
};
